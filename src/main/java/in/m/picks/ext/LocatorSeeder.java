package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.model.Locators;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.BeanService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;
import in.m.picks.step.Seeder;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class LocatorSeeder extends Seeder {

	final static Logger logger = LoggerFactory.getLogger(LocatorSeeder.class);

	List<Locator> locators = new ArrayList<>();

	@Override
	public IStep instance() {
		return new LocatorSeeder();
	}

	@Override
	public void load() throws Exception {
		initLocators();
		List<FieldsBase> fields = BeanService.INSTANCE.getBeans(FieldsBase.class);
		try {
			FieldsBase classFields = FieldsUtil.getFieldsByValue(fields, "class",
					Locator.class.getName());
			if (classFields != null) {
				mergeFields(classFields);
			}
		} catch (FieldNotFoundException e) {
		}
	}

	private void initLocators() {
		List<Locators> list = BeanService.INSTANCE.getBeans(Locators.class);
		for (Locators locators : list) {
			trikleGroup(locators);
		}
		for (Locators locators : list) {
			extractLocator(locators);
		}
		for (Locator locator : locators) {
			Util.logState(logger, "locator", "initialized locator",
					locator.getFields(), locator);
		}
	}

	private void extractLocator(Locators locators) {
		for (Locators locs : locators.getLocators()) {
			extractLocator(locs);
		}
		for (Locator locator : locators.getLocator()) {
			this.locators.add(locator);
		}
	}

	private void trikleGroup(Locators locators) {
		for (Locators locs : locators.getLocators()) {
			trikleGroup(locs);
		}
		for (Locator locator : locators.getLocator()) {
			if (locator.getGroup() == null) {
				locator.setGroup(locators.getGroup());
			}
		}
	}

	@Override
	public void handover() {
		int count = 0;
		for (Locator locator : locators) {
			try {
				List<FieldsBase> loaders = FieldsUtil
						.getFieldList(locator.getFields(), "loader");
				if (loaders.size() == 0) {
					throw new FieldNotFoundException("no loader field defined");
				}
				for (FieldsBase loader : loaders) {
					IStep task = createTask(loader.getValue(), locator);
					TaskPoolService.getInstance().submit("loader", task);
					count++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | FieldNotFoundException e) {
				MonitorService.INSTANCE.addActivity(Type.GIVENUP, locator.toString(),
						e);
			}
		}
		logger.info("Locators : Total [{}]. Queued to loader [{}].", locators.size(),
				count);
	}

	private IStep createTask(String loaderClassName, Locator locator)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, FieldNotFoundException {
		IStep task = StepService.INSTANCE.getStep(loaderClassName);
		task.setInput(locator);
		task.setFields(locator.getFields());
		logger.trace("> [Loader] {}", locator);
		return task;
	}

	private void mergeFields(FieldsBase classFields) throws FieldNotFoundException {
		logger.debug("Merging Fields with Locators");
		for (Locator locator : locators) {
			List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields,
					locator.getGroup());
			locator.getFields().addAll(fields);
		}
		for (Locator locator : locators) {
			Util.logState(logger, "locator", "after merging fields",
					locator.getFields(), locator);
		}
	}

}
