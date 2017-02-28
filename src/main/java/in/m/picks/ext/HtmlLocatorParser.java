package in.m.picks.ext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.AxisName;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.model.Member;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.BeanService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class HtmlLocatorParser extends HtmlParser {

	final Logger log = LoggerFactory.getLogger(HtmlLocatorParser.class);

	@Override
	public IStep instance() {
		return new HtmlLocatorParser();
	}

	@Override
	public void handover() throws Exception {
		String givenUpMessage = Util.buildString("Create locator for locator [",
				locatorName, "] failed.");
		for (Member member : data.getMembers()) {
			Locator locator = createLocator(member);
			try {
				List<FieldsBase> loaders = FieldsUtil
						.getFieldList(locator.getFields(), "loader");
				if (loaders.size() == 0) {
					throw new FieldNotFoundException("no loader field defined");
				}
				for (FieldsBase loader : loaders) {
					IStep task = createTask(loader.getValue(), locator);
					TaskPoolService.getInstance().submit("loader", task);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | FieldNotFoundException e) {
				log.warn("{} {}", givenUpMessage, e.getMessage());
				MonitorService.INSTANCE.addActivity(Type.GIVENUP, locator.toString(),
						e);
			}
		}
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

	private Locator createLocator(Member member) throws FieldNotFoundException {
		Locator locator = new Locator();
		locator.setName(FieldsUtil.getValue(getFields(), "locatorName"));
		locator.setUrl(member.getValue(AxisName.FACT));
		if (member.getGroup() == null) {
			throw new FieldNotFoundException(
					"unable to create new locator. define group for member in datadef of locator type "
							+ member.getName());
		} else {
			locator.setGroup(member.getGroup());
			List<FieldsBase> groupFileds = getGroupFields(locator.getGroup());
			locator.getFields().addAll(groupFileds);
			if (member.getFields() != null) {
				locator.getFields().addAll(member.getFields());
			}
		}
		log.trace("created new {} {}", locator, locator.getUrl());
		return locator;
	}

	private List<FieldsBase> getGroupFields(String group)
			throws FieldNotFoundException {
		List<FieldsBase> fieldsBeans = BeanService.INSTANCE
				.getBeans(FieldsBase.class);
		FieldsBase classFields = FieldsUtil.getFieldsByValue(fieldsBeans, "class",
				Locator.class.getName());
		if (classFields != null) {
			List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields, group);
			return fields;
		}
		return null;
	}

	@Override
	protected void filter() throws Exception {
		// not required
	}

	@Override
	public void store() throws Exception {
		// not required
	}
}
