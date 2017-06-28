package org.codetab.gotz;

import javax.inject.Inject;

import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.step.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GotzEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(GotzEngine.class);

    @Inject
    private ActivityService activityService;
    @Inject
    private GSystem gSystem;
    @Inject
    private GTaskRunner gTaskRunner;

    /*
     * single thread env throws CriticalException and terminates the app and
     * multi thread env may also throw CriticalException but they terminates
     * just the executing thread
     *
     */
    public void start() {
        LOGGER.info("starting GotzEngine");
        activityService.start();
        try {
            // single thread env
            gSystem.initSystem();
            Task task = gSystem.createInitialTask();
            LOGGER.info("basic system initialized");

            LOGGER.info("switching to multi thread environment");
            gTaskRunner.executeInitalTask(task);
            gTaskRunner.waitForFinish();
        } catch (CriticalException e) {
            LOGGER.error("{}", "terminating Gotz", e);
        }
        activityService.end();
        LOGGER.info("shutting down GotzEngine");
    }
}
