package org.codetab.gotz.step.base;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * <p>
 * Abstract Base Loader. Either, loads active document from store or if not
 * found, creates new document by fetching resource from from web or file
 * system. Delegates the fetch to the concrete sub class.
 * @author Maithilish
 *
 */
public abstract class BaseLoader extends Step {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(BaseLoader.class);

    /**
     * locator.
     */
    private Locator locator;
    /**
     * active document.
     */
    private Document document;
    /**
     * log marker to log state.
     */
    private Marker marker;

    /**
     * persister.
     */
    @Inject
    private LocatorPersistence locatorPersistence;
    /**
     * persister.
     */
    @Inject
    private DocumentPersistence documentPersistence;
    /**
     * helper.
     */
    @Inject
    private DocumentHelper documentHelper;

    /**
     * Creates log marker from locator name and group.
     * @return true
     * @see org.codetab.gotz.step.IStep#initialize()
     */
    @Override
    public boolean initialize() {
        Validate.validState(locator != null,
                "step input [locator] must not be null");
        marker = MarkerUtil.getMarker(locator.getName(), locator.getGroup());
        return true;
    }

    /**
     * Tries to load locator from store, if successful, then add fields and URL
     * of input locator to loaded locator and use it as input locator. If no
     * locator found in db, then input locator with its fields is used.
     *
     * @return true
     * @see org.codetab.gotz.step.IStep#load()
     */
    @Override
    public boolean load() {
        Validate.validState(locator != null,
                "step input [locator] must not be null");

        // load locator from db
        Locator savedLocator = locatorPersistence.loadLocator(locator.getName(),
                locator.getGroup());

        if (savedLocator == null) {
            // use the locator passed as input to this step
            LOGGER.debug("{} : {}", "using locator read from file : ",
                    getLabel());
        } else {
            // update existing locator with new fields and URL
            savedLocator.setFields(locator.getFields());
            savedLocator.setUrl(locator.getUrl());

            // switch locator to persisted locator (detached locator)
            locator = savedLocator;

            LOGGER.debug("{} : {}", "using locator loaded from datastore : ",
                    getLabel());
            LOGGER.trace(marker, "-- Locator loaded --{}{}", Util.LINE,
                    locator);
        }
        setStepState(StepState.LOAD);
        return true;
    }

    /**
     * <p>
     * Loads the active document for locator.
     * <p>
     * In case, locator id it not null (locator is loaded from DB), then gets
     * active document id by scanning locator documents. If document id is not
     * null, then it loads the document with its documentObject. Otherwise, it
     * creates new document and adds it locator.
     * <p>
     * When new document is created, it fetches the documentObject either from
     * web or file system and adds it to document using DocumentHelper which
     * compresses the documentObject. Other metadata like from and to dates are
     * also added to new document.
     * <p>
     *
     * @return true
     * @throws StepRunException
     *             if error when fetch document content or compressing it
     * @see org.codetab.gotz.step.IStep#process()
     */
    @Override
    public boolean process() {
        Validate.validState(locator != null,
                "step input [locator] must not be null");

        Long activeDocumentId = null;
        // locator loaded from db
        if (locator.getId() != null) {
            activeDocumentId =
                    documentHelper.getActiveDocumentId(locator.getDocuments());
        }

        if (activeDocumentId == null) {
            // no existing active document, we need to create new one
            byte[] documentObject = null;
            try {
                // fetch documentObject as byte[]
                documentObject = fetchDocumentObject(locator.getUrl());
            } catch (IOException e) {
                String givenUpMessage = Util
                        .buildString("unable to fetch document ", getLabel());
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }

            // document metadata
            Date fromDate = configService.getRunDateTime();
            Date toDate =
                    documentHelper.getToDate(fromDate, locator.getFields());

            // create new document
            document = documentHelper.createDocument(locator.getName(),
                    locator.getUrl(), fromDate, toDate);

            // set documentObject (helper method compress the bytes before set)
            try {
                documentHelper.setDocumentObject(document, documentObject);
            } catch (IOException e) {
                String errorMessage = Util.buildString(
                        "unable to compress document object ", getLabel());
                LOGGER.error("{} {}", errorMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, errorMessage, e);
                throw new StepRunException(errorMessage, e);
            }

            // add document to locator
            locator.getDocuments().add(document);

            setConsistent(true);

            LOGGER.info(
                    "create new document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(),
                    document.getToDate());
            LOGGER.trace("create new document {}", document);
        } else {
            // load the existing active document
            document = documentPersistence.loadDocument(activeDocumentId);
            setConsistent(true);
            LOGGER.info(
                    "use stored document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(),
                    document.getToDate());
            LOGGER.trace("found document {}", document);
        }
        setStepState(StepState.PROCESS);
        return true;
    }

