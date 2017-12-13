package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.exception.StepRunException;
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
            step.load();
            step.process();
            step.store();
            step.handover();
        } catch (StepRunException | StepPersistenceException e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage());
            LOGGER.error("[{}] {}", label, message);
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e);
            activityService.addActivity(Type.FAIL, label, message, e);
        } catch (Exception e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage());
            LOGGER.error("[{}] {}", label, message);
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e);
            activityService.addActivity(Type.INTERNAL, label, message, e);
        }
    }

    private String getLabel() {
        Labels labels = step.getLabels();
        // internal error
        String label = "step labels not set";
        if (labels != null) {
            label = labels.getLabel();
        }
        return label;
    }
}
