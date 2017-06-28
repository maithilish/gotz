package org.codetab.gotz.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Seeder;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LocatorSeeder extends Seeder {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorSeeder.class);

    private static final long SLEEP_MILLIS = 1000;

    private List<Locator> locators = new ArrayList<>();

    @Inject
    private BeanService beanService;

    @Override
    public IStep instance() {
        setStepType("seeder");
        return this;
    }

    @Override
    public boolean initialize() {
        return false;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean process() {
        initLocators();
        logState("locator after init");
        initFields();
        logState("locator after merging fields");
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    @Override
    public boolean store() {
        return false;
    }

    @Override
    public boolean handover() {
        LOGGER.info("push locators to loader taskpool");
        int count = 0;
        for (Locator locator : locators) {
            stepService.pushTask(this, locator, locator.getFields());
            count++;
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("locators count [{}], queued to loader [{}].",
                locators.size(), count);
        setStepState(StepState.HANDOVER);
        return true;
    }

    @Override
    public void setInput(final Object input) {
        LOGGER.warn("unsupported operation");
    }

    private void initLocators() {
        LOGGER.info("initialize locators");
        List<Locators> list = beanService.getBeans(Locators.class);
        for (Locators locators : list) {
            trikleGroup(locators);
        }
        for (Locators locators : list) {
            extractLocator(locators);
        }
        for (Locator locator : locators) {
            addLabelField(locator);
        }
    }

    private void initFields() {
        List<FieldsBase> fields = beanService.getBeans(FieldsBase.class);
        try {
            List<FieldsBase> classFields = FieldsUtil.filterByValue(fields,
                    "class", Locator.class.getName());
            List<FieldsBase> stepsGroup =
                    OFieldsUtil.getGroupFields(classFields, "steps");
            List<FieldsBase> stepFields =
                    FieldsUtil.filterByName(stepsGroup, "step");
            setFields(stepFields);
            mergeStepFields(classFields, stepFields);
            mergeFields(classFields);
        } catch (FieldNotFoundException e) {
        }
    }

    private void addLabelField(final Locator locator) {
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = OFieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    private void extractLocator(final Locators locatorsList) {
        for (Locators locs : locatorsList.getLocators()) {
            extractLocator(locs);
        }
        for (Locator locator : locatorsList.getLocator()) {
            locators.add(locator);
        }
    }

    private void trikleGroup(final Locators locators) {
        LOGGER.info("cascade locators group to all locator");
        for (Locators locs : locators.getLocators()) {
            if (locs.getGroup() == null) {
                locs.setGroup(locators.getGroup());
            }
            trikleGroup(locs);
        }
        for (Locator locator : locators.getLocator()) {
            if (locator.getGroup() == null) {
                locator.setGroup(locators.getGroup());
            }
        }
    }

    private void mergeFields(final List<FieldsBase> classFields) {
        LOGGER.info("merge fields with locators");
        for (Locator locator : locators) {
            try {
                List<Fields> groupFields = FieldsUtil
                        .filterByGroupAsFields(classFields, locator.getGroup());
                locator.getFields().addAll(groupFields);
            } catch (FieldNotFoundException e) {
            }
        }
    }

    private void mergeStepFields(final List<FieldsBase> classFields,
            final List<FieldsBase> stepFields) {
        LOGGER.info("merge step fields with datadef fields");
        try {
            List<Fields> dataDefGroup =
                    FieldsUtil.filterByGroupAsFields(classFields, "datadef");
            for (Fields dataDefFields : dataDefGroup) {
                for (FieldsBase step : stepFields) {
                    if (!FieldsUtil.contains(dataDefFields, step.getName(),
                            step.getValue())) {
                        dataDefFields.getFields().add(step);
                    }
                }
            }
        } catch (FieldNotFoundException e) {
            LOGGER.warn(
                    "unable to find datadef fields in class fields, check fields xml file");
        }
    }

    private void logState(String message) {
        for (Locator locator : locators) {
            Marker marker =
                    MarkerUtil.getMarker(locator.getName(), locator.getGroup());
            LOGGER.trace(marker, "-- {} --{}{}", message, Util.LINE, locator);
        }
    }
}
