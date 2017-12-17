package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private IStep step;

    @Inject
    private ActivityService activityService;

    public void setStep(final IStep step) {
        this.step = step;
    }

    @Override
    public void run() {
        try {
            step.initialize();

            LOGGER.trace(step.getMarker(), Messages.getString("Task.0"), //$NON-NLS-1$
                    getLabel(), step.getStepType());

            step.load();
            step.process();
            step.store();
            step.handover();

            LOGGER.trace(step.getMarker(), Messages.getString("Task.1"), //$NON-NLS-1$
                    getLabel(), step.getStepType());

        } catch (StepRunException | StepPersistenceException e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e); //$NON-NLS-1$
            activityService.addActivity(Type.FAIL, label, message, e);
        } catch (Exception e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e); //$NON-NLS-1$
            activityService.addActivity(Type.INTERNAL, label, message, e);
        }
    }

    private String getLabel() {
        Labels labels = step.getLabels();
        // internal error
        String label = Messages.getString("Task.10"); //$NON-NLS-1$
        if (labels != null) {
            label = labels.getLabel();
        }
        return label;
    }
}
