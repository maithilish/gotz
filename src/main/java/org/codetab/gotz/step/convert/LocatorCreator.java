package org.codetab.gotz.step.convert;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseConverter;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Creates new locators from Data.
 * @author Maithilish
 *
 */
public final class LocatorCreator extends BaseConverter {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorCreator.class);

    @Inject
    private LocatorFieldsHelper locatorFieldsHelper;
    @Inject
    private LocatorHelper locatorHelper;

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
                setConvertedData(locator);
                setConsistent(true);
            } catch (FieldsException e) {
                String givenUpMessage = "unable to create locator";
                LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
                activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
                throw new StepRunException(givenUpMessage, e);
            }

            Fields nextStepField = createNextStepFields(locator);
            // List<Locator> locatorList = new ArrayList<>();
            // locatorList.add(locator);
            Labels labels = locatorHelper.createLabels(locator);
            stepService.pushTask(this, locator, labels, nextStepField);
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    private Locator createLocator(final Member member) throws FieldsException {
        Locator locator = new Locator();
        locator.setName(getLabels().getName());
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.buildString(
                    "unable to create new locator. define group for member ",
                    "in datadef of locator type ", member.getName());
            throw new FieldsException(message);
        } else {
            locator.setGroup(member.getGroup());
            Fields fields = locatorFieldsHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            locator.setFields(fields);
            if (member.getFields() != null) {
                locator.getFields().getNodes()
                        .addAll(member.getFields().getNodes());
            }
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private Fields createNextStepFields(final Locator locator) {
        try {
            Fields nextStepFields = locatorFieldsHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            if (nextStepFields.getNodes().size() == 0) {
                throw new FieldsException("no nodes in fields");
            }
            return nextStepFields;
        } catch (FieldsException e) {
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

    @Override
    public boolean process() {
        // not required - don't throw illegal oper
        return false;
    }

}
