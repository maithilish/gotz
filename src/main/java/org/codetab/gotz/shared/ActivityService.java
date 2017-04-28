package org.codetab.gotz.shared;

import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.gotz.misc.MemoryTask;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.model.Activity.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ActivityService {

    private final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private static final long MB_DIVISOR = 1048576; // mega bytes

    private List<Activity> activities;
    private LongSummaryStatistics totalMemory;
    private LongSummaryStatistics freeMemory;
    private Timer timer;
    private StopWatch stopWatch;
    private MemoryTask memoryTask;

    private Runtime runtime;

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

    public void addActivity(final Type type, final String message) {
        activities.add(new Activity(type, message));
    }

    public void addActivity(final Type type, final String message,
            final Throwable throwable) {
        activities.add(new Activity(type, message, throwable));
    }

    public void logActivities() {
        logger.info("{}", "--- Summary ---");
        if (activities.size() == 0) {
            logger.info("no issues");
        }
        for (Activity activity : activities) {
            Throwable throwable = activity.getThrowable();
            String throwableClass = "";
            String throwableMessage = "";
            if (throwable != null) {
                throwableClass = throwable.getClass().getSimpleName();
                throwableMessage = throwable.getLocalizedMessage();
            }
            logger.info("Activity type={}", activity.getType());
            logger.info("         message={}", activity.getMessage());
            logger.info("         {}={}", throwableClass, throwableMessage);
        }
        logger.info("{}  {}", "Total time:", stopWatch);
    }

    public void collectMemoryStat() {
        long mm = runtime.maxMemory() / MB_DIVISOR;
        long fm = runtime.freeMemory() / MB_DIVISOR;
        long tm = runtime.totalMemory() / MB_DIVISOR;
        logger.debug("{} {} {} {}", new Date(), mm, tm, fm);

        freeMemory.accept(fm);
        totalMemory.accept(tm);
    }

    public void logMemoryUsage() {
        logger.info("{}", "--- Memory Usage ---");
        logger.info("Max   : {}", runtime.maxMemory() / MB_DIVISOR);
        logger.info("Total : Avg {} High {} Low {}", (long)totalMemory.getAverage(),
                totalMemory.getMax(), totalMemory.getMin());
        logger.info("Free  : Avg {} High {} Low {}", (long)freeMemory.getAverage(),
                freeMemory.getMax(), freeMemory.getMin());
    }

    @Inject
    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    @Inject
    public void setMemoryTask(MemoryTask memoryTask) {
        this.memoryTask = memoryTask;
    }

    @Inject
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Inject
    public void setStopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Inject
    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    @Inject
    public void setTotalMemory(LongSummaryStatistics totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Inject
    public void setFreeMemory(LongSummaryStatistics freeMemory) {
        this.freeMemory = freeMemory;
    }
}
