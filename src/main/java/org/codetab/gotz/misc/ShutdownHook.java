package org.codetab.gotz.misc;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.shared.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ShutdownHook extends Thread {

    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private ActivityService activityService;

    @Inject
    private ShutdownHook() {
        logger.info("shutdownhook created");
    }

    @Override
    public synchronized void start() {
        activityService.logActivities();
        activityService.logMemoryUsage();
    }

    @Inject
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }
}
