package org.codetab.gotz.helper;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    @Inject
    private ConfigService configService;

    public Long getActiveDocumentId(List<Document> documents) {
        Long activeDocumentId = null;
        for (Document r : documents) {
            Date toDate = r.getToDate();
            Date runDateTime = configService.getRunDateTime();
            // toDate > today
            if (toDate.compareTo(runDateTime) >= 0) {
                activeDocumentId = r.getId();
            }
        }
        return activeDocumentId;
    }

    public Date getToDate(Date fromDate, List<FieldsBase> fields, String locatorLabel) {
        ZonedDateTime fromDateTime = ZonedDateTime.ofInstant(fromDate.toInstant(),
                ZoneId.systemDefault());
        ZonedDateTime toDate = null;
        String live = null;
        try {
            live = FieldsUtil.getValue(fields, "live");
        } catch (FieldNotFoundException e) {
            LOGGER.warn("{} - defaults to 0 day. ", e, locatorLabel);
        }
        if (StringUtils.equals(live, "0") || StringUtils.isBlank(live)) {
            live = "PT0S"; // zero second
        }
        try {
            TemporalAmount ta = Util.praseTemporalAmount(live);
            toDate = fromDateTime.plus(ta);
        } catch (DateTimeParseException e) {
            try {
                String[] patterns = configService.getConfigArray("gotz.dateParsePattern");
                // multiple patterns so needs DateUtils
                Date td = DateUtils.parseDateStrictly(live, patterns);
                toDate = ZonedDateTime.ofInstant(td.toInstant(), ZoneId.systemDefault());
            } catch (ParseException | ConfigNotFoundException pe) {
                LOGGER.warn("{} field [live] {} {}. Defaults to 0 days", locatorLabel,
                        live, e);
                TemporalAmount ta = Util.praseTemporalAmount("PT0S");
                toDate = fromDateTime.plus(ta);
            }
        }
        LOGGER.trace("Document.toDate. [live] {} [toDate] {} : {}", live, toDate,
                locatorLabel);
        return Date.from(Instant.from(toDate));
    }

}
