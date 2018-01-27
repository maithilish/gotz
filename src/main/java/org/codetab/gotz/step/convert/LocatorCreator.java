package org.codetab.gotz.step.convert;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.helper.ThreadSleep;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseConverter;
import org.codetab.gotz.util.MarkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
    @Inject
    private ThreadSleep threadSleep;

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
                locator = locatorHelper.createLocator(member,
                        getLabels().getName(), getLabel());
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(getMarker(),
                            Messages.getString("LocatorCreator.2"), //$NON-NLS-1$
                            member);
                    // marker and trace for new locator
                    Marker marker = MarkerUtil.getMarker(locator.getName(),
                            locator.getGroup());
                    LOGGER.trace(marker, Messages.getString("LocatorCreator.3"), //$NON-NLS-1$
                            locator, locator.getUrl());
                }
                setConvertedData(locator);
                setConsistent(true);
            } catch (FieldsException | InvalidDataDefException e) {
                String message = Messages.getString("LocatorCreator.0"); //$NON-NLS-1$
                throw new StepRunException(message, e);
            }

            Fields nextStepField = createNextStepFields(locator);
            Labels labels = locatorHelper.createLabels(locator);
            stepService.pushTask(this, locator, labels, nextStepField);
            threadSleep.sleep(sleepMillis);
        }
        return true;
    }

    private Fields createNextStepFields(final Locator locator) {
        try {
            Fields nextStepFields = locatorFieldsHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            if (nextStepFields.getNodes().size() == 0) {
                throw new FieldsException(
                        Messages.getString("LocatorCreator.4")); //$NON-NLS-1$
            }
            return nextStepFields;
        } catch (FieldsException e) {
            String message = Messages.getString("LocatorCreator.5"); //$NON-NLS-1$
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
