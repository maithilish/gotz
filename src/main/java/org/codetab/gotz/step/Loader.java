package org.codetab.gotz.step;

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
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public abstract class Loader extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

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
        marker = MarkerUtil.getMarker(locator.getName(), locator.getGroup());
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
            LOGGER.debug("{} : {}", "using locator read from file : ", getLabel());
        } else {
            // update existing locator with new values
            savedLocator.getFields().addAll(locator.getFields());
            savedLocator.setUrl(locator.getUrl());
            // switch locator to persisted locator (detached locator)
            locator = savedLocator;
            LOGGER.debug("{} : {}", "using locator loaded from datastore : ", getLabel());
            LOGGER.trace(marker, "-- Locator loaded --{}{}", Util.LINE, locator);
        }
        setStepState(StepState.LOAD);
        return true;
    }

    @Override
    public boolean process() {
        Long activeDocumentId = null;
        if (locator.getId() != null) {
            activeDocumentId = documentHelper.getActiveDocumentId(locator.getDocuments());
        }
        if (activeDocumentId == null) {
            Object object = null;
            try {
                object = fetchDocument(locator.getUrl());
            } catch (IOException e) {
                String givenUpMessage = "unable to fetch document";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
            Date fromDate = configService.getRunDateTime();

            Date toDate = documentHelper.getToDate(fromDate, locator.getFields(),
                    getLabel());
            document = dInjector.instance(Document.class);
            document.setName(locator.getName());
            document.setDocumentObject(object);
            document.setFromDate(fromDate);
            document.setToDate(toDate);
            document.setUrl(locator.getUrl());
            locator.getDocuments().add(document);
            setConsistent(true);
            LOGGER.info("create new document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(), document.getToDate());
            LOGGER.trace("create new document {}", document);
        } else {
            document = documentPersistence.loadDocument(activeDocumentId);
            setConsistent(true);
            LOGGER.info("use stored document. Locator[name={} group={} toDate={}]",
                    locator.getName(), locator.getGroup(), document.getToDate());
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
        boolean persist = OFieldsUtil.isPersist(locator.getFields(), "document");
        if (persist) {
            /*
             * fields are not persistable, so need to set them from the fields.xml every
             * time
             */
            try {
                List<FieldsBase> fields = locator.getFields();
                locatorPersistence.storeLocator(locator);
                // reload locator and document
                locator = locatorPersistence.loadLocator(locator.getId());
                locator.getFields().addAll(fields);
                document = documentPersistence.loadDocument(document.getId());
                LOGGER.debug("Stored {}", locator);
                LOGGER.trace(marker, "-- Locator stored --{}{}", Util.LINE, locator);
            } catch (RuntimeException e) {
                String givenUpMessage = "unable to store";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
        } else {
            LOGGER.debug("Persist [false]. Not Stored {}", locator);
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

        //List<FieldsBase> stepsFields = null;
        List<FieldsBase> dataDefFields = null;
        try {
            //stepsFields = OFieldsUtil.getGroupFields(locator.getFields(), "steps");
            dataDefFields = FieldsUtil.filterByGroup(locator.getFields(), "datadef");
        } catch (FieldNotFoundException e) {
            LOGGER.error("{} {}", givenUpMessage, e);
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
            throw new StepRunException(givenUpMessage, e);
        }
        for (FieldsBase dataDefField : dataDefFields) {
            if (dataDefField instanceof Fields) {
                Fields dataDefFieldCopy = null;
                try {
                    dataDefFieldCopy = Util.deepClone(Fields.class,
                            (Fields) dataDefField);
                } catch (ClassNotFoundException | IOException e) {
                    LOGGER.error("{} {}", "unable to clone fields",
                            e.getLocalizedMessage());
                    continue;
                }
                if (isDocumentLoaded()) {
                    List<FieldsBase> nextStepFields = new ArrayList<>();
                    nextStepFields.add(dataDefFieldCopy);
                    //nextStepFields.addAll(stepsFields);
                    nextStepFields.add(
                            OFieldsUtil.createField("locatorName", locator.getName()));
                    nextStepFields.add(
                            OFieldsUtil.createField("locatorGroup", locator.getGroup()));
                    nextStepFields
                    .add(OFieldsUtil.createField("locatorUrl", locator.getUrl()));
                    stepService.pushTask(this, document, nextStepFields);
                } else {
                    LOGGER.warn("Document not loaded - Locator [{}]", locator);
                    activityService.addActivity(Type.GIVENUP,
                            "Document not loaded. " + givenUpMessage);
                }
            }
        }
        setStepState(StepState.HANDOVER);
        return true;
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
    public abstract Object fetchDocument(String url) throws IOException;
}
