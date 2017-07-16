package org.codetab.gotz;

import java.io.Console;
import java.util.Date;

import javax.inject.Inject;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.misc.ShutdownHook;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Task;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(GSystem.class);

    @Inject
    private ConfigService configService;
    @Inject
    private BeanService beanService;
    @Inject
    private DataDefService dataDefService;
    @Inject
    private StepService stepService;
    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    public GSystem() {
    }

    /*
     *
     */
    public boolean initSystem() {
        LOGGER.info("initialize basic system");
        runTime.addShutdownHook(shutdownHook);

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        String mode = getMode();
        LOGGER.info(mode);
        Date runDate = configService.getRunDate();
        LOGGER.info("Run Date : [{}]", runDate);

        try {
            String beanFile = configService.getConfig("gotz.beanFile");
            String schemaFile = configService.getConfig("gotz.schemaFile");
            beanService.init(beanFile, schemaFile);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException("unable to initialize beanservice", e);
        }

        dataDefService.init();
        int dataDefsCount = dataDefService.getCount();
        LOGGER.info("DataDefs loaded {}", dataDefsCount);
        return true;
    }

    /*
     *
     */
    public Task createInitialTask() {
        LOGGER.info("create inital task");
        try {
            String seederClassName =
                    configService.getConfig("gotz.seederClass");
            IStep step = stepService.getStep(seederClassName);
            step = step.instance();
            Task task = stepService.createTask(step);
            return task;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | ConfigNotFoundException e) {
            LOGGER.error("{}", e);
            throw new CriticalException("unable to create initial task", e);
        }
    }

    private String getMode() {
        String modeInfo = "Mode : [Production]";
        if (configService.isTestMode()) {
            modeInfo = "Mode : [CTest]";
        }
        if (configService.isDevMode()) {
            modeInfo = "Mode : [Dev]";
        }
        return modeInfo;
    }

    public void waitForHeapDump() {
        String wait = "false";
        try {
            wait = configService.getConfig("waitForHeapDump");
        } catch (ConfigNotFoundException e) {
        }
        if (wait.equalsIgnoreCase("true")) {
            Console console = System.console();
            console.printf("%s%s", "Waiting to acquire Heap Dump", Util.LINE);
            console.printf("%s", "Press Enter to continue ...");
            console.readLine();
        }
    }
}
