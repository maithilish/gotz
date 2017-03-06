package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.model.Locators;
import in.m.picks.shared.BeanService;
import in.m.picks.step.IStep;
import in.m.picks.step.Seeder;
import in.m.picks.step.Step;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class LocatorSeeder extends Seeder {

	final static Logger logger = LoggerFactory.getLogger(LocatorSeeder.class);

	List<Locator> locators = new ArrayList<>();

	@Override
	public IStep instance() {
		Step step = new LocatorSeeder();
		step.setStepType("seeder");
		return step;
	}

	@Override
	public void load() {
		initLocators();
		List<FieldsBase> fields = BeanService.INSTANCE.getBeans(FieldsBase.class);
		try {
			FieldsBase classFields = FieldsUtil.getFieldsByValue(fields, "class",
					Locator.class.getName());
			List<FieldsBase> steps = FieldsUtil.getGroupFields(classFields, "steps");
			setFields(steps);
			if (classFields != null) {
				mergeFields(classFields);
			}
		} catch (FieldNotFoundException e) {
		}
	}

	private void initLocators() {
		logger.info("initialize locators");
		List<Locators> list = BeanService.INSTANCE.getBeans(Locators.class);
		for (Locators locators : list) {
			trikleGroup(locators);
		}
		for (Locators locators : list) {
			extractLocator(locators);
		}
		for (Locator locator : locators) {
			addLabelField(locator);
		}
		for (Locator locator : locators) {
			Util.logState(logger, "locator", "initialized locator",
					locator.getFields(), locator);
		}
	}

	private void addLabelField(Locator locator) {
		String label = Util.buildString(locator.getName(), ":", locator.getGroup());
		FieldsBase field = FieldsUtil.createField("label", label);
		locator.getFields().add(field);
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
		logger.info("cascade locators group to all locator");
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
		logger.info("push locators to loader taskpool");
		int count = 0;
		for (Locator locator : locators) {
			pushTask(locator, locator.getFields());
			count++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("locators count [{}], queued to loader [{}].", locators.size(),
				count);
	}

	private void mergeFields(FieldsBase classFields) {
		logger.info("merge fields with locators");
		for (Locator locator : locators) {
			try {
				List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields,
						locator.getGroup());
				locator.getFields().addAll(fields);

				List<FieldsBase> steps = FieldsUtil.getGroupFields(classFields,
						"steps");
				locator.getFields().addAll(steps);
			} catch (FieldNotFoundException e) {
			}
		}
		for (Locator locator : locators) {
			Util.logState(logger, "locator", "after merging fields",
					locator.getFields(), locator);
		}
	}

}
