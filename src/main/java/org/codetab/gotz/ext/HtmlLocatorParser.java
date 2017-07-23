package org.codetab.gotz.ext;

import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlLocatorParser extends HtmlParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(HtmlLocatorParser.class);

    @Inject
    private BeanService beanService;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean handover() {
        final long sleepMillis = 1000;
        for (Member member : getData().getMembers()) {
            Locator locator = null;
            try {
                locator = createLocator(member);
            } catch (FieldNotFoundException e) {
                String givenUpMessage = "unable to create locator";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }

            List<FieldsBase> nextStepFields = createNextStepFields(locator);
            stepService.pushTask(this, locator, nextStepFields);

            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    private Locator createLocator(final Member member)
            throws FieldNotFoundException {
        Locator locator = new Locator();
        locator.setName(FieldsUtil.getValue(getFields(), "locatorName"));
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.buildString(
                    "unable to create new locator. define group for member ",
                    "in datadef of locator type ", member.getName());
            throw new FieldNotFoundException(message);
        } else {
            /*
             * getGroupFields gets cloned fields from BeanService so that new
             * locator gets fresh set of fields. Ensure that existing fields are
             * not reused here
             */
            List<FieldsBase> stepsGroup = getGroupFields("steps");
            List<FieldsBase> stepFields =
                    FieldsUtil.filterByName(stepsGroup, "step");

            locator.setGroup(member.getGroup());
            List<FieldsBase> groupFields = getGroupFields(locator.getGroup());
            locator.getFields().addAll(groupFields);

            List<Fields> dataDefGroup = FieldsUtil
                    .filterByGroupAsFields(locator.getFields(), "datadef");
            for (Fields dataDefFields : dataDefGroup) {
                for (FieldsBase step : stepFields) {
                    if (!FieldsUtil.contains(dataDefFields, step.getName(),
                            step.getValue())) {
                        dataDefFields.getFields().add(step);
                    }
                }
            }
            if (member.getFields() != null) {
                locator.getFields().addAll(member.getFields());
            }
            // as in locatorSeeder, we add label to new locator
            addLabelField(locator);
        }
        LOGGER.trace(getMarker(), "created new {} {}", locator,
                locator.getUrl());
        return locator;
    }

    private List<FieldsBase> getGroupFields(final String group)
            throws FieldNotFoundException {
        List<FieldsBase> fieldsBeans = beanService.getBeans(FieldsBase.class);
        List<FieldsBase> classFields = FieldsUtil.filterByValue(fieldsBeans,
                "class", Locator.class.getName());
        if (classFields != null) {
            List<FieldsBase> fields =
                    FieldsUtil.filterByGroup(classFields, group);
            return fields;
        }
        return null;
    }

    private List<FieldsBase> createNextStepFields(final Locator locator) {
        /*
         * createLocator above adds fresh set of fields, so no need for deep
         * copy
         */
        List<FieldsBase> nextStepFields = locator.getFields();
        if (nextStepFields.size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepFields;
    }

    private void addLabelField(final Locator locator) {
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = FieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    @Override
    public boolean store() {
        // not required - don't throw illegal operation
        return false;
    }
}
