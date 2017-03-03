package in.m.picks.step;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.DaoFactory;
import in.m.picks.dao.DaoFactory.ORM;
import in.m.picks.dao.IDataDao;
import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.Data;
import in.m.picks.model.DataDef;
import in.m.picks.model.Document;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.DataDefService;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Parser extends Step {

	final static Logger logger = LoggerFactory.getLogger(Parser.class);

	protected String dataDefName;
	protected String locatorName;
	protected Document document;

	protected Data data;

	private Set<Integer[]> memberIndexSet = new HashSet<>();

	@Override
	public void run() {
		processStep();
	}

	// template method pattern
	private void processStep() {
		try {
			initialize();
			load();
			if (data == null) {
				logger.info("parse data {}", Util.getLocatorLabel(fields));
				prepareData();
				parse();
				consistent = true;
				store();
			} else {
				consistent = true;
				logger.info("found parsed data {}", Util.getLocatorLabel(fields));
			}
			handover();
		} catch (Exception e) {
			String message = "parse data " + Util.getLocatorLabel(fields);
			logger.error("{} {}", message, Util.getMessage(e));
			logger.debug("{}", e);
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
		}
	}

	protected abstract void setValue(DataDef dataDef, Member member)
			throws ScriptException, NumberFormatException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException;

	private void initialize()
			throws FieldNotFoundException, DataDefNotFoundException {
		dataDefName = FieldsUtil.getValue(fields, "datadef");
		locatorName = FieldsUtil.getValue(fields, "locatorName");
	}

	private void prepareData()
			throws DataDefNotFoundException, ClassNotFoundException, IOException {
		data = DataDefService.INSTANCE.getDataTemplate(dataDefName);
		data.setDataDefId(DataDefService.INSTANCE.getDataDef(dataDefName).getId());
		data.setDocumentId(getDocument().getId());
		Util.logState(logger, "parser-" + dataDefName, "Data Template", fields,
				data);
	}

	public void parse() throws DataDefNotFoundException, ScriptException,
			FieldNotFoundException, ClassNotFoundException, IOException,
			NumberFormatException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		parseData();
	}

	public void parseData() throws DataDefNotFoundException, ScriptException,
			ClassNotFoundException, IOException, NumberFormatException,
			FieldNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
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
		Util.logState(logger, "parser-" + dataDefName, "Data after parse", fields,
				data);
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
					+ Util.getLocatorLabel(fields));
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
					+ Util.getLocatorLabel(fields));
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
	public void load() throws Exception {
		Long dataDefId = DataDefService.INSTANCE.getDataDef(dataDefName).getId();
		Long documentId = getDocument().getId();
		data = getDataFromStore(dataDefId, documentId);
	}

	@Override
	public void store() throws Exception {
		boolean persist = true;
		try {
			persist = FieldsUtil.isFieldTrue(fields, "persistdata");
		} catch (FieldNotFoundException e) {
		}
		if (persist) {
			try {
				ORM orm = DaoFactory
						.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
				IDataDao dao = DaoFactory.getDaoFactory(orm).getDataDao();
				dao.storeData(data);
				data = dao.getData(data.getId());
			} catch (Exception e) {
				logger.debug("{}", e.getMessage());
				throw e;
			}
			logger.debug("Stored {}", data);
		} else {
			logger.debug("Persist Data [false]. Not Stored {}", data);
		}
	}

	@Override
	public void handover() throws Exception {		
		pushTask(data, fields);
	}

	@Override
	public boolean isConsistent() {
		return (consistent && data != null);
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Document) {
			this.document = (Document) input;
		} else {
			logger.warn("Input is not instance of Document type. {}",
					input.getClass().toString());
		}
	}

	public Document getDocument() {
		return document;
	}

	protected boolean isDocumentLoaded() {
		if (document.getDocumentObject() == null) {
			return false;
		}
		return true;
	}

	private Data getDataFromStore(Long dataDefId, Long documentId) {
		try {
			ORM orm = DaoFactory
					.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			IDataDao dao = DaoFactory.getDaoFactory(orm).getDataDao();
			Data data = dao.getData(documentId, dataDefId);
			return data;
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

	protected Integer getStartIndex(List<FieldsBase> fields)
			throws NumberFormatException, FieldNotFoundException {
		Range<Integer> indexRange = FieldsUtil.getRange(fields, "indexRange");
		return indexRange.getMinimum();
	}

	protected Integer getEndIndex(List<FieldsBase> fields)
			throws NumberFormatException, FieldNotFoundException {
		Range<Integer> indexRange = FieldsUtil.getRange(fields, "indexRange");
		return indexRange.getMaximum();
	}

}
