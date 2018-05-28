package org.codetab.gotz;

import javax.inject.Inject;

import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Activity.Type;
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
        LOGGER.info(Messages.getString("GotzEngine.0")); //$NON-NLS-1$
        activityService.start();
        try {
            // single thread env
            gSystem.initSystem();
            Task task = gSystem.createInitialTask();
            LOGGER.info(Messages.getString("GotzEngine.1")); //$NON-NLS-1$
            gSystem.waitForHeapDump();

            LOGGER.info(Messages.getString("GotzEngine.2")); //$NON-NLS-1$
            gTaskRunner.executeInitalTask(task);
            task = null;
            gTaskRunner.waitForFinish();
            gSystem.waitForHeapDump();
            LOGGER.info(Messages.getString("GotzEngine.3")); //$NON-NLS-1$

        } catch (CriticalException e) {
            LOGGER.error("{}", e.getMessage()); //$NON-NLS-1$
            LOGGER.warn(Messages.getString("GotzEngine.5")); //$NON-NLS-1$
            LOGGER.debug("{}", e); //$NON-NLS-1$
            activityService.addActivity(Type.FATAL, e.getMessage(), e);
        }
        gSystem.stopMetricsServer();
        activityService.end();
        LOGGER.info(Messages.getString("GotzEngine.7")); //$NON-NLS-1$
    }

}
