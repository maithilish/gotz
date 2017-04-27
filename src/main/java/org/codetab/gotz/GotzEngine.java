package org.codetab.gotz;

import java.util.Date;

import javax.inject.Inject;

import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.MonitorService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GotzEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(GotzEngine.class);

    // di items
    private MonitorService monitorService;
    private ConfigService configService;
    private BeanService beanService;
    private DataDefService dataDefService;
    private StepService stepService;
    private TaskPoolService taskPoolService;
    private AppenderPoolService appenderPoolService;
    private AppenderService appenderService;

    public void start() {

        LOGGER.info("Starting GotzEngine");
        monitorService.start();

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

        String seederClassName = configService.getConfig("gotz.seederClass");
        seed(seederClassName);

        taskPoolService.waitForFinish();

        appenderService.closeAll();
        appenderPoolService.waitForFinish();

        monitorService.end();
        LOGGER.info("GotzEngine shutdown");

    }

    private void seed(String seederClassName) {
        try {
            IStep task = stepService.getStep(seederClassName);
            task = task.instance();
            taskPoolService.submit("seeder", task);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    @Inject
    public void setMonitorService(MonitorService monitorService) {
        this.monitorService = monitorService;
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

}
