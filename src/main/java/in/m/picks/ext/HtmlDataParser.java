package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.Field;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsIterator;
import in.m.picks.util.FieldsUtil;

public class HtmlDataParser extends HtmlParser {

	final static Logger logger = LoggerFactory.getLogger(HtmlDataParser.class);

	@Override
	public IStep instance() {
		return new HtmlDataParser();
	}

	@Override
	public void filter() throws Exception {
		List<Member> forRemovalMembers = new ArrayList<Member>();
		Map<AxisName, List<FieldsBase>> filterMap = DataDefService.INSTANCE
				.getFilterMap(dataDefName);
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

}
