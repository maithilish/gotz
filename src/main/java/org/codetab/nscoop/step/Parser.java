package org.codetab.nscoop.step;

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
import org.codetab.nscoop.dao.DaoFactory;
import org.codetab.nscoop.dao.IDataDao;
import org.codetab.nscoop.dao.DaoFactory.ORM;
import org.codetab.nscoop.exception.DataDefNotFoundException;
import org.codetab.nscoop.exception.FieldNotFoundException;
import org.codetab.nscoop.model.Axis;
import org.codetab.nscoop.model.AxisName;
import org.codetab.nscoop.model.Data;
import org.codetab.nscoop.model.DataDef;
import org.codetab.nscoop.model.Document;
import org.codetab.nscoop.model.FieldsBase;
import org.codetab.nscoop.model.Member;
import org.codetab.nscoop.model.Activity.Type;
import org.codetab.nscoop.shared.ConfigService;
import org.codetab.nscoop.shared.DataDefService;
import org.codetab.nscoop.shared.MonitorService;
import org.codetab.nscoop.util.FieldsUtil;
import org.codetab.nscoop.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Parser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private String dataDefName;
    private Document document;
    private Data data;

    private Set<Integer[]> memberIndexSet = new HashSet<>();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
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
                LOGGER.info("parse data {}", Util.getLocatorLabel(getFields()));
                prepareData();
                parse();
                setConsistent(true);
                store();
            } else {
                setConsistent(true);
                LOGGER.info("found parsed data {}", Util.getLocatorLabel(getFields()));
            }
            handover();
        } catch (Exception e) {
            String message = "parse data " + Util.getLocatorLabel(getFields());
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
        }
    }

    protected abstract void setValue(DataDef dataDef, Member member)
            throws ScriptException, NumberFormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException;

    private void initialize() throws FieldNotFoundException, DataDefNotFoundException {
        dataDefName = FieldsUtil.getValue(getFields(), "datadef");
        // locatorName = FieldsUtil.getValue(getFields(), "locatorName");
    }

    private void prepareData()
            throws DataDefNotFoundException, ClassNotFoundException, IOException {
        data = DataDefService.INSTANCE.getDataTemplate(dataDefName);
        data.setDataDefId(DataDefService.INSTANCE.getDataDef(dataDefName).getId());
        data.setDocumentId(getDocument().getId());
        Util.logState(LOGGER, "parser-" + dataDefName, "Data Template", getFields(),
                data);
    }

    /*
     *
     */
    public void parse()
            throws DataDefNotFoundException, ScriptException, FieldNotFoundException,
            ClassNotFoundException, IOException, NumberFormatException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        parseData();
    }

    /*
     *
     */
    public void parseData()
            throws DataDefNotFoundException, ScriptException, ClassNotFoundException,
            IOException, NumberFormatException, FieldNotFoundException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
        Util.logState(LOGGER, "parser-" + dataDefName, "Data after parse", getFields(),
                data);
    }

    private void pushNewMember(final Deque<Member> mStack, final Member member)
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
                    // nullify all axis value
                    for (Axis na : newMember.getAxes()) {
                        na.setValue(null);
                    }
                    mStack.addFirst(newMember); // push
                    memberIndexSet.add(nextMemberIndexes);
                }
            }
        }
    }

    private boolean hasFinished(final Axis axis)
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

    private Integer[] nextMemberIndexes(final Member member, final AxisName axisName) {
        Integer[] indexes = getMemberIndexes(member);
        indexes[axisName.ordinal()] = indexes[axisName.ordinal()] + 1;
        return indexes;
    }

    private boolean alreadyProcessed(final Integer[] memberIndexes) {
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

    private Integer[] getMemberIndexes(final Member member) {
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

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#load()
     */
    @Override
    public void load() throws Exception {
        Long dataDefId = DataDefService.INSTANCE.getDataDef(dataDefName).getId();
        Long documentId = getDocument().getId();
        data = getDataFromStore(dataDefId, documentId);
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#store()
     */
    @Override
    public void store() throws Exception {
        boolean persist = true;
        try {
            persist = FieldsUtil.isFieldTrue(getFields(), "persistdata");
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
                LOGGER.debug("{}", e.getMessage());
                throw e;
            }
            LOGGER.debug("Stored {}", data);
        } else {
            LOGGER.debug("Persist Data [false]. Not Stored {}", data);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#handover()
     */
    @Override
    public void handover() throws Exception {
        pushTask(data, getFields());
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.Step#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#setInput(java.lang.Object)
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Document) {
            this.document = (Document) input;
        } else {
            LOGGER.warn("Input is not instance of Document type. {}",
                    input.getClass().toString());
        }
    }

    /*
     *
     */
    protected Document getDocument() {
        return document;
    }

    /*
     *
     */
    protected boolean isDocumentLoaded() {
        if (document.getDocumentObject() == null) {
            return false;
        }
        return true;
    }

    /*
     *
     */
    protected Data getData() {
        return data;
    }

    private Data getDataFromStore(final Long dataDefId, final Long documentId) {
        try {
            ORM orm = DaoFactory
                    .getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
            IDataDao dao = DaoFactory.getDaoFactory(orm).getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw e;
        }
    }

    /*
     *
     */
    protected Integer getStartIndex(final List<FieldsBase> fields)
            throws NumberFormatException, FieldNotFoundException {
        Range<Integer> indexRange = FieldsUtil.getRange(fields, "indexRange");
        return indexRange.getMinimum();
    }

    /*
     *
     */
    protected Integer getEndIndex(final List<FieldsBase> fields)
            throws NumberFormatException, FieldNotFoundException {
        Range<Integer> indexRange = FieldsUtil.getRange(fields, "indexRange");
        return indexRange.getMaximum();
    }

    /*
     *
     */
    protected String getDataDefName() {
        return dataDefName;
    }
}