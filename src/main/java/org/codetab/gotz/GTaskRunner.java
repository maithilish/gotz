package org.codetab.gotz;

import javax.inject.Inject;

import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTaskRunner {

    static final Logger LOGGER = LoggerFactory.getLogger(GTaskRunner.class);

    @Inject
    private TaskPoolService taskPoolService;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    private AppenderService appenderService;

    @Inject
    public GTaskRunner() {
    }

    /*
     */
    public boolean executeInitalTask(final Task task) {
        LOGGER.info(Messages.getString("GTaskRunner.0")); //$NON-NLS-1$
        taskPoolService.submit("seeder", task); //$NON-NLS-1$
        return true;
    }

    /*
     */
    public boolean waitForFinish() {
        taskPoolService.waitForFinish();
        appenderService.closeAll();
        appenderPoolService.waitForFinish();
        return true;
    }
}
