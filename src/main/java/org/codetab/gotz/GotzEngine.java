package org.codetab.gotz;

import org.codetab.gotz.model.Locator;
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

    public void start() {

        LOGGER.info("Starting GotzEngine");
        logMode();
        MonitorService.instance().start();
        LOGGER.info("Run Date : [{}]", ConfigService.INSTANCE.getRunDate());
        BeanService.instance().getBeans(Locator.class);
        loadDataDefs();

        seed();
        TaskPoolService.getInstance().waitForFinish();

        AppenderService.INSTANCE.closeAll();
        AppenderPoolService.getInstance().waitForFinish();

        MonitorService.instance().end();
        LOGGER.info("GotzEngine shutdown");

    }

    private void loadDataDefs() {
        DataDefService dataDefs = DataDefService.instance();
        LOGGER.info("DataDefs loaded {}", dataDefs.getCount());
    }

    private void seed() {
        try {
            String seederClassName = ConfigService.INSTANCE.getConfig("gotz.seederClass");
            IStep task = StepService.INSTANCE.getStep(seederClassName).instance();
            TaskPoolService.getInstance().submit("seeder", task);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void logMode() {
        String modeInfo = "Mode : [Production]";
        if (ConfigService.INSTANCE.isTestMode()) {
            modeInfo = "Mode : [CTest]";
        }
        if (ConfigService.INSTANCE.isDevMode()) {
            modeInfo = "Mode : [Dev]";
        }
        LOGGER.info(modeInfo);
    }
}
