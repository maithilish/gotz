package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.shared.ActivityService;
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
        } catch (Exception e) {
            Labels labels = step.getLabels();
            // internal error
            String label = "step labels not set";
            if (labels != null) {
                label = labels.getLabel();
            }
            LOGGER.debug("[{}] {}", label, e);
            LOGGER.error("[{}] {}", label, e.getLocalizedMessage());
            activityService.addActivity(Type.GIVENUP, label, e);
        }
    }
}
