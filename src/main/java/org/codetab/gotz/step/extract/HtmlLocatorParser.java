package org.codetab.gotz.step.extract;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlLocatorParser extends HtmlParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(HtmlLocatorParser.class);

    @Inject
    private LocatorFieldsHelper locatorXFieldHelper;

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
            } catch (FieldsException e) {
                String givenUpMessage = "unable to create locator";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
            // List<FieldsBase> nextStepFields = createNextStepFields(locator);
            Fields nextStepField = createNextStepFields(locator);
            // List<Locator> locatorList = new ArrayList<>();
            // locatorList.add(locator);
            stepService.pushTask(this, locator, nextStepField);
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    private Locator createLocator(final Member member) throws FieldsException {
        Locator locator = new Locator();
        locator.setName(
                fieldsHelper.getLastValue("//:locatorName", getFields()));
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.buildString(
                    "unable to create new locator. define group for member ",
                    "in datadef of locator type ", member.getName());
            throw new FieldsException(message);
        } else {
            locator.setGroup(member.getGroup());
            Fields fields = locatorXFieldHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            locator.setFields(fields);
            if (member.getFields() != null) {
                locator.getFields().getNodes()
                        .addAll(member.getFields().getNodes());
            }
            locatorXFieldHelper.addLabel(locator);
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private Fields createNextStepFields(final Locator locator) {
        try {
            Fields nextStepXField = locatorXFieldHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            if (nextStepXField.getNodes().size() == 0) {
                throw new FieldsException("no nodes in xfield");
            }
            return nextStepXField;
        } catch (FieldsException e) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
    }

    @Override
    public boolean store() {
        // not required - don't throw illegal operation
        return false;
    }
}
