package org.codetab.gotz.steps;

import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlLocatorParser extends JSoupHtmlParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(JSoupHtmlLocatorParser.class);

    @Inject
    private LocatorFieldsHelper fieldsHelper;

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
            // List<Locator> locatorList = new ArrayList<>();
            // locatorList.add(locator);
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
            locator.setGroup(member.getGroup());
            List<FieldsBase> groupFields =
                    fieldsHelper.getLocatorGroupFields(locator.getGroup());
            locator.getFields().addAll(groupFields);
            if (member.getFields() != null) {
                locator.getFields().addAll(member.getFields());
            }
            fieldsHelper.addLabel(locator);
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private List<FieldsBase> createNextStepFields(final Locator locator) {
        List<FieldsBase> nextStepFields =
                fieldsHelper.getLocatorGroupFields(locator.getGroup());
        if (nextStepFields.size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepFields;
    }

    @Override
    public boolean store() {
        // not required - don't throw illegal oper
        return false;
    }
}
