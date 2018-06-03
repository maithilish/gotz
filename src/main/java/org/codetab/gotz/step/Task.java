package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.metrics.MetricsHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer.Context;

public class Task implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    @Inject
    private ActivityService activityService;
    @Inject
    private MetricsHelper metricsHelper;

    private IStep step;

    public void setStep(final IStep step) {
        this.step = step;
    }

    @Override
    public void run() {
        try {
            Context taskTimer = metricsHelper.getTimer(step, "task");
            Marker marker = step.getMarker();
            String label = getLabel();
            String stepType = step.getStepType();

            step.initialize();

            LOGGER.trace(marker, Messages.getString("Task.0"), //$NON-NLS-1$
                    label, stepType);

            step.load();
            step.process();
            step.store();
            step.handover();

            LOGGER.trace(marker, Messages.getString("Task.1"), //$NON-NLS-1$
                    label, stepType);

            taskTimer.stop();

        } catch (StepRunException | StepPersistenceException e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e); //$NON-NLS-1$
            activityService.addActivity(Type.FAIL, label, message, e);
            countError();
        } catch (Exception e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepType(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepType(), e); //$NON-NLS-1$
            activityService.addActivity(Type.INTERNAL, label, message, e);
            countError();
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

    private void countError() {
        Counter counter = metricsHelper.getCounter(this, "error");
        counter.inc();
    }
}
