package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.CompressionUtil;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines to handle documents.
 * @author Maithilish
 *
 */
public class DocumentHelper {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    /**
     * ConfigService singleton.
     */
    @Inject
    private ConfigService configService;
    /**
     * DI singleton.
     */
    @Inject
    private DInjector dInjector;

    /**
     * private constructor.
     */
    @Inject
    private DocumentHelper() {
    }

    /**
     * Returns id of document where toDate is &gt;= runDateTime config. When,
     * there are more than one matching documents, then the id of last one is
     * returned. If, list is null then returns null.
     * @param documents
     *            list of {@link Document}, not null
     * @return active document id or null when no matching document is found or
     *         input is empty or null.
     */
    public Long getActiveDocumentId(final List<Document> documents) {
        Validate.validState(configService != null, "configService is null");

        if (documents == null) {
            return null;
        }
        Long activeDocumentId = null;
        for (Document doc : documents) {
            Date toDate = doc.getToDate();
            Date runDateTime = configService.getRunDateTime();
            // toDate > today
            if (toDate.compareTo(runDateTime) >= 0) {
                activeDocumentId = doc.getId();
            }
        }
        return activeDocumentId;
    }

    /**
     * <p>
     * Calculates document expire date from live field and from date.
     * <p>
     * Live field can hold duration (ISO-8601 duration format PnDTnHnMn.nS) or
     * date string. When live is duration then it is added to fromDate else
     * string is parsed as to date based on parse pattern provided by
     * ConfigService.
     * <p>
     * In case, live is not defined or it is zero or blank then from date is
     * returned.
     * @param fromDate
     *            document from date, not null
     * @param fields
     *            list of fields, not null
     * @return a Date which is document expire date, not null
     * @see java.time.Duration
     */
    public Date getToDate(final Date fromDate, final List<FieldsBase> fields) {

        Validate.notNull(fromDate, "fromDate must not be null");
        Validate.notNull(fields, "fields must not be null");

        Validate.validState(configService != null, "configService is null");

        // set label
        String label = "not defined";
        try {
            label = FieldsUtil.getValue(fields, "label");
        } catch (FieldNotFoundException e) {
            LOGGER.warn("{}", e.getLocalizedMessage());
        }

        // convert fromDate to DateTime
        ZonedDateTime fromDateTime = ZonedDateTime
                .ofInstant(fromDate.toInstant(), ZoneId.systemDefault());
        ZonedDateTime toDate = null;

        // extract live value
        String live = null;
        try {
            live = FieldsUtil.getValue(fields, "live");
        } catch (FieldNotFoundException e) {
            LOGGER.warn("{} - defaults to 0 day. ", e, label);
        }
        if (StringUtils.equals(live, "0") || StringUtils.isBlank(live)) {
            live = "PT0S"; // zero second
        }

        // calculate toDate
        try {
            TemporalAmount ta = Util.parseTemporalAmount(live);
            toDate = fromDateTime.plus(ta);
        } catch (DateTimeParseException e) {
            // if live is not Duration string then parse it as Date
            try {
                String[] patterns =
                        configService.getConfigArray("gotz.dateParsePattern");
                // multiple patterns so needs DateUtils
                Date td = DateUtils.parseDateStrictly(live, patterns);
                toDate = ZonedDateTime.ofInstant(td.toInstant(),
                        ZoneId.systemDefault());
            } catch (ParseException | ConfigNotFoundException pe) {
                LOGGER.warn("{} field [live] {} {}. Defaults to 0 days", label,
                        live, e);
                TemporalAmount ta = Util.parseTemporalAmount("PT0S");
                toDate = fromDateTime.plus(ta);
            }
        }

        LOGGER.trace("Document.toDate. [live] {} [toDate] {} : {}", live,
                toDate, label);
        return Date.from(Instant.from(toDate));
    }

    /**
     * <p>
     * Get uncompressed bytes of the documentObject.
     * @param document
     *            which has the documentObject, not null
     * @return uncompressed bytes of the documentObject, not null
     * @throws IOException
     *             if error closing stream
     * @throws DataFormatException
     *             if error decompress data
     */
    public byte[] getDocumentObject(final Document document)
            throws DataFormatException, IOException {
        Validate.notNull(document, "document must not be null");
        Validate.notNull(document.getDocumentObject(),
                "documentObject must not be null");

        final int bufferLength = 4086;
        return CompressionUtil.decompressByteArray(
                (byte[]) document.getDocumentObject(), bufferLength);
    }

    /**
     * <p>
     * Compresses the documentObject and sets it to Document.
     * @param document
     *            document to set, not null
     * @param documentObject
     *            object to compress and set, not null
     * @return true if success
     * @throws IOException
     *             any exception while compression
     */
    public boolean setDocumentObject(final Document document,
            final byte[] documentObject) throws IOException {
        Validate.notNull(document, "document must not be null");
        Validate.notNull(documentObject, "documentObject must not be null");

        final int bufferLength = 4086;
        byte[] compressedObject =
                CompressionUtil.compressByteArray(documentObject, bufferLength);
        document.setDocumentObject(compressedObject);
        return true;
    }

    /**
     * <p>
     * Factory method to create Document and set its fields.
     * <p>
     * Uses DI to create the Document.
     * @param name
     *            document name, not null
     * @param url
     *            document URL, not null
     * @param fromDate
     *            document start date, not null
     * @param toDate
     *            document expire date, not null
     * @return document, not null
     */
    public Document createDocument(final String name, final String url,
            final Date fromDate, final Date toDate) {
        Validate.notNull(name, "name must not be null");
        Validate.notNull(url, "url must not be null");
        Validate.notNull(fromDate, "fromDate must not be null");
        Validate.notNull(toDate, "toDate must not be null");

        Validate.validState(dInjector != null, "dInjector is null");

        Document document = dInjector.instance(Document.class);
        document.setName(name);
        document.setUrl(url);
        document.setFromDate(fromDate);
        document.setToDate(toDate);
        return document;
    }
}
