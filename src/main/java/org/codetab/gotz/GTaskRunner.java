package org.codetab.gotz;

import javax.inject.Inject;

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

    public boolean executeInitalTask(Task task) {
        LOGGER.info("submit initial task to executor pool");
        taskPoolService.submit("seeder", task);
        return true;
    }

    public boolean waitForFinish() {
        taskPoolService.waitForFinish();
        appenderService.closeAll();
        appenderPoolService.waitForFinish();
        return true;
    }
}
