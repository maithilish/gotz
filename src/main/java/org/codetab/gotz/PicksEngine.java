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

public final class PicksEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(PicksEngine.class);

    public void start() {

        LOGGER.info("Starting PicksEngine");
        logPicksMode();
        MonitorService.INSTANCE.start();
        LOGGER.info("Run Date : [{}]", ConfigService.INSTANCE.getRunDate());
        BeanService.INSTANCE.getBeans(Locator.class);
        loadDataDefs();

        seed();
        TaskPoolService.getInstance().waitForFinish();

        AppenderService.INSTANCE.closeAll();
        AppenderPoolService.getInstance().waitForFinish();

        MonitorService.INSTANCE.end();
        LOGGER.info("PicksEngine shutdown");

    }

    private void loadDataDefs() {
        DataDefService dataDefs = DataDefService.INSTANCE;
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

    private void logPicksMode() {
        String modeInfo = "Mode : [Production]";
        if (ConfigService.INSTANCE.isTestMode()) {
            modeInfo = "Mode : [Test]";
        }
        if (ConfigService.INSTANCE.isDevMode()) {
            modeInfo = "Mode : [Dev]";
        }
        LOGGER.info(modeInfo);
    }
}
