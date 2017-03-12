package org.codetab.gotz.ext;

import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Seeder;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocatorSeeder extends Seeder {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorSeeder.class);

    private static final long SLEEP_MILLIS = 1000;

    private List<Locator> locators = new ArrayList<>();

    @Override
    public IStep instance() {
        Step step = new LocatorSeeder();
        step.setStepType("seeder");
        return step;
    }

    @Override
    public void load() {
        initLocators();
        List<FieldsBase> fields = BeanService.INSTANCE.getBeans(FieldsBase.class);
        try {
            FieldsBase classFields = FieldsUtil.getFieldsByValue(fields, "class",
                    Locator.class.getName());
            List<FieldsBase> steps = FieldsUtil.getGroupFields(classFields, "steps");
            setFields(steps);
            if (classFields != null) {
                mergeFields(classFields);
            }
        } catch (FieldNotFoundException e) {
        }
    }

    private void initLocators() {
        LOGGER.info("initialize locators");
        List<Locators> list = BeanService.INSTANCE.getBeans(Locators.class);
        for (Locators locators : list) {
            trikleGroup(locators);
        }
        for (Locators locators : list) {
            extractLocator(locators);
        }
        for (Locator locator : locators) {
            addLabelField(locator);
        }
        for (Locator locator : locators) {
            Util.logState(LOGGER, "locator", "initialized locator", locator.getFields(),
                    locator);
        }
    }

    private void addLabelField(final Locator locator) {
        String label = Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = FieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    private void extractLocator(final Locators locators) {
        for (Locators locs : locators.getLocators()) {
            extractLocator(locs);
        }
        for (Locator locator : locators.getLocator()) {
            this.locators.add(locator);
        }
    }

    private void trikleGroup(final Locators locators) {
        LOGGER.info("cascade locators group to all locator");
        for (Locators locs : locators.getLocators()) {
            trikleGroup(locs);
        }
        for (Locator locator : locators.getLocator()) {
            if (locator.getGroup() == null) {
                locator.setGroup(locators.getGroup());
            }
        }
    }

    @Override
    public void handover() {
        LOGGER.info("push locators to loader taskpool");
        int count = 0;
        for (Locator locator : locators) {
            pushTask(locator, locator.getFields());
            count++;
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("locators count [{}], queued to loader [{}].", locators.size(),
                count);
    }

    private void mergeFields(final FieldsBase classFields) {
        LOGGER.info("merge fields with locators");
        for (Locator locator : locators) {
            try {
                List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields,
                        locator.getGroup());
                locator.getFields().addAll(fields);

                List<FieldsBase> steps = FieldsUtil.getGroupFields(classFields, "steps");
                locator.getFields().addAll(steps);
            } catch (FieldNotFoundException e) {
            }
        }
        for (Locator locator : locators) {
            Util.logState(LOGGER, "locator", "after merging fields", locator.getFields(),
                    locator);
        }
    }

    @Override
    public void store() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInput(final Object input) {
        throw new UnsupportedOperationException();
    }

}
