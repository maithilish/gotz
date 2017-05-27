package org.codetab.gotz.step;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.codetab.gotz.dao.DaoFactory;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Parser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private String dataDefName;
    private Document document;
    private Data data;

    private Set<Integer[]> memberIndexSet = new HashSet<>();

    @Inject
    private DaoFactory daoFactory;

    @Override
    public boolean initialize() {
        try {
            dataDefName = FieldsUtil.getValue(getFields(), "datadef");
        } catch (FieldNotFoundException e) {
            throw new StepRunException("unable to initialize parser", e);
        }
        return true;
        // locatorName = FieldsUtil.getValue(getFields(), "locatorName");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#load()
     */
    @Override
    public boolean load() {
        Long dataDefId;
        try {
            dataDefId = dataDefService.getDataDef(dataDefName).getId();
            Long documentId = getDocument().getId();
            data = getDataFromStore(dataDefId, documentId);
        } catch (DataDefNotFoundException e) {
            String givenUpMessage = "unable to get datadef id";
            LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
            throw new StepRunException(givenUpMessage, e);
        }
        return true;
    }

    @Override
    public boolean process() {
        if (data == null) {
            LOGGER.info("parse data {}", Util.getLocatorLabel(getFields()));
            try {
                prepareData();
                parse();
                setConsistent(true);
            } catch (ClassNotFoundException | DataDefNotFoundException | IOException
                    | NumberFormatException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException | ScriptException
                    | FieldNotFoundException e) {
                String message = "parse data " + Util.getLocatorLabel(getFields());
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            LOGGER.info("found parsed data {}", Util.getLocatorLabel(getFields()));
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#store()
     */
    @Override
    public boolean store() {
        boolean persist = true;
        try {
            persist = FieldsUtil.isFieldTrue(getFields(), "persistdata");
        } catch (FieldNotFoundException e) {
        }
        if (persist) {
            try {
                ORM orm = configService.getOrmType();
                IDataDao dao = daoFactory.getDaoFactory(orm).getDataDao();
                dao.storeData(data);
                data = dao.getData(data.getId());
            } catch (RuntimeException e) {
                LOGGER.debug("{}", e.getMessage());
                throw e;
            }
            LOGGER.debug("Stored {}", data);
        } else {
            LOGGER.debug("Persist Data [false]. Not Stored {}", data);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#handover()
     */
    @Override
    public boolean handover() {
        stepService.pushTask(this, data, getFields());
        return true;
    }

    protected abstract void setValue(DataDef dataDef, Member member)
            throws ScriptException, NumberFormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException;

    private void prepareData()
            throws DataDefNotFoundException, ClassNotFoundException, IOException {
        data = dataDefService.getDataTemplate(dataDefName);
        data.setDataDefId(dataDefService.getDataDef(dataDefName).getId());
        data.setDocumentId(getDocument().getId());
        Util.logState(LOGGER, "parser-" + dataDefName, "Data Template", getFields(),
                data);
    }

    //    public void parse()
    //            throws DataDefNotFoundException, ScriptException, FieldNotFoundException,
    //            ClassNotFoundException, IOException, NumberFormatException,
    //            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    //        parseData();
    //    }

    /*
     *
     */
    public void parse()
            throws DataDefNotFoundException, ScriptException, ClassNotFoundException,
            IOException, NumberFormatException, FieldNotFoundException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        DataDef dataDef = dataDefService.getDataDef(dataDefName);
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
            Axis axis = null;
            try {
                axis = member.getAxis(axisName);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (axis.getName().equals(AxisName.FACT)) {
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
                int index = indexes[i];
                int memberIndex = memberIndexes[i];
                if (index != memberIndex) {
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
            Axis axis = null;
            int index = 0;
            try {
                axis = member.getAxis(axisName);
                index = new Integer(axis.getIndex());
            } catch (NoSuchElementException e) {
            }
            memberIndexes[axisName.ordinal()] = index;
        }
        return memberIndexes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.StepO#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#setInput(java.lang.Object)
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
            ORM orm = configService.getOrmType();
            IDataDao dao = daoFactory.getDaoFactory(orm).getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new StepRunException("config error", e);
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
