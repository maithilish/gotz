package in.m.picks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Locator;
import in.m.picks.pool.AppenderPoolService;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.AppenderService;
import in.m.picks.shared.BeanService;
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.DataDefService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;

public class PicksEngine {

	final static Logger logger = LoggerFactory.getLogger(PicksEngine.class);

	public void start() {

		logger.info("Starting PicksEngine");
		logPicksMode();
		MonitorService.INSTANCE.start();
		logger.info("Run Date : [{}]", ConfigService.INSTANCE.getRunDate());
		BeanService.INSTANCE.getBeans(Locator.class);
		loadDataDefs();

		seed();
		TaskPoolService.getInstance().waitForFinish();

		AppenderService.INSTANCE.closeAll();
		AppenderPoolService.getInstance().waitForFinish();

		MonitorService.INSTANCE.end();
		logger.info("PicksEngine shutdown");

	}

	private void loadDataDefs() {
		DataDefService dataDefs = DataDefService.INSTANCE;
		logger.info("DataDefs loaded {}", dataDefs.getCount());
	}

	private void seed() {
		try {
			String seederClassName = ConfigService.INSTANCE
					.getConfig("picks.seederClass");
			IStep task = StepService.INSTANCE.getStep(seederClassName)
					.instance();
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
		logger.info(modeInfo);
	}
}
