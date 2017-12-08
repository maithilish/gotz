package org.codetab.gotz.step.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.Range;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DataDefHelper;
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
    private ScriptEngine jsEngine;

    private Set<Integer[]> memberIndexSet = new HashSet<>();

    private String blockBegin;
    private String blockEnd;

    @Inject
    private DataPersistence dataPersistence;
    @Inject
    private DataDefHelper dataDefHelper;

    @Override
    public boolean initialize() {

        String dashes = "---------";
        blockBegin = Util.join(Util.LINE, dashes, Util.LINE);
        blockEnd = Util.join(Util.LINE, dashes, dashes);

        try {
            dataDefName = fieldsHelper
                    .getLastValue("/xf:fields/xf:task/@dataDef", getFields());
            String taskName = fieldsHelper
                    .getLastValue("/xf:fields/xf:task/@name", getFields());
            marker = MarkerUtil.getMarker(getLabels().getName(),
                    getLabels().getGroup(), dataDefName);
            // new labels with dataDef and task name
            Labels labels = new Labels(getLabels().getName(),
                    getLabels().getGroup(), taskName, dataDefName);
            setLabels(labels);
        } catch (FieldsNotFoundException e) {
            String message = getLabeled("unable to initialize parser");
            throw new StepRunException(message, e);
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
            String message = getLabeled("unable to get datadef id");
            throw new StepRunException(message, e);
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
            LOGGER.info("[{}] parse data", getLabel());
            try {
                prepareData();
                parse();
                setConsistent(true);
            } catch (ClassNotFoundException | DataDefNotFoundException
                    | IOException | NumberFormatException
                    | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | ScriptException
                    | DataFormatException | FieldsException e) {
                String message = getLabeled("unable to parse");
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            LOGGER.info("[{}] parsed data exists", getLabel());
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
            persist = fieldsHelper.isTrue(
                    "/xf:fields/xf:task/xf:persist/xf:data", getFields());
        } catch (FieldsNotFoundException e) {
        }
        if (persist) {
            dataPersistence.storeData(data);
            data = dataPersistence.loadData(data.getId());
            LOGGER.debug("[{}] data stored", getLabel());
        } else {
            LOGGER.debug("[{}] [persist=false] data not stored", getLabel());
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
        stepService.pushTask(this, data, getLabels(), nextStepFields);
        return true;
    }

    private Fields createNextStepFields() {
        Fields nextStepFields = getFields();
        if (nextStepFields.getNodes().size() == 0) {
            String message = getLabeled("unable to get next step fields");
            throw new StepRunException(message);
        }
        return nextStepFields;
    }

    protected abstract void setValue(DataDef dataDef, Member member)
            throws ScriptException, NumberFormatException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, MalformedURLException, IOException,
            DataFormatException;

    protected abstract String queryByQuery(Object page,
            Map<String, String> queries);

    protected String queryByScript(final Map<String, String> scripts)
            throws ScriptException {
        // TODO - check whether thread safety is involved
        if (jsEngine == null) {
            initializeScriptEngine();
        }

        LOGGER.trace(getMarker(), "<< Query [Script] >>{}", Util.LINE);
        LOGGER.trace(getMarker(), "{}{}{}{}", getLabeled("scripts"), blockBegin,
                scripts, blockEnd);

        jsEngine.put("configs", configService);
        jsEngine.put("document", getDocument());
        String scriptStr = scripts.get("script");
        Object val = jsEngine.eval(scriptStr);
        String value = ConvertUtils.convert(val);

        LOGGER.trace(getMarker(), "result {}", value);
        LOGGER.trace(getMarker(), "<<< Query End >>>");
        LOGGER.trace(getMarker(), "");

        return value;
    }

    private void initializeScriptEngine() {
        LOGGER.debug("{}", getLabeled("initializing script engine"));
        ScriptEngineManager scriptEngineMgr = new ScriptEngineManager();
        jsEngine = scriptEngineMgr.getEngineByName("JavaScript");
        if (jsEngine == null) {
            throw new NullPointerException(
                    "no script engine found for JavaScript. Script engine lib not available in classpath.");
        }
    }

    protected String getValue(final Object page, final DataDef dataDef,
            final Member member, final Axis axis)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // TODO explore whether query can be made strict so that it has
        // to return value else raise StepRunException
        StringBuilder sb = null; // to trace query strings
        String value = null;
        Fields fields =
                dataDefHelper.getAxis(dataDef, axis.getName()).getFields();
        try {
            Map<String, String> scripts = new HashMap<>();
            scripts.put("script",
                    fieldsHelper.getLastValue("/xf:script/@script", fields));

            sb = new StringBuilder();
            setTraceString(sb, scripts, "<<<");

            fieldsHelper.replaceVariables(scripts, member.getAxisMap());

            setTraceString(sb, scripts, ">>>");
            LOGGER.trace(getMarker(), "{}{}{}{}", getLabeled("patch scripts"),
                    getBlockBegin(), sb.toString(), getBlockEnd());

            value = queryByScript(scripts);

        } catch (FieldsNotFoundException e) {
        }

        try {
            Map<String, String> queries = new HashMap<>();
            queries.put("region",
                    fieldsHelper.getLastValue("/xf:query/@region", fields));
            queries.put("field",
                    fieldsHelper.getLastValue("/xf:query/@field", fields));
            // optional attribute
            try {
                queries.put("attribute", fieldsHelper
                        .getLastValue("/xf:query/@attribute", fields));
            } catch (FieldsNotFoundException e) {
                queries.put("attribute", "");
            }

            sb = new StringBuilder();
            setTraceString(sb, queries, "<<<");

            fieldsHelper.replaceVariables(queries, member.getAxisMap());

            setTraceString(sb, queries, ">>>");
            LOGGER.trace(getMarker(), "{}{}{}{}",
                    getLabeled("<< Query [Patch] >>"), getBlockBegin(),
                    sb.toString(), getBlockEnd());

            value = queryByQuery(page, queries);

        } catch (FieldsNotFoundException e) {
        }

        try {
            List<String> prefixes =
                    fieldsHelper.getValues("/xf:prefix", false, fields);
            value = fieldsHelper.prefixValue(value, prefixes);
        } catch (FieldsNotFoundException e) {
        }

        return value;
    }

    private void prepareData() throws DataDefNotFoundException,
            ClassNotFoundException, IOException {
        data = dataDefService.getDataTemplate(dataDefName);
        data.setDataDefId(dataDefService.getDataDef(dataDefName).getId());
        data.setDocumentId(getDocument().getId());
        LOGGER.trace(marker, "-- data template --{}{}{}{}", Util.LINE,
                marker.getName(), Util.LINE, data);
    }

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
            // xpath - not abs path
            String breakAfter = fieldsHelper
                    .getLastValue("//xf:breakAfter/@value", axis.getFields());
            noField = false;
            String value = axis.getValue();
            if (value == null) {
                String message = getLabeled(
                        "value is null, check breakAfter or query in datadef");
                throw new NullPointerException(message);
            } else {
                if (value.equals(breakAfter)) {
                    return true;
                }
            }
        } catch (FieldsNotFoundException e) {
        }
        try {
            Integer endIndex = getEndIndex(axis.getFields());
            noField = false;
            if (axis.getIndex() + 1 > endIndex) {
                return true;
            }
        } catch (FieldsNotFoundException e) {
        }
        if (noField) {
            String message = getLabeled("breakAfter or indexRange undefined");
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
            String message = getLabeled("unable to set next step input, ");
            message = Util.join(message,
                    "Document type expected but is instance of ",
                    input.getClass().getName());
            throw new StepRunException(message);
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
            throws NumberFormatException, FieldsNotFoundException {
        // xpath - not abs path
        Range<Integer> indexRange =
                fieldsHelper.getRange("//xf:indexRange/@value", fields);
        return indexRange.getMinimum();
    }

    /*
     *
     */
    protected Integer getEndIndex(final Fields fields)
            throws NumberFormatException, FieldsNotFoundException {
        // xpath - not abs path
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

    public String getBlockBegin() {
        return blockBegin;
    }

    public String getBlockEnd() {
        return blockEnd;
    }

    protected void setTraceString(final StringBuilder sb,
            final Map<String, String> strings, final String header) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        sb.append(Util.LINE);
        if (header != null) {
            sb.append(header);
            sb.append(Util.LINE);
        }

        for (String key : strings.keySet()) {
            sb.append(key);
            sb.append(" : ");
            sb.append(strings.get(key));
            sb.append(Util.LINE);
        }
    }
}
