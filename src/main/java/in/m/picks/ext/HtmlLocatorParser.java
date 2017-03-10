package in.m.picks.ext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.AxisName;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.model.Member;
import in.m.picks.shared.BeanService;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsUtil;

public class HtmlLocatorParser extends HtmlParser {

	final Logger log = LoggerFactory.getLogger(HtmlLocatorParser.class);

	@Override
	public IStep instance() {
		return new HtmlLocatorParser();
	}

	@Override
	public void handover() throws Exception {
		for (Member member : data.getMembers()) {
			Locator locator = createLocator(member);
			String stepType = getStepType();
			setStepType("seeder");
			pushTask(locator, locator.getFields());
			setStepType(stepType);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	private Locator createLocator(Member member) throws FieldNotFoundException {
		Locator locator = new Locator();
		locator.setName(FieldsUtil.getValue(fields, "locatorName"));
		locator.setUrl(member.getValue(AxisName.FACT));
		if (member.getGroup() == null) {
			throw new FieldNotFoundException(
					"unable to create new locator. define group for member in datadef of locator type "
							+ member.getName());
		} else {
			locator.setGroup(member.getGroup());
			List<FieldsBase> groupFields = getGroupFields(locator.getGroup());
			locator.getFields().addAll(groupFields);
			List<FieldsBase> stepFields = getGroupFields("steps");
			locator.getFields().addAll(stepFields);
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
		FieldsBase classFields = FieldsUtil.getFieldsByValue(fieldsBeans,
				"class", Locator.class.getName());
		if (classFields != null) {
			List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields,
					group);
			return fields;
		}
		return null;
	}

	@Override
	public void store() throws Exception {
		// not required
	}
}
