package org.codetab.gotz.ext;

import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlLocatorParser extends HtmlParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(HtmlLocatorParser.class);

    private BeanService beanService;

    @Inject
    void setBeanService(final BeanService beanService) {
        this.beanService = beanService;
    }

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean handover() {
        final int sleepMillis = 1000;
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
            // String stepType = getStepType();
            // setStepType("seeder");
            stepService.pushTask(this, locator, locator.getFields());
            // setStepType(stepType);
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
        locator.setName(OFieldsUtil.getValue(getFields(), "locatorName"));
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message =
                    Util.buildString("unable to create new locator. define ",
                            "group for member in datadef of locator type ",
                            member.getName());
            throw new FieldNotFoundException(message);
        } else {
            locator.setGroup(member.getGroup());
            List<FieldsBase> groupFields = getGroupFields(locator.getGroup());
            locator.getFields().addAll(groupFields);
            List<FieldsBase> stepFields = getGroupFields("steps");
            locator.getFields().addAll(stepFields);
            if (member.getFields() != null) {
                locator.getFields().addAll(member.getFields());
            }
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private List<FieldsBase> getGroupFields(final String group)
            throws FieldNotFoundException {
        List<FieldsBase> fieldsBeans = beanService.getBeans(FieldsBase.class);
        FieldsBase classFields = OFieldsUtil.getFieldsByValue(fieldsBeans,
                "class", Locator.class.getName());
        if (classFields != null) {
            List<FieldsBase> fields =
                    OFieldsUtil.getGroupFields(classFields, group);
            return fields;
        }
        return null;
    }

    @Override
    public boolean store() {
        // not required - don't throw illegal operation
        return false;
    }
}
