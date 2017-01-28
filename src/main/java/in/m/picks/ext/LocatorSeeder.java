package in.m.picks.ext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Afield;
import in.m.picks.model.Afields;
import in.m.picks.model.Locator;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.BeanService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;
import in.m.picks.step.Seeder;
import in.m.picks.util.Util;

public class LocatorSeeder extends Seeder {

	final static Logger logger = LoggerFactory.getLogger(LocatorSeeder.class);

	List<Locator> locators;

	@Override
	public IStep instance() {
		return new LocatorSeeder();
	}

	@Override
	public void load() throws Exception {
		locators = BeanService.INSTANCE.getBeans(Locator.class);
		List<Afields> afieldsList = BeanService.INSTANCE.getBeans(Afields.class);
		Afields afields = getLocatorAfields(afieldsList);
		if (afields != null) {
			mergeFields(afields);
		}
	}

	@Override
	public void handover() throws Exception {
		int count = 0;
		for (Locator locator : locators) {
			try {
				String givenUpMessage = Util.buildString(
						"Create loader for locator [", locator.getName(),
						"] failed.");
				List<Afield> afieldList = locator.getAfieldsByGroup("loader")
						.getAfields();
				if (afieldList.size() == 0) {
					logger.warn("{} {}", givenUpMessage, " No loader afield found.");
				}
				for (Afield afield : afieldList) {
					IStep task = createTask(afield.getValue(), locator);
					TaskPoolService.getInstance().submit("loader", task);
					count++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | AfieldNotFoundException e) {
				MonitorService.INSTANCE.addActivity(Type.GIVENUP, locator.toString(),
						e);
			}
		}
		logger.info("Locators : Total [{}]. Queued to loader [{}].", locators.size(),
				count);
	}

	private IStep createTask(String loaderClassName, Locator locator)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, AfieldNotFoundException {
		IStep task = StepService.INSTANCE.getStep(loaderClassName);
		task.setInput(locator);
		logger.trace("> [Loader] {}", locator);
		return task;
	}

	private Afields getLocatorAfields(List<Afields> afieldsList) {
		for (Afields afields : afieldsList) {
			try {
				Class<?> clz = Class.forName(afields.getClassName());
				if (clz == Locator.class) {
					return afields;
				}
			} catch (ClassNotFoundException e) {

			}
		}
		return null;
	}

	private void mergeFields(Afields afields) {
		logger.debug("Merging Afields with Locators");
		logger.debug("Locator afields size [{}]", afields.size());
		for (Locator locator : locators) {
			for (Afield afield : afields.getAfields()) {				
					locator.addAfield(afield);
			}
		}
	}
	
//	private void mergeFields(Afields afields) {
//		logger.debug("Merging Afields with Locators");
//		logger.debug("Locator afields size [{}]", afields.size());
//		for (Locator locator : locators) {
//			for (Afield afield : afields.getAfields()) {
//				if (locator.getGroup().equals(afield.getGroup())) {
//					locator.addAfield(afield);
//				}
//			}
//		}
//	}
}