    /**
     * <p>
     * Stores locator and its documents when persists is true. As DAO may clear
     * the data object after persist, they are reloaded. Locator fields are not
     * stored, hence they are added back upon reload.
     *
     * @return true;
     * @throws StepRunException
     *             if error when persist.
     * @see org.codetab.gotz.step.IStep#store()
     */
    @Override
    public boolean store() {
        Validate.validState(locator != null,
                "step input [locator] must not be null");
        Validate.validState(document != null, "document must not be null");

        boolean persist = true;
        try {
            persist = fieldsHelper.isTrue("/:fields/:tasks/:persist/:document",
                    locator.getFields());
        } catch (FieldsException e) {
        }

        if (persist) {
            /*
             * fields are not persistable, so need to set them from the
             * fields.xml every time
             */
            try {
                Fields fields = locator.getFields();

                // store and reload locator and document
                locatorPersistence.storeLocator(locator);
                locator = locatorPersistence.loadLocator(locator.getId());
                locator.setFields(fields);

                document = documentPersistence.loadDocument(document.getId());
                LOGGER.debug("stored Locator[{}:{}]", locator.getName(),
                        locator.getGroup());
                LOGGER.trace(marker, "-- Locator stored --{}{}", Util.LINE,
                        locator);
            } catch (RuntimeException e) {
                String givenUpMessage = "unable to store";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
        } else {
            LOGGER.debug("locator[{}:{}] is not stored as [persist=false]",
                    locator.getName(), locator.getGroup());
        }
        setStepState(StepState.STORE);
        return true;
    }

    /**
     * <p>
     * For each DataDef defined for locator, pushes task with document as input.
     * @return true
     * @throws StepRunException
     *             if datadef fields is not found
     * @see org.codetab.gotz.step.IStep#handover()
     */
    @Override
    public boolean handover() {
        // document state is not checked here as we log error for each dataDef
        Validate.validState(locator != null,
                "step input [locator] must not be null");

        String errorMessage =
                Util.buildString("create parser failed ", getLabel());

        List<Fields> tasks = null;
        try {
            tasks = fieldsHelper.split("/:fields/:tasks/:task",
                    locator.getFields());
        } catch (FieldsException e) {
            LOGGER.error("{} {}", errorMessage, e);
            activityService.addActivity(Type.GIVENUP, errorMessage, e);
            throw new StepRunException(errorMessage, e);
        }

        for (Fields task : tasks) {
            if (isDocumentLoaded()) {
                try {
                    Fields nextStepFields = createNextStepFields(task);
                    stepService.pushTask(this, document, nextStepFields);
                } catch (RuntimeException e) {
                    String message = "unable to get next step fields";
                    LOGGER.error("{} {}", message, locator);
                    activityService.addActivity(Type.GIVENUP,
                            Util.buildString(errorMessage, " : ", message));
                }
            } else {
                String message = "document not loaded";
                LOGGER.error("{} {}", message, locator);
                activityService.addActivity(Type.GIVENUP,
                        Util.buildString(errorMessage, " : ", message));
            }
        }
        setStepState(StepState.HANDOVER);
        return true;
    }

    /**
     * <p>
     * Create next step fields by deep cloning the fields. Also, adds locator
     * name,group and url fields which are useful for other steps.
     * @param fields
     *            to add to next step
     * @return list of fields for next step
     */
    private Fields createNextStepFields(final Fields fields) {
        /*
         * need to deep copy the fields as each locator may have multiple tasks
         * with different datadef and steps
         */
        Fields nextStepFields = null;
        try {
            nextStepFields = fieldsHelper.deepCopy(fields);
        } catch (FieldsException e) {
            String message = "unable to clone next step fields";
            LOGGER.error("{} {}", message, e.getLocalizedMessage());
            throw new StepRunException(message, e);
        }

        try {
            fieldsHelper.addElement("locatorName", locator.getName(),
                    nextStepFields);
            fieldsHelper.addElement("locatorGroup", locator.getGroup(),
                    nextStepFields);
            fieldsHelper.addElement("locatorUrl", locator.getUrl(),
                    nextStepFields);
        } catch (FieldsException e) {
            throw new StepRunException("unable to create next step fields", e);
        }
        return nextStepFields;
    }

    /**
     * <p>
     * Returns whether step is consistent.
     * @return true if document is loaded
     * @see org.codetab.gotz.step.Step#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && isDocumentLoaded());
    }

    /**
     * <p>
     * Sets field if input is instance of Locator.
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

    /**
     * <p>
     * Returns whether document is loaded.
     * @return true if document is not null
     */
    public boolean isDocumentLoaded() {
        return Objects.nonNull(document);
    }

    /**
     * <p>
     * Fetch document from web or file system. Template method to be implemented
     * by subclass.
     * @param url
     *            to fetch
     * @return document contents as byte array
     * @throws IOException
     *             on error
     */
    public abstract byte[] fetchDocumentObject(String url) throws IOException;
}
