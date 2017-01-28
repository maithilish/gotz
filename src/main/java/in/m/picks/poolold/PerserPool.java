package in.m.picks.poolold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Afields;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;
import in.m.picks.util.AccessUtil;

public class PerserPool extends Pool {

	final Logger logger = LoggerFactory.getLogger(PerserPool.class);

	public final static PerserPool INSTANCE = new PerserPool();

	private PerserPool() {
		super("picks.parserThreads");
	}

	public boolean submitTask(String stepClzName, Object input, Afields afields) {
		String locatorName = null;
		try {
			locatorName = AccessUtil.getStringValue(afields, "locatorName");
			IStep task = StepService.INSTANCE.getStep(stepClzName);
			task.setInput(input);
			task.setAfields(afields);
			submit(task);
			log.trace(">> [{}] {}", stepClzName, input);
			return true;
		} catch (AfieldNotFoundException e) {
			logger.debug("{}", e);
			givenupTask(locatorName, e);
		} catch (ClassNotFoundException e) {
			givenupTask(locatorName, e);
		} catch (InstantiationException e) {
			givenupTask(locatorName, e);
		} catch (IllegalAccessException e) {
			givenupTask(locatorName, e);
		}
		return false;
	}

	public void givenupTask(String locatorName, Exception e) {
		MonitorService.INSTANCE.addActivity(Type.GIVENUP, locatorName, e);
	}

}
