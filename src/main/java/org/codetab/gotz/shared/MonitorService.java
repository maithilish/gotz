package org.codetab.gotz.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class MonitorService {

    private final Logger logger = LoggerFactory.getLogger(MonitorService.class);

    private List<Activity> activitesList;

    private Map<String, Long> memoryHighs = new HashMap<>();
    private Map<String, Long> memoryLows = new HashMap<>();

    private Timer timer;
    private StopWatch stopWatch;
    private MemoryTask memoryTask;
    private ConfigService configService;

    @Inject
    private MonitorService() {
        activitesList = new ArrayList<Activity>();
    }

    public void start() {
        stopWatch.start();

        final long memoryPollFrequency = 5000;
        timer.schedule(memoryTask, 0, memoryPollFrequency);
    }

    public void triggerFatal(final String message) {
        activitesList.add(new Activity(Type.FATAL, message));
        end();
        logger.info("Gotz Terminated");
        if (configService.isTestMode()) {
            throw new IllegalStateException("fatal expection triggered");
        } else {
            System.exit(1);
        }
    }

    public void addActivity(final Type type, final String message) {
        activitesList.add(new Activity(type, message));
    }

    public void addActivity(final Type type, final String message,
            final Throwable throwable) {
        activitesList.add(new Activity(type, message, throwable));
    }

    public void end() {
        logger.info("{}", "Gotz run completed");
        timer.cancel();
        logActivities();
        logMemoryUsage();
        stopWatch.stop();
        logger.info("{}  {}", "Total time:", stopWatch);
    }

    private void logMemoryUsage() {
        logger.info("{}", "--- Memory Usage ---");
        logger.info("Highs: {}", memoryUsage(memoryHighs));
        logger.info("Lows: {}", memoryUsage(memoryLows));
    }

    private String memoryUsage(final Map<String, Long> map) {
        final long divisor = 1024 * 1024;
        StringBuilder sb = new StringBuilder();
        for (String name : map.keySet()) {
            Long inMB = (map.get(name)) / divisor;
            sb.append(name);
            sb.append(" : ");
            sb.append(inMB);
            sb.append("M   ");
        }
        return sb.toString();
    }

    private void logActivities() {
        logger.info("{}", "--- Summary ---");
        if (activitesList.size() == 0) {
            logger.info("no issues");
        }
        for (Activity activity : activitesList) {
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
    }

    public void pollMemory(final Long maxMemory, final Long totalMemory,
            final Long freeMemory) {
        setHighs("Total", totalMemory);
        setHighs("Maximum", maxMemory);
        setHighs("Free", freeMemory);
        setLows("Total", totalMemory);
        setLows("Maximum", maxMemory);
        setLows("Free", freeMemory);
    }

    private void setHighs(final String name, final long value) {
        Long previousValue = memoryHighs.get(name);
        if (previousValue == null) {
            memoryHighs.put(name, value);
        } else {
            if (value > previousValue) {
                memoryHighs.put(name, value);
            }
        }
    }

    private void setLows(final String name, final long value) {
        Long previousValue = memoryLows.get(name);
        if (previousValue == null) {
            memoryLows.put(name, value);
        } else {
            if (value < previousValue) {
                memoryLows.put(name, value);
            }
        }
    }

    @Inject
    public void setMemoryTask(MemoryTask memoryTask){
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
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
