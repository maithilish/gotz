package org.codetab.gotz.step.base;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        Locator savedLocator = null;

        // load locator from db
        savedLocator = locatorPersistence.loadLocator(locator.getName(),
                locator.getGroup());

        if (savedLocator == null) {
            // use the locator passed as input to this step
            LOGGER.debug("{} {}", getLabel(), "using locator read from file");
        } else {
            // update existing locator with new fields and URL
            savedLocator.setFields(locator.getFields());
            savedLocator.setUrl(locator.getUrl());

            // switch locator to persisted locator (detached locator)
            locator = savedLocator;

            LOGGER.debug("{} {}", getLabel(),
                    "using locator loaded from datastore");
            LOGGER.trace(getMarker(), "-- Locator loaded --{}{}", Util.LINE,
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
                String message = "unable to fetch document ";
                throw new StepRunException(message, e);
            }

            // document metadata
            Date fromDate = configService.getRunDateTime();
            Date toDate = documentHelper.getToDate(fromDate,
                    locator.getFields(), getLabels());

            // create new document
            document = documentHelper.createDocument(locator.getName(),
                    locator.getUrl(), fromDate, toDate);

            // set documentObject (helper method compress the bytes before set)
            try {
                documentHelper.setDocumentObject(document, documentObject);
            } catch (IOException e) {
                String message = "unable to compress document object";
                throw new StepRunException(message, e);
            }

            // add document to locator
            locator.getDocuments().add(document);

            setConsistent(true);

            LOGGER.info("{} create new document, toDate={}", getLabel(),
                    document.getToDate());
            LOGGER.trace(getMarker(), "create new document {}", document);
        } else {
            // load the existing active document
            document = documentPersistence.loadDocument(activeDocumentId);
            setConsistent(true);
            LOGGER.info("{} use stored document, toDate={}", getLabel(),
                    document.getToDate());

            LOGGER.trace(getMarker(), "found document {}", document);
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

        /*
         * fields are not persistable, so need to set them from the fields.xml
         * every time
         */
        try {
            Fields fields = locator.getFields();

            // store locator
            if (locatorPersistence.storeLocator(locator)) {
                // if stored then reload locator and document
                Locator tLocator =
                        locatorPersistence.loadLocator(locator.getId());
                if (tLocator != null) {
                    locator = tLocator;
                    locator.setFields(fields);
                }

                Document tDocument =
                        documentPersistence.loadDocument(document.getId());
                if (tDocument != null) {
                    document = tDocument;
                }

                LOGGER.debug("{} stored locator", getLabel());
                LOGGER.trace(getMarker(), "-- Locator stored --{}{}", Util.LINE,
                        locator);
            }

        } catch (RuntimeException e) {
            String message = "unable to store";
            throw new StepRunException(message, e);
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

        String message = getLabeled("unable to create parser");

        List<Fields> tasks = null;
        try {
            tasks = fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                    locator.getFields());
            if (tasks.size() == 0) {
                throw new FieldsException("no task defined");
            }
        } catch (FieldsException e) {
            throw new StepRunException(message, e);
        }

        for (Fields task : tasks) {
            if (isDocumentLoaded()) {
                try {
                    Fields nextStepFields = createNextStepFields(task);
                    stepService.pushTask(this, document, getLabels(),
                            nextStepFields);
                } catch (RuntimeException e) {
                    message = Util.join(message,
                            " as get next step fields failed");
                    LOGGER.error("{}", message);
                    activityService.addActivity(Type.FAIL, message);
                }
            } else {
                message = Util.join(message, " as document not loaded");
                LOGGER.error("{}", message);
                activityService.addActivity(Type.FAIL, message);
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
        try {
            Fields nextStepFields = fieldsHelper.deepCopy(fields);
            return nextStepFields;
        } catch (FieldsException e) {
            String message = "unable to clone next step fields";
            throw new StepRunException(message, e);
        }

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
            String message = Util.join(
                    "next step input : required [Locator], but is instance of ",
                    input.getClass().getName());
            throw new StepRunException(message);
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
