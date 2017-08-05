package org.codetab.gotz.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.stepbase.BaseSeeder;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Create seeder tasks and handover them to queue.
 * @author Maithilish
 *
 */

public final class LocatorSeeder extends BaseSeeder {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LocatorSeeder.class);

    /**
     * delay between task submit.
     */
    private static final long SLEEP_MILLIS = 1000;

    /**
     * list of locator don't name the next as locators as it hides field.
     * (checkstyle)
     */
    private List<Locator> locatorList = new ArrayList<>();

    /**
     * helper - provides fields for locators.
     */
    @Inject
    private LocatorFieldsHelper fieldsHelper;

    /**
     * helper - create and manage locators.
     */
    @Inject
    private LocatorHelper locatorHelper;

    /**
     * <p>
     * get instance of this class.
     */
    @Override
    public IStep instance() {
        setStepType("seeder");
        return this;
    }

    /**
     * <p>
     * Creates a list of locator and adds input locator to the list. Normally,
     * LocatorParser uses this method to seed the locator it has parsed.
     * Otherwise, if list is empty, then initialize method obtains list of
     * locators from LocatorHelper.
     *
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Locator) {
            Locator locator = (Locator) input;
            // locatorseeder adds new set of fields based on group, so clear the
            // existing fields
            locator.getFields().clear();
            locatorList = new ArrayList<>();
            locatorList.add(locator);
        }
    }

    /**
     * <p>
     * Initialize list of locators (or forked locators to load test).
     */
    @Override
    public boolean initialize() {
        if (locatorList.size() == 0) {
            locatorList = locatorHelper.getLocatorsFromBeans();
            // fork for load test
            List<Locator> forkedLocators =
                    locatorHelper.forkLocators(locatorList);
            if (forkedLocators.size() > 0) {
                locatorList = forkedLocators;
            }
        }
        logState("locator after init");
        setStepState(StepState.INIT);
        return true;
    }

    /**
     * <p>
     * Adds group fields and label field to locators.
     */
    @Override
    public boolean process() {
        setFields(fieldsHelper.getStepFields());
        for (Locator locator : locatorList) {
            List<FieldsBase> groupFields =
                    fieldsHelper.getLocatorGroupFields(locator.getGroup());
            fieldsHelper.addLabel(locator);
            locator.getFields().addAll(groupFields);
        }
        logState("locator after merging fields");
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    /**
     * <p>
     * Submit tasks to queue.
     */
    @Override
    public boolean handover() {
        LOGGER.info("push locators to taskpool");
        int count = 0;
        for (Locator locator : locatorList) {
            stepService.pushTask(this, locator, locator.getFields());
            count++;
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("locators count [{}], queued to taskpool [{}].",
                locatorList.size(), count);
        setStepState(StepState.HANDOVER);
        return true;
    }

    /**
     * <p>
     * Logs trace with marker.
     * @param message
     *            - message to log {@link String}
     */
    private void logState(final String message) {
        for (Locator locator : locatorList) {
            Marker marker =
                    MarkerUtil.getMarker(locator.getName(), locator.getGroup());
            LOGGER.trace(marker, "-- {} --{}{}", message, Util.LINE, locator);
        }
    }
}
