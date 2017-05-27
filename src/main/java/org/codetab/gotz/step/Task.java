package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.shared.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private IStep step;

    @Inject
    private ActivityService activityService;

    public void setStep(IStep step) {
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
            LOGGER.warn("{}", e.getLocalizedMessage());
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, "unable to seed", e);
        }
    }
}
