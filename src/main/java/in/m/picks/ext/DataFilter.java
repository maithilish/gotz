package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.Data;
import in.m.picks.model.Field;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.DataDefService;
import in.m.picks.shared.MonitorService;
import in.m.picks.step.Filter;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsIterator;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class DataFilter extends Filter {

	final static Logger logger = LoggerFactory.getLogger(DataFilter.class);
	private Data data;

	@Override
	public IStep instance() {
		return new DataFilter();
	}

	@Override
	public void filter() throws Exception {
		List<Member> forRemovalMembers = new ArrayList<Member>();
		Map<AxisName, List<FieldsBase>> filterMap = DataDefService.INSTANCE
				.getFilterMap(data.getDataDef());
		for (Member member : data.getMembers()) {
			for (Axis axis : member.getAxes()) {
				if (requireFilter(axis, filterMap)) {
					forRemovalMembers.add(member);
					break;
				}
			}
		}
		for (Member member : forRemovalMembers) {
			data.getMembers().remove(member);
		}
		DataDefService.INSTANCE.traceDataStructure(data);
	}

	private boolean requireFilter(Axis axis,
			Map<AxisName, List<FieldsBase>> filterMap) {
		List<FieldsBase> filters = filterMap.get(axis.getName());
		if (filters == null) {
			return false;
		}
		if (requireFilter(axis, filters, "match")) {
			return true;
		}
		if (requireFilter(axis, filters, "value")) {
			return true;
		}
		return false;
	}

	private boolean requireFilter(Axis axis, List<FieldsBase> filters,
			String filterGroup) {
		try {
			List<FieldsBase> fil = FieldsUtil.getGroupFields(filters, filterGroup);
			FieldsIterator ite = new FieldsIterator(fil);
			while (ite.hasNext()) {
				FieldsBase field = ite.next();
				if (field instanceof Field) {
					String value = "";
					if (filterGroup.equals("match")) {
						value = axis.getMatch();
					}
					if (filterGroup.equals("value")) {
						value = axis.getValue();
					}
					if (value == null) {
						return false;
					}
					if (value.equals(field.getValue())) {
						return true;
					}
				}
			}
		} catch (FieldNotFoundException e) {
		}
		return false;
	}

	@Override
	public void handover() throws Exception {
		String givenUpMessage = Util.buildString("Create transformer for locator [",
				locatorName, "] failed.");
		List<FieldsBase> transformers = FieldsUtil.getFieldList(fields,
				"transformer");
		if (transformers.size() == 0) {
			logger.warn("{} {}", givenUpMessage, " no transformer field found.");
		}
		for (FieldsBase transformer : transformers) {
			if (data != null) {
				String filterClassName = transformer.getValue();
				IStep task = createTask(filterClassName, data, fields);
				pushTask(task);
			} else {
				logger.warn("Data not loaded - Locator [{}]", locatorName);
				MonitorService.INSTANCE.addActivity(Type.GIVENUP,
						"Data not loaded. " + givenUpMessage);
			}
		}
	}

	private void pushTask(IStep task) {
		try {
			TaskPoolService.getInstance().submit("transformer", task);
			logger.debug("Transformer instance [{}] pushed to pool. Locator [{}]",
					task.getClass(), locatorName);
		} catch (Exception e) {
			logger.warn("Unable to create transformer [{}] for locator [{}]", e,
					locatorName);
			String givenUpMessage = Util.buildString(
					"create transformer for locator [", locatorName, "] failed.");
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, givenUpMessage, e);
		}
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Data) {
			this.data = (Data) input;
		} else {
			logger.error("input is not instance of Data {}",
					input.getClass().toString());
		}
	}

}
