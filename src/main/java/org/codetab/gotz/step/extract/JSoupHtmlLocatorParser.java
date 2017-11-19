package org.codetab.gotz.step.extract;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.LocatorXFieldHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlLocatorParser extends JSoupHtmlParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(JSoupHtmlLocatorParser.class);

    @Inject
    private LocatorXFieldHelper locatorXFieldHelper;

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
            } catch (XFieldException e) {
                String givenUpMessage = "unable to create locator";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }
            XField nextStepField = createNextStepFields(locator);
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

    private Locator createLocator(final Member member) throws XFieldException {
        Locator locator = new Locator();
        locator.setName(
                xFieldHelper.getLastValue("//:locatorName", getXField()));
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.buildString(
                    "unable to create new locator. define group for member ",
                    "in datadef of locator type ", member.getName());
            throw new XFieldException(message);
        } else {
            locator.setGroup(member.getGroup());
            XField xField = locatorXFieldHelper.getXField(
                    locator.getClass().getName(), locator.getGroup());
            locator.setXField(xField);
            if (member.getXField() != null) {
                locator.getXField().getNodes()
                        .addAll(member.getXField().getNodes());
            }
            locatorXFieldHelper.addLabel(locator);
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private XField createNextStepFields(final Locator locator) {
        try {
            XField nextStepXField = locatorXFieldHelper.getXField(
                    locator.getClass().getName(), locator.getGroup());
            if (nextStepXField.getNodes().size() == 0) {
                throw new XFieldException("no nodes in xfield");
            }
            return nextStepXField;
        } catch (XFieldException e) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
    }

    @Override
    public boolean store() {
        // not required - don't throw illegal oper
        return false;
    }
}
