package in.m.picks.ext;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	Set<Integer[]> memberIndexSet = new HashSet<>();

	@Override
	public IStep instance() {
		return new HtmlDataParser();
	}

	@Override
	public void parse() throws DataDefNotFoundException, ScriptException,
			FieldNotFoundException, ClassNotFoundException, IOException {
		parseData();
	}

	public void parseData()
			throws DataDefNotFoundException, ScriptException, ClassNotFoundException,
			IOException, NumberFormatException, FieldNotFoundException {
		DataDef dataDef = DataDefService.INSTANCE.getDataDef(dataDefName);
		Deque<Member> mStack = new ArrayDeque<>();
		for (Member member : data.getMembers()) {
			mStack.addFirst(member);
		}
		List<Member> members = new ArrayList<>(); // expanded member list
		while (!mStack.isEmpty()) {
			Member member = mStack.removeFirst(); // pop
			members.add(member);
			// collections.sort not possible as axes is set so implied sort
			// as value field of an axis may be referred by later axis
			setValue(dataDef, member);
			pushNewMember(mStack, member);
		}
		data.setMembers(members); // replace with expanded member list
		Util.logState(logger, "parser-" + dataDefName, "Data after parse",
				getFields(), data);
	}

	private void setValue(DataDef dataDef, Member member)
			throws ScriptException, NumberFormatException {
		for (AxisName axisName : AxisName.values()) {
			Axis axis = member.getAxis(axisName);
			if (axis == null) {
				continue;
			}
			if (axis.getIndex() == null) {
				Integer startIndex;
				try {
					startIndex = getStartIndex(axis.getFields());
				} catch (FieldNotFoundException e) {
					startIndex = 1;
				}
				axis.setIndex(startIndex);
			}
			if (isDocumentLoaded()) {
				HtmlPage documentObject = (HtmlPage) getDocument()
						.getDocumentObject();
				String value = getValue(documentObject, dataDef, member, axis);
				axis.setValue(value);
			}
		}
	}

	private void pushNewMember(Deque<Member> mStack, Member member)
			throws IOException, ClassNotFoundException, NumberFormatException,
			FieldNotFoundException {
		for (AxisName axisName : AxisName.values()) {
			Axis axis = member.getAxis(axisName);
			if (axis == null || axis.getName().equals(AxisName.FACT)) {
				continue;
			}
			if (!hasFinished(axis)) {
				Integer[] nextMemberIndexes = nextMemberIndexes(member, axisName);
				if (!alreadyProcessed(nextMemberIndexes)) {
					Member newMember = Util.deepClone(Member.class, member);
					Axis newAxis = newMember.getAxis(axisName);
					newAxis.setIndex(newAxis.getIndex() + 1);
					newAxis.setOrder(newAxis.getOrder() + 1);
					newAxis.setValue(null);
					mStack.addFirst(newMember); // push
					memberIndexSet.add(nextMemberIndexes);
				}
			}
		}
	}

	private boolean hasFinished(Axis axis)
			throws NumberFormatException, FieldNotFoundException {
		boolean noField = true;
		try {
			String breakAfter = FieldsUtil.getValue(axis.getFields(), "breakAfter");
			noField = false;
			String value = axis.getValue().trim();
			if (value.equals(breakAfter)) {
				return true;
			}
		} catch (FieldNotFoundException e) {
		} catch (NullPointerException e) {
			throw new NullPointerException("check breakAfter value in datadef "
					+ Util.getLocatorLabel(getFields()));
		}
		try {
			Integer endIndex = getEndIndex(axis.getFields());
			noField = false;
			if (axis.getIndex() + 1 > endIndex) {
				return true;
			}
		} catch (FieldNotFoundException e) {
		}
		if (noField) {
			throw new FieldNotFoundException("breakAfter or indexRange undefined "
					+ Util.getLocatorLabel(getFields()));
		}
		return false;
	}

	private Integer[] nextMemberIndexes(Member member, AxisName axisName) {
		Integer[] indexes = getMemberIndexes(member);
		indexes[axisName.ordinal()] = indexes[axisName.ordinal()] + 1;
		return indexes;
	}

	private boolean alreadyProcessed(Integer[] memberIndexes) {
		for (Integer[] indexes : memberIndexSet) {
			boolean processed = true;
			for (int i = 0; i < AxisName.values().length; i++) {
				if (indexes[i] != memberIndexes[i]) {
					processed = false;
				}
			}
			if (processed) {
				return processed;
			}
		}
		return false;
	}

	private Integer[] getMemberIndexes(Member member) {
		Integer[] memberIndexes = new Integer[AxisName.values().length];
		for (AxisName axisName : AxisName.values()) {
			Axis axis = member.getAxis(axisName);
			int index = 0;
			if (axis != null) {
				index = new Integer(axis.getIndex());
			}
			memberIndexes[axisName.ordinal()] = index;
		}
		return memberIndexes;
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
