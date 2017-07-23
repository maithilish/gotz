package org.codetab.gotz.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.gotz.helper.FieldsHelper;
import org.codetab.gotz.helper.LocatorHelper;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.stepbase.BaseSeeder;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LocatorSeeder extends BaseSeeder {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorSeeder.class);

    private static final long SLEEP_MILLIS = 1000;

    // cs - don't name the next as locators as it hides field
    private List<Locator> locatorList = new ArrayList<>();

    @Inject
    private FieldsHelper fieldsHelper;
    @Inject
    private LocatorHelper locatorHelper;

    @Override
    public IStep instance() {
        setStepType("seeder");
        return this;
    }

    @Override
    public boolean initialize() {
        if (locatorList.size() == 0) {
            locatorList = locatorHelper.getLocatorsFromBeans();
        }
        logState("locator after init");
        setStepState(StepState.INIT);
        return true;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean process() {
        setFields(fieldsHelper.getStepFields());
        for (Locator locator : locatorList) {
            List<FieldsBase> groupFields =
                    fieldsHelper.getLocatorGroupFields(locator.getGroup());
            addLabel(locator);
            locator.getFields().addAll(groupFields);
        }
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
        for (Locator locator : locatorList) {
            stepService.pushTask(this, locator, locator.getFields());
            count++;
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("locators count [{}], queued to loader [{}].",
                locatorList.size(), count);
        setStepState(StepState.HANDOVER);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInput(final Object input) {
        if (input instanceof List) {
            locatorList = (List<Locator>) input;
        }
    }

    private void addLabel(final Locator locator) {
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = FieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    private void logState(final String message) {
        for (Locator locator : locatorList) {
            Marker marker =
                    MarkerUtil.getMarker(locator.getName(), locator.getGroup());
            LOGGER.trace(marker, "-- {} --{}{}", message, Util.LINE, locator);
        }
    }
}
