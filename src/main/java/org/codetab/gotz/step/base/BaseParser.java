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
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.Range;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.metrics.MetricsHelper;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.BreakAfterHelper;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.DataHelper;
import org.codetab.gotz.persistence.DataPersistence;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

public abstract class BaseParser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseParser.class);

    private String dataDefName;
    private Document document;
    private Data data;
    private ScriptEngine jsEngine;

    private Set<Integer[]> memberIndexSet = new HashSet<>();

    private String blockBegin;
    private String blockEnd;

    @Inject
    private DataHelper dataHelper;
    @Inject
    private BreakAfterHelper breakAfterHelper;
    @Inject
    private DataPersistence dataPersistence;
    @Inject
    private DataDefHelper dataDefHelper;
    @Inject
    private MetricsHelper metricsHelper;

    @Override
    public boolean initialize() {

        String dashes = "---------"; //$NON-NLS-1$
        blockBegin = Util.join(Util.LINE, dashes, Util.LINE);
        blockEnd = Util.join(Util.LINE, dashes, dashes);

        try {
            dataDefName = fieldsHelper
                    .getLastValue("/xf:fields/xf:task/@dataDef", getFields()); //$NON-NLS-1$
            String taskName = fieldsHelper
                    .getLastValue("/xf:fields/xf:task/@name", getFields()); //$NON-NLS-1$
            // new labels with dataDef and task name
            Labels labels = new Labels(getLabels().getName(),
                    getLabels().getGroup(), taskName, dataDefName);
            setLabels(labels);
        } catch (FieldsNotFoundException e) {
            String message = Messages.getString("BaseParser.3"); //$NON-NLS-1$
            throw new StepRunException(message, e);
        }
        return postInitialize();
    }

    protected abstract boolean postInitialize();

    /*
     * Load data from dataDef id and document id. (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#load()
     */
    @Override
    public boolean load() {
        Long dataDefId;
        try {
            dataDefId = dataDefService.getDataDef(dataDefName).getId();
        } catch (DataDefNotFoundException e) {
            String message = Messages.getString("BaseParser.4"); //$NON-NLS-1$
            throw new StepRunException(message, e);
        }
        Long documentId = getDocument().getId();
        if (documentId != null && dataDefId != null) {
            data = dataPersistence.loadData(dataDefId, documentId);
        }
        return true;
    }

    @Override
    public boolean process() {
        Counter dataParseCounter =
                metricsHelper.getCounter(this, "data", "parse");
        Counter dataReuseCounter =
                metricsHelper.getCounter(this, "data", "reuse");
        if (data == null) {
            LOGGER.info(Messages.getString("BaseParser.5"), getLabel()); //$NON-NLS-1$
            try {

                data = dataHelper.getDataTemplate(dataDefName, document.getId(),
                        getLabel());
                LOGGER.trace(getMarker(), Messages.getString("BaseParser.45"), //$NON-NLS-1$
                        Util.LINE, getLabels().getName(), Util.LINE, data);

                parse();
                setConsistent(true);
                dataParseCounter.inc();
            } catch (ClassNotFoundException | DataDefNotFoundException
                    | IOException | NumberFormatException
                    | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | ScriptException
                    | DataFormatException | FieldsException e) {
                String message = Messages.getString("BaseParser.6"); //$NON-NLS-1$
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            dataReuseCounter.inc();
            LOGGER.info(Messages.getString("BaseParser.7"), getLabel()); //$NON-NLS-1$
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
        if (dataPersistence.storeData(data, getFields())) {
            data = dataPersistence.loadData(data.getId());
            LOGGER.debug(Messages.getString("BaseParser.8"), getLabel()); //$NON-NLS-1$
        } else {
            LOGGER.debug(Messages.getString("BaseParser.9"), getLabel()); //$NON-NLS-1$
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
            String message = Messages.getString("BaseParser.10"); //$NON-NLS-1$
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
            LOGGER.debug("{}", getLabeled(Messages.getString("BaseParser.21"))); //$NON-NLS-1$ //$NON-NLS-2$
            jsEngine = dataHelper.getScriptEngine();
            if (jsEngine == null) {
                throw new CriticalException(
                        Messages.getString("BaseParser.23")); //$NON-NLS-1$
            }
        }

        LOGGER.trace(getMarker(), Messages.getString("BaseParser.11"), //$NON-NLS-1$
                Util.LINE);
        LOGGER.trace(getMarker(), "{}{}{}{}", getLabeled("scripts"), blockBegin, //$NON-NLS-1$ //$NON-NLS-2$
                scripts, blockEnd);

        jsEngine.put("configs", configService); //$NON-NLS-1$
        jsEngine.put("document", getDocument()); //$NON-NLS-1$
        String scriptStr = scripts.get("script"); //$NON-NLS-1$
        Object val = jsEngine.eval(scriptStr);
        String value = ConvertUtils.convert(val);

        LOGGER.trace(getMarker(), Messages.getString("BaseParser.17"), value); //$NON-NLS-1$
        LOGGER.trace(getMarker(), Messages.getString("BaseParser.18")); //$NON-NLS-1$
        LOGGER.trace(getMarker(), ""); //$NON-NLS-1$

        return value;
    }

    protected String getValue(final Object page, final DataDef dataDef,
            final Member member, final Axis axis)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // TODO explore whether query can be made strict so that it has
        // to return value else raise StepRunException
        StringBuilder sb = null; // to trace query strings
        String value = null;
        boolean traceEnabled = LOGGER.isTraceEnabled();
        Fields fields =
                dataDefHelper.getAxis(dataDef, axis.getName()).getFields();

        if (fields == null) {
            String message = getLabeled(Messages.getString("BaseParser.24")); //$NON-NLS-1$
            throw new StepRunException(message);
        }
        try {
            Map<String, String> scripts = new HashMap<>();
            scripts.put("script", //$NON-NLS-1$
                    fieldsHelper.getLastValue("/xf:script/@script", fields)); //$NON-NLS-1$

            sb = new StringBuilder();
            setTraceString(sb, scripts, "<<<", traceEnabled); //$NON-NLS-1$

            fieldsHelper.replaceVariables(scripts, member.getAxisMap());

            setTraceString(sb, scripts, ">>>", traceEnabled); //$NON-NLS-1$
            LOGGER.trace(getMarker(), "{}{}{}{}", //$NON-NLS-1$
                    getLabeled(Messages.getString("BaseParser.30")), //$NON-NLS-1$
                    getBlockBegin(), sb.toString(), getBlockEnd());

            value = queryByScript(scripts);

        } catch (FieldsNotFoundException e) {
        }

        try {
            Map<String, String> queries = new HashMap<>();
            queries.put("region", //$NON-NLS-1$
                    fieldsHelper.getLastValue("/xf:query/@region", fields)); //$NON-NLS-1$
            queries.put("field", //$NON-NLS-1$
                    fieldsHelper.getLastValue("/xf:query/@field", fields)); //$NON-NLS-1$
            // optional attribute
            try {
                queries.put("attribute", fieldsHelper //$NON-NLS-1$
                        .getLastValue("/xf:query/@attribute", fields)); //$NON-NLS-1$
            } catch (FieldsNotFoundException e) {
                queries.put("attribute", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            sb = new StringBuilder();
            setTraceString(sb, queries, "<<<", traceEnabled); //$NON-NLS-1$

            fieldsHelper.replaceVariables(queries, member.getAxisMap());

            setTraceString(sb, queries, ">>>", traceEnabled); //$NON-NLS-1$
            LOGGER.trace(getMarker(), "{}{}{}{}", //$NON-NLS-1$
                    getLabeled(Messages.getString("BaseParser.42")), //$NON-NLS-1$
                    getBlockBegin(), sb.toString(), getBlockEnd());

            value = queryByQuery(page, queries);

        } catch (FieldsNotFoundException e) {
        }

        try {
            List<String> prefixes =
                    fieldsHelper.getValues("/xf:prefix", false, fields); //$NON-NLS-1$
            if (value == null) {
                String message =
                        getLabeled(Messages.getString("BaseParser.44")); //$NON-NLS-1$
                throw new StepRunException(message);
            } else {
                value = fieldsHelper.prefixValue(value, prefixes);
            }
        } catch (FieldsNotFoundException e) {
        }

        return value;
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
        LOGGER.trace(getMarker(), Messages.getString("BaseParser.46"), //$NON-NLS-1$
                Util.LINE, getLabels().getName(), Util.LINE, data);
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
            int endIndex;
            try {
                endIndex = getEndIndex(axis.getFields());
            } catch (FieldsNotFoundException e) {
                endIndex = -1;
            }
            if (!breakAfterHelper.hasFinished(axis, endIndex)) {
                Integer[] nextMemberIndexes =
                        dataHelper.nextMemberIndexes(member, axisName);
                if (!dataHelper.alreadyProcessed(memberIndexSet,
                        nextMemberIndexes)) {
                    // Member newMember = Util.deepClone(Member.class, member);
                    Member newMember = dataHelper.createMember(member);
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
            String message = Util.join(Messages.getString("BaseParser.50"), //$NON-NLS-1$
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
                fieldsHelper.getRange("//xf:indexRange/@value", fields); //$NON-NLS-1$
        return indexRange.getMinimum();
    }

    /*
     *
     */
    protected Integer getEndIndex(final Fields fields)
            throws NumberFormatException, FieldsNotFoundException {
        // xpath - not abs path
        Range<Integer> indexRange =
                fieldsHelper.getRange("//xf:indexRange/@value", fields); //$NON-NLS-1$
        return indexRange.getMaximum();
    }

    /*
     *
     */
    protected String getDataDefName() {
        return dataDefName;
    }

    public String getBlockBegin() {
        return blockBegin;
    }

    public String getBlockEnd() {
        return blockEnd;
    }

    protected void setTraceString(final StringBuilder sb,
            final Map<String, String> strings, final String header,
            final boolean traceEnabled) {
        if (!traceEnabled) {
            return;
        }
        sb.append(Util.LINE);
        if (header != null) {
            sb.append(header);
            sb.append(Util.LINE);
        }

        for (String key : strings.keySet()) {
            sb.append(key);
            sb.append(" : "); //$NON-NLS-1$
            sb.append(strings.get(key));
            sb.append(Util.LINE);
        }
    }
}
