package org.codetab.gotz.step.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.persistence.DataPersistence;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public abstract class BaseParser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseParser.class);

    private String dataDefName;
    private Document document;
    private Data data;
    private Marker marker;

    private Set<Integer[]> memberIndexSet = new HashSet<>();

    @Inject
    private DataPersistence dataPersistence;

    @Override
    public boolean initialize() {
        try {
            dataDefName = fieldsHelper.getLastValue("/:fields/:task/@dataDef",
                    getFields());
            String locatorName = fieldsHelper
                    .getLastValue("/:fields/:locatorName", getFields());
            String locatorGroup = fieldsHelper
                    .getLastValue("/:fields/:locatorGroup", getFields());
            marker = MarkerUtil.getMarker(locatorName, locatorGroup,
                    dataDefName);
        } catch (FieldsException e) {
            throw new StepRunException("unable to initialize parser", e);
        }
        return postInitialize();
    }

    protected abstract boolean postInitialize();

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
        } catch (DataDefNotFoundException e) {
            String givenUpMessage = "unable to get datadef id";
            throw new StepRunException(givenUpMessage, e);
        }
        Long documentId = getDocument().getId();
        if (documentId != null) {
            data = dataPersistence.loadData(dataDefId, documentId);
        }
        return true;
    }

    @Override
    public boolean process() {
        if (data == null) {
            LOGGER.info("parse data {}", getLabel());
            try {
                prepareData();
                parse();
                setConsistent(true);
            } catch (ClassNotFoundException | DataDefNotFoundException
                    | IOException | NumberFormatException
                    | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | ScriptException
                    | DataFormatException | FieldsException e) {
                String message =
                        Util.buildString("unable to parse ", getLabel());
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            LOGGER.info("found parsed data {}", getLabel());
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
            persist = fieldsHelper.isTrue("/:fields/:task/:persist/:data",
                    getFields());
        } catch (FieldsException e) {
        }
        if (persist) {
            dataPersistence.storeData(data);
            data = dataPersistence.loadData(data.getId());
            LOGGER.debug("stored data : {}", getLabel());
        } else {
            LOGGER.debug("Data for [{}] is not stored as [persist=false]",
                    getLabel());
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
        Fields nextStepFields = createNextStepFields();
        stepService.pushTask(this, data, nextStepFields);
        return true;
    }

    private Fields createNextStepFields() {
        Fields nextStepFields = getFields();
        if (nextStepFields.getNodes().size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepFields;
    }

    protected abstract void setValue(DataDef dataDef, Member member)
            throws ScriptException, NumberFormatException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, MalformedURLException, IOException,
            DataFormatException;

    private void prepareData() throws DataDefNotFoundException,
            ClassNotFoundException, IOException {
        data = dataDefService.getDataTemplate(dataDefName);
        data.setDataDefId(dataDefService.getDataDef(dataDefName).getId());
        data.setDocumentId(getDocument().getId());
        LOGGER.trace(marker, "-- data template --{}{}{}{}", Util.LINE,
                marker.getName(), Util.LINE, data);
    }

    // public void parse()
    // throws DataDefNotFoundException, ScriptException, FieldNotFoundException,
    // ClassNotFoundException, IOException, NumberFormatException,
    // IllegalAccessException, InvocationTargetException, NoSuchMethodException
    // {
    // parseData();
    // }

    /*
     *
     */
    public void parse() throws DataDefNotFoundException, ScriptException,
            ClassNotFoundException, IOException, NumberFormatException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, DataFormatException, FieldsException {
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
        LOGGER.trace(marker, "-- data after parse --{}{}{}{}", Util.LINE,
                marker.getName(), Util.LINE, data);
    }

    private void pushNewMember(final Deque<Member> mStack, final Member member)
            throws IOException, ClassNotFoundException, NumberFormatException,
            FieldsException {
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
                Integer[] nextMemberIndexes =
                        nextMemberIndexes(member, axisName);
                if (!alreadyProcessed(nextMemberIndexes)) {
                    // Member newMember = Util.deepClone(Member.class, member);
                    Member newMember = createMember(member);
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

    /**
     * Deep copy member, but for performance fields are not cloned instead
     * reference is passed to copy.
     * @param member
     * @return
     */
    private Member createMember(final Member member) {
        Member cMember = new Member();
        cMember.setName(member.getName());
        cMember.setGroup(member.getGroup());
        for (Axis axis : member.getAxes()) {
            Axis cAxis = new Axis();
            cAxis.setName(axis.getName());
            cAxis.setMatch(axis.getMatch());
            cAxis.setOrder(Integer.valueOf(axis.getOrder()));
            cAxis.setIndex(Integer.valueOf(axis.getIndex()));
            cAxis.setValue(axis.getValue());
            cAxis.setFields(axis.getFields());
            cMember.addAxis(cAxis);
        }
        cMember.setFields(member.getFields());
        return cMember;
    }

    private boolean hasFinished(final Axis axis)
            throws NumberFormatException, FieldsException {
        boolean noField = true;
        try {
            String breakAfter = fieldsHelper
                    .getLastValue("//xf:breakAfter/@value", axis.getFields());
            noField = false;
            String value = axis.getValue().trim();
            if (value.equals(breakAfter)) {
                return true;
            }
        } catch (FieldsException e) {
        } catch (NullPointerException e) {
            String message = Util.buildString(
                    "check breakAfter value in datadef ", getLabel());
            throw new NullPointerException(message);
        }
        try {
            Integer endIndex = getEndIndex(axis.getFields());
            noField = false;
            if (axis.getIndex() + 1 > endIndex) {
                return true;
            }
        } catch (FieldsException e) {
        }
        if (noField) {
            String message = Util.buildString(
                    "breakAfter or indexRange undefined ", getLabel());
            throw new FieldsException(message);
        }
        return false;
    }

    private Integer[] nextMemberIndexes(final Member member,
            final AxisName axisName) {
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

    /*
     *
     */
    protected Integer getStartIndex(final Fields fields)
            throws NumberFormatException, FieldsException {
        if (fields == null) {
            throw new FieldsException("fields is null");
        }
        Range<Integer> indexRange =
                fieldsHelper.getRange("//xf:indexRange/@value", fields);
        return indexRange.getMinimum();
    }

    /*
     *
     */
    protected Integer getEndIndex(final Fields fields)
            throws NumberFormatException, FieldsException {
        if (fields == null) {
            throw new FieldsException("fields is null");
        }
        Range<Integer> indexRange =
                fieldsHelper.getRange("//xf:indexRange/@value", fields);
        return indexRange.getMaximum();
    }

    /*
     *
     */
    protected String getDataDefName() {
        return dataDefName;
    }

    public Marker getMarker() {
        return marker;
    }
}
