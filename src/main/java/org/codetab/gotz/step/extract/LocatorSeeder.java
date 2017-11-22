package org.codetab.gotz.step.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseSeeder;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Creates seeder tasks and handover them to queue.
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
    private LocatorFieldsHelper locatorXFieldHelper;

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
            // TODO check whether clear is required for xfield
            // locator.getFields().clear();
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
        for (Locator locator : locatorList) {
            try {
                XField xField = locatorXFieldHelper.getXField(
                        locator.getClass().getName(), locator.getGroup());
                locator.setXField(xField);
                locatorXFieldHelper.addLabel(locator);
            } catch (FieldsException e) {
                throw new StepRunException(
                        "unable to set xfield copy to locator", e);
            }
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
            // create new instance of seeder step for each locator
            Step seederStep = getSeederStep(locator);
            stepService.pushTask(seederStep, locator, locator.getXField());
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
     * Creates new instance of this type and set it fields from locator and this
     * step. This step holds multiple locators, we can't pass its fields to next
     * step. Hence create new step just to hold the step fields.
     * @param locator
     * @return step
     */
    private Step getSeederStep(final Locator locator) {
        try {
            Step seederStep =
                    (Step) stepService.getStep(this.getClass().getName());
            seederStep.setConsistent(true);
            seederStep.setXField(locator.getXField());
            seederStep.setStepType(this.getStepType());
            seederStep.setStepState(this.getStepState());
            return seederStep;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            throw new StepRunException("unable spawn seeder", e);
        }
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
