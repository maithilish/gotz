package org.codetab.gotz.stepbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.helper.DocumentHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public abstract class BaseLoader extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseLoader.class);

    private Locator locator;
    private Document document;
    private Marker marker;

    @Inject
    private LocatorPersistence locatorPersistence;
    @Inject
    private DocumentPersistence documentPersistence;
    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private DInjector dInjector;

    @Override
    public boolean initialize() {
        // TODO write test
        String lName = locator.getName();
        String lGroup = locator.getGroup();
        marker = MarkerUtil.getMarker(lName, lGroup);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#load()
     */
    @Override
    public boolean load() {
        Locator savedLocator = locatorPersistence.loadLocator(locator.getName(),
                locator.getGroup());
        if (savedLocator == null) {
            LOGGER.debug("{} : {}", "using locator read from file : ",
                    getLabel());
        } else {
            // update existing locator with new values
            savedLocator.getFields().addAll(locator.getFields());
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

    @Override
    public boolean process() {
        String locatorLabel = Util.buildString("Locator[name=",
                locator.getName(), ",group=", locator.getGroup(), "]");
        Long activeDocumentId = null;
        if (locator.getId() != null) {
            activeDocumentId =
                    documentHelper.getActiveDocumentId(locator.getDocuments());
        }
        if (activeDocumentId == null) {
            byte[] documentObject = null;
            try {
                // object is byte[]
                documentObject = fetchDocument(locator.getUrl());
            } catch (IOException e) {
                String givenUpMessage = Util
                        .buildString("unable to fetch document ", locatorLabel);
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
            Date fromDate = configService.getRunDateTime();

            Date toDate = documentHelper.getToDate(fromDate,
                    locator.getFields(), getLabel());
            document = dInjector.instance(Document.class);
            document.setName(locator.getName());
            document.setFromDate(fromDate);
            document.setToDate(toDate);
            document.setUrl(locator.getUrl());

            try {
                documentHelper.setDocumentObject(document, documentObject);
            } catch (IOException e) {
                String givenUpMessage = Util.buildString(
                        "unable to compress document object ", locatorLabel);
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
            locator.getDocuments().add(document);
            setConsistent(true);
            LOGGER.info(
                    "create new document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(),
                    document.getToDate());
            LOGGER.trace("create new document {}", document);
        } else {
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

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#store()
     */
    @Override
    public boolean store() {
        boolean persist = true;
        try {
            persist = FieldsUtil.isTrue(locator.getFields(), "persist",
                    "document");
        } catch (FieldNotFoundException e) {
        }
        if (persist) {
            /*
             * fields are not persistable, so need to set them from the
             * fields.xml every time
             */
            try {
                List<FieldsBase> fields = locator.getFields();
                locatorPersistence.storeLocator(locator);
                // reload locator and document
                locator = locatorPersistence.loadLocator(locator.getId());
                locator.getFields().addAll(fields);
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

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#handover()
     */
    @Override
    public boolean handover() {
        // TODO test separate instance for each call
        // for each dataDef create dedicated parser

        String givenUpMessage = Util.buildString("create parser for locator [",
                locator.getName(), "] failed.");

        List<FieldsBase> dataDefFields = null;
        try {

            dataDefFields =
                    FieldsUtil.filterByGroup(locator.getFields(), "datadef");
        } catch (FieldNotFoundException e) {
            LOGGER.error("{} {}", givenUpMessage, e);
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
            throw new StepRunException(givenUpMessage, e);
        }
        for (FieldsBase dataDefField : dataDefFields) {
            if (isDocumentLoaded()) {
                List<FieldsBase> nextStepFields =
                        createNextStepFields(dataDefField);
                if (nextStepFields.size() == 0) {
                    String message = "unable to get next step fields";
                    LOGGER.error("{} {}", message, locator);
                    activityService.addActivity(Type.GIVENUP,
                            Util.buildString(givenUpMessage, " : ", message));
                    continue;
                }
                stepService.pushTask(this, document, nextStepFields);
            } else {
                String message = "document not loaded";
                LOGGER.error("{} {}", message, locator);
                activityService.addActivity(Type.GIVENUP,
                        Util.buildString(givenUpMessage, " : ", message));
            }
        }
        setStepState(StepState.HANDOVER);
        return true;
    }

    private List<FieldsBase> createNextStepFields(
            final FieldsBase dataDefField) {
        /*
         * need to deep copy the fields as each locator may have multiple
         * datadefs and parsers
         */
        List<FieldsBase> nextStepFields = new ArrayList<>();
        FieldsBase dataDefFieldCopy = null;
        try {
            dataDefFieldCopy = FieldsUtil.deepClone(dataDefField);
        } catch (RuntimeException e) {
            LOGGER.error("{} {}", "unable to clone datadef fields",
                    e.getLocalizedMessage());
            return nextStepFields;
        }
        nextStepFields.add(dataDefFieldCopy);
        // we add info fields here, as parser also push locator to loader
        nextStepFields
                .add(FieldsUtil.createField("locatorName", locator.getName()));
        nextStepFields.add(
                FieldsUtil.createField("locatorGroup", locator.getGroup()));
        nextStepFields
                .add(FieldsUtil.createField("locatorUrl", locator.getUrl()));
        return nextStepFields;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.StepO#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && isDocumentLoaded());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#setInput(java.lang.Object)
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

    /*
     *
     */
    public boolean isDocumentLoaded() {
        return Objects.nonNull(document);
    }

    // template method to be implemented by subclass
    public abstract byte[] fetchDocument(String url) throws IOException;
}
