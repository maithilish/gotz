package org.codetab.gotz.step;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.DaoFactory;
import org.codetab.gotz.dao.DaoFactory.ORM;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loader extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

    private Locator locator;
    private Document document;

    private DaoFactory daoFactory;

    @Inject
    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (locator == null) {
            LOGGER.warn("{}", "unable to run loader as Locator is not set");
        } else {
            try {
                load();
                store();
                handover();
            } catch (Exception e) {
                String message = Util.buildString("load Locator[name=", locator.getName(),
                        " group=", locator.getGroup(), "]");
                LOGGER.error("{} {}", message, Util.getMessage(e));
                LOGGER.debug("{}", e);
                activityService.addActivity(Type.GIVENUP, message, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#load()
     */
    @Override
    public void load() throws Exception {
        Locator savedLocator = getLocatorFromStore(locator.getName(), locator.getGroup());
        if (savedLocator != null) {
            // update existing locator with new values
            savedLocator.getFields().addAll(locator.getFields());
            savedLocator.setUrl(locator.getUrl());
            // switch locator with existing locator (detached locator)
            locator = savedLocator;
            Util.logState(LOGGER, "locator", "--- Locator loaded from store ---",
                    locator.getFields(), locator);
        } else {
            LOGGER.debug("{}", "Locator from file is used as it is not yet stored");
        }

        Long liveDocumentId = getLiveDocumentId();
        if (liveDocumentId == null) {
            Object object = fetchDocument(locator.getUrl());
            document = new Document();
            document.setName(locator.getName());
            document.setDocumentObject(object);
            document.setFromDate(configService.getRunDateTime());
            document.setToDate(getToDate());
            document.setUrl(locator.getUrl());
            locator.getDocuments().add(document);
            setConsistent(true);
            LOGGER.info("create new document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(), document.getToDate());
            LOGGER.trace("create new document {}", document);
        } else {
            document = getDocument(liveDocumentId);
            setConsistent(true);
            LOGGER.info("use stored document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(), document.getToDate());
            LOGGER.trace("found document {}", document);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#store()
     */
    @Override
    public void store() throws Exception {
        boolean persist = true;
        try {
            persist = FieldsUtil.isFieldTrue(locator.getFields(), "persist");
        } catch (FieldNotFoundException e) {
        }

        if (persist) {
            /*
             * fields are not persistable, so need to set them from the fields.xml every
             * time
             */
            try {
                List<FieldsBase> fields = locator.getFields();
                ORM orm = DaoFactory
                        .getOrmType(configService.getConfig("gotz.datastore.orm"));
                ILocatorDao dao = daoFactory.getDaoFactory(orm).getLocatorDao();
                dao.storeLocator(locator);
                // reload locator and document
                locator = dao.getLocator(locator.getId());
                locator.getFields().addAll(fields);
                document = getDocument(document.getId());
                LOGGER.debug("Stored {}", locator);
                Util.logState(LOGGER, "locator", "--- Locator now stored ---",
                        locator.getFields(), locator);
            } catch (RuntimeException | ConfigNotFoundException e) {
                LOGGER.error("{}", e.getLocalizedMessage());
                LOGGER.trace("", e);
                throw e;
            }
        } else {
            LOGGER.debug("Persist [false]. Not Stored {}", locator);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#handover()
     */
    @Override
    public void handover() throws Exception {
        // TODO test separate instance for each call
        // for each dataDef create dedicated parser

        String givenUpMessage = Util.buildString("Create parser for locator [",
                locator.getName(), "] failed.");
        List<FieldsBase> stepsFields = FieldsUtil.getGroupFields(locator.getFields(),
                "steps");
        List<FieldsBase> dataDefFields = FieldsUtil.getGroupFields(locator.getFields(),
                "datadef");
        if (dataDefFields.size() == 0) {
            LOGGER.warn("{} {}", givenUpMessage, " No datadef field found.");
        }
        for (FieldsBase dataDefField : dataDefFields) {
            if (dataDefField instanceof Fields) {
                Fields fields = Util.deepClone(Fields.class, (Fields) dataDefField);
                if (isDocumentLoaded()) {
                    Field field = FieldsUtil.createField("locatorName",
                            locator.getName());
                    fields.getFields().add(field);
                    field = FieldsUtil.createField("locatorGroup", locator.getGroup());
                    fields.getFields().add(field);
                    field = FieldsUtil.createField("locatorUrl", locator.getUrl());
                    fields.getFields().add(field);
                    List<FieldsBase> fieldsList = new ArrayList<>();
                    fieldsList.add(fields);
                    fieldsList.addAll(stepsFields);

                    pushTask(document, fieldsList);
                } else {
                    LOGGER.warn("Document not loaded - Locator [{}]", locator);
                    activityService.addActivity(Type.GIVENUP,
                            "Document not loaded. " + givenUpMessage);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.Step#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && isDocumentLoaded());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#setInput(java.lang.Object)
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Locator) {
            this.locator = (Locator) input;
        } else {
            LOGGER.warn("Input is not instance of Locator type. {}",
                    input.getClass().toString());
        }
    }

    private Locator getLocatorFromStore(final String locName, final String locGroup)
    {
        try {
            ORM orm = DaoFactory
                    .getOrmType(configService.getConfig("gotz.datastore.orm"));
            ILocatorDao dao = daoFactory.getDaoFactory(orm).getLocatorDao();
            Locator existingLocator = dao.getLocator(locName, locGroup);
            return existingLocator;
        } catch (ConfigNotFoundException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new CriticalException("config error");
        }
    }

    private Long getLiveDocumentId(){
        Long liveDocumentId = null;
        if (locator.getId() == null) {
            // new locator so no document
            return null;
        } else {
            for (Document r : locator.getDocuments()) {
                Date toDate = r.getToDate();
                Date runDateTime = configService.getRunDateTime();
                // toDate > today
                if (toDate.compareTo(runDateTime) >= 0) {
                    liveDocumentId = r.getId();
                }
            }
        }
        return liveDocumentId;
    }

    private Date getToDate(){
        ZonedDateTime fromDate = ZonedDateTime
                .ofInstant(document.getFromDate().toInstant(), ZoneId.systemDefault());
        ZonedDateTime toDate = null;
        String live = null;
        try {
            live = FieldsUtil.getValue(locator.getFields(), "live");
        } catch (FieldNotFoundException e) {
            LOGGER.warn("{} - defaults to 0 day. Locator[name={}, group={}]", e,
                    locator.getName(), locator.getGroup());
        }
        if (StringUtils.equals(live, "0") || StringUtils.isBlank(live)) {
            live = "PT0S"; // zero second
        }
        try {
            TemporalAmount ta = Util.praseTemporalAmount(live);
            toDate = fromDate.plus(ta);
        } catch (DateTimeParseException e) {
            try {
                String[] patterns = configService.getConfigArray("gotz.dateParsePattern");
                // multiple patterns so needs DateUtils
                Date td = DateUtils.parseDateStrictly(live, patterns);
                toDate = ZonedDateTime.ofInstant(td.toInstant(), ZoneId.systemDefault());
            } catch (ParseException | ConfigNotFoundException pe) {
                LOGGER.warn("{} field [live] {} {}. Defaults to 0 days", locator, live,
                        e);
                TemporalAmount ta = Util.praseTemporalAmount("PT0S");
                toDate = fromDate.plus(ta);
            }
        }
        LOGGER.trace("Document.toDate. [live] {} [toDate] {} : {}", live, toDate,
                locator);
        return Date.from(Instant.from(toDate));
    }

    private Document getDocument(final Long id) {
        // get Document with documentObject
        try {
            ORM orm = DaoFactory
                    .getOrmType(configService.getConfig("gotz.datastore.orm"));
            IDocumentDao dao = daoFactory.getDaoFactory(orm).getDocumentDao();
            return dao.getDocument(id);
        } catch (RuntimeException | ConfigNotFoundException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new CriticalException("config error");
        }
    }

    /*
     *
     */
    public boolean isDocumentLoaded() {
        return Objects.nonNull(document);
    }

    // template method to be implemented by subclass
    public abstract Object fetchDocument(String url)
            throws Exception, MalformedURLException, IOException;
}
