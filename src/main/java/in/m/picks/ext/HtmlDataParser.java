package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.misc.FieldsIterator;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.DataDef;
import in.m.picks.model.Field;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public class HtmlDataParser extends HtmlParser {

	final static Logger logger = LoggerFactory.getLogger(HtmlDataParser.class);

	@Override
	public IStep instance() {
		return new HtmlDataParser();
	}

	@Override
	public void parse() throws DataDefNotFoundException, ScriptException,
			FieldNotFoundException {
		parseData();
	}

	public void parseData() throws DataDefNotFoundException, ScriptException,
			FieldNotFoundException {
		DataDef dataDef = DataDefService.INSTANCE.getDataDef(dataDefName);
		for (Member member : data.getMembers()) {
			// collections.sort not possible as axes is set so implied sort
			// as value field of an axis may be referred by later axis
			for (AxisName axisName : AxisName.values()) {
				Axis axis = member.getAxis(axisName.toString());
				if (axis != null) {
					if (isDocumentLoaded()) {
						HtmlPage page = (HtmlPage) getDocument().getDocumentObject();
						String value = getValue(page, dataDef, member, axis);
						axis.setValue(value);
					}
				}
			}
		}
		Util.logState(logger, "parser-" + dataDefName, "Data after parse",
				getFields(), data);
	}

	@Override
	public void filter() throws Exception {
		List<Member> forRemovalMembers = new ArrayList<Member>();
		Map<String, List<FieldsBase>> filterMap = DataDefService.INSTANCE
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
			Map<String, List<FieldsBase>> filterMap) {
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
