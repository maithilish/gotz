package org.codetab.gotz;

import java.util.Date;

import javax.inject.Inject;

import org.codetab.gotz.exception.FatalException;
import org.codetab.gotz.misc.ShutdownHook;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GotzEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(GotzEngine.class);

    // di items
    private ActivityService activityService;
    private ConfigService configService;
    private BeanService beanService;
    private DataDefService dataDefService;
    private StepService stepService;
    private TaskPoolService taskPoolService;
    private AppenderPoolService appenderPoolService;
    private AppenderService appenderService;
    private ShutdownHook shutdownHook;
    private Runtime runTime;

    @Inject
    private GotzEngine() {

    }

    /*
     * single thread env throws FatalException and terminates the app and multi thread env
     * also throws FatalException but they are catched at thread level and terminates the
     * thread
     *
     */
    public void start() {
        try {
            // single thread env
            initSystem();
            IStep task = createInitialTask();

            // multi thread env
            executeInitalTask(task);
            waitForFinish();
        } catch (FatalException e) {
            LOGGER.error("{}", e);
        }
    }

    private void initSystem() throws FatalException {
        LOGGER.info("Starting GotzEngine");

        runTime.addShutdownHook(shutdownHook);
        activityService.start();

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        String mode = getMode();
        LOGGER.info(mode);
        Date runDate = configService.getRunDate();
        LOGGER.info("Run Date : [{}]", runDate);

        beanService.init();

        dataDefService.init();
        int dataDefsCount = dataDefService.getCount();
        LOGGER.info("DataDefs loaded {}", dataDefsCount);
    }

    private IStep createInitialTask() throws FatalException {
        try {
            String seederClassName = configService.getConfig("gotz.seederClass");
            IStep task = stepService.getStep(seederClassName);
            task = task.instance();
            return task;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            LOGGER.error("{}", e);
            throw new FatalException("unable to create initial task");
        }
    }

    private void executeInitalTask(IStep task) {
        taskPoolService.submit("seeder", task);
    }

    private void waitForFinish() {
        taskPoolService.waitForFinish();

        appenderService.closeAll();
        appenderPoolService.waitForFinish();

        activityService.end();
        LOGGER.info("GotzEngine shutdown");
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

    @Inject
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Inject
    public void setBeanService(BeanService beanService) {
        this.beanService = beanService;
    }

    @Inject
    public void setDataDefService(DataDefService dataDefService) {
        this.dataDefService = dataDefService;
    }

    @Inject
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Inject
    public void setStepService(StepService stepService) {
        this.stepService = stepService;
    }

    @Inject
    public void setTaskPoolService(TaskPoolService taskPoolService) {
        this.taskPoolService = taskPoolService;
    }

    @Inject
    public void setAppenderPoolService(AppenderPoolService appenderPoolService) {
        this.appenderPoolService = appenderPoolService;
    }

    @Inject
    public void setAppenderService(AppenderService appenderService) {
        this.appenderService = appenderService;
    }

    @Inject
    public void setShutdownHook(ShutdownHook shutdownHook) {
        this.shutdownHook = shutdownHook;
    }

    @Inject
    public void setRunTime(Runtime runTime) {
        this.runTime = runTime;
    }

}
