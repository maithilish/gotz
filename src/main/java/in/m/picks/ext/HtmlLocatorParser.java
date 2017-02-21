package in.m.picks.ext;

import java.io.IOException;
import java.util.List;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.Data;
import in.m.picks.model.DataDef;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.model.Member;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.BeanService;
import in.m.picks.shared.DataDefService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class HtmlLocatorParser extends HtmlParser {

	final Logger log = LoggerFactory.getLogger(HtmlLocatorParser.class);
	private Data data;

	@Override
	public void parse() throws ClassNotFoundException, DataDefNotFoundException,
			IOException, ScriptException {
		parseLocator();
	}

	private void parseLocator() throws DataDefNotFoundException,
			ClassNotFoundException, IOException, ScriptException {
		DataDef dataDef = DataDefService.INSTANCE.getDataDef(dataDefName);
		data = DataDefService.INSTANCE.getDataTemplate(dataDefName);
		for (Member member : data.getMembers()) {
			// collections.sort not possible as axes is set so implied sort
			// as value field of an axis may be referred by later axis
			for (AxisName axisName : AxisName.values()) {
				Axis axis = member.getAxis(axisName.toString());
				if (axis != null) {
					if (isDocumentLoaded()) {
						HtmlPage page = (HtmlPage) document.getDocumentObject();
						String value = getValue(page, dataDef, member, axis);
						axis.setValue(value);
					}
				}
			}
		}
		DataDefService.INSTANCE.traceDataStructure(data);
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
		locator.setUrl(member.getValue("fact"));
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
	public IStep instance() {
		return new HtmlLocatorParser();
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
