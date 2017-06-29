package org.codetab.gotz.misc;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.shared.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ShutdownHook extends Thread {

    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    @Inject
    private ActivityService activityService;

    @Inject
    public ShutdownHook() {
        // cs - if private then class has to be final which is unable to mock
        logger.info("shutdownhook created");
    }

    @Override
    public synchronized void start() {
        activityService.logActivities();
        activityService.logMemoryUsage();
    }
}
