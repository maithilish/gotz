package org.codetab.gotz.shared;

import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.misc.MemoryTask;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.model.Activity.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ActivityService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ActivityService.class);

    private static final long MB_DIVISOR = 1048576; // mega bytes

    @Inject
    private List<Activity> activities;
    @Inject
    private Timer timer;
    @Inject
    private StopWatch stopWatch;
    @Inject
    private MemoryTask memoryTask;
    @Inject
    private Runtime runtime;
    @Inject
    private LongSummaryStatistics totalMemory;
    @Inject
    private LongSummaryStatistics freeMemory;

    @Inject
    private ActivityService() {
    }

    public void start() {
        stopWatch.start();

        final long memoryPollFrequency = 5000;
        timer.schedule(memoryTask, 0, memoryPollFrequency);
    }

    public void end() {
        timer.cancel();
        stopWatch.stop();
    }

    // TODO remove next two methods and also similar constructor in activity
    // class
    public void addActivity(final Type type, final String message) {
        activities.add(new Activity(type, message));
    }

    public void addActivity(final Type type, final String message,
            final Throwable throwable) {
        activities.add(new Activity(type, message, throwable));
    }

    public void addActivity(final Type type, final String label,
            final String message) {
        activities.add(new Activity(type, label, message));
    }

    public void addActivity(final Type type, final String label,
            final String message, final Throwable throwable) {
        activities.add(new Activity(type, label, message, throwable));
    }

    public void logActivities() {
        LOGGER.info("{}", Messages.getString("ActivityService.0")); //$NON-NLS-1$ //$NON-NLS-2$
        if (activities.size() == 0) {
            LOGGER.info(Messages.getString("ActivityService.2")); //$NON-NLS-1$
        }
        for (Activity activity : activities) {
            Throwable throwable = activity.getThrowable();
            String throwableClass = ""; //$NON-NLS-1$
            String throwableMessage = ""; //$NON-NLS-1$
            String causeClass = ""; //$NON-NLS-1$
            String causeMessage = ""; //$NON-NLS-1$
            Throwable cause = null;
            if (throwable != null) {
                throwableClass = throwable.getClass().getSimpleName();
                throwableMessage = throwable.getLocalizedMessage();
                cause = throwable.getCause();
                if (cause != null) {
                    causeClass = cause.getClass().getSimpleName();
                    causeMessage = cause.getLocalizedMessage();
                }
            }
            LOGGER.info(Messages.getString("ActivityService.7"), //$NON-NLS-1$
                    activity.getType(), activity.getLabel(),
                    activity.getMessage());
            LOGGER.info(Messages.getString("ActivityService.8"), throwableClass, //$NON-NLS-1$
                    throwableMessage);
            if (cause != null) {
                LOGGER.info(Messages.getString("ActivityService.9"), causeClass, //$NON-NLS-1$
                        causeMessage);
            }

        }
        LOGGER.info("{}  {}", Messages.getString("ActivityService.11"), //$NON-NLS-1$ //$NON-NLS-2$
                stopWatch);
    }

    public void collectMemoryStat() {
        long mm = runtime.maxMemory() / MB_DIVISOR;
        long fm = runtime.freeMemory() / MB_DIVISOR;
        long tm = runtime.totalMemory() / MB_DIVISOR;
        LOGGER.debug("{} {} {} {}", new Date(), mm, tm, fm); //$NON-NLS-1$

        freeMemory.accept(fm);
        totalMemory.accept(tm);
    }

    public void logMemoryUsage() {
        LOGGER.info("{}", Messages.getString("ActivityService.14")); //$NON-NLS-1$ //$NON-NLS-2$
        LOGGER.info(Messages.getString("ActivityService.15"), //$NON-NLS-1$
                runtime.maxMemory() / MB_DIVISOR);
        LOGGER.info(Messages.getString("ActivityService.16"), //$NON-NLS-1$
                (long) totalMemory.getAverage(), totalMemory.getMax(),
                totalMemory.getMin());
        LOGGER.info(Messages.getString("ActivityService.17"), //$NON-NLS-1$
                (long) freeMemory.getAverage(), freeMemory.getMax(),
                freeMemory.getMin());
    }
}
