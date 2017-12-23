package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.w3c.dom.Element;

/**
 * <p>
 * DataDef helper.
 * @author Maithilish
 *
 */
public class DataDefHelper {

    /**
     * Config Service.
     */
    @Inject
    private ConfigService configService;
    @Inject
    private FieldsHelper fieldsHelper;

    /**
     * <p>
     * Private constructor.
     */
    @Inject
    private DataDefHelper() {
    }

    /**
     * <p>
     * For all DAxis of datadef, adds default fact.
     * @param dataDef
     *            datadef, not null
     * @throws FieldsException
     */
    public void addFact(final DataDef dataDef) throws FieldsException {
        Validate.notNull(dataDef, Messages.getString("DataDefHelper.0")); //$NON-NLS-1$

        for (DAxis axis : dataDef.getAxis()) {
            if (axis.getName().equals("fact")) { //$NON-NLS-1$
                DMember fact = new DMember();
                fact.setAxis(axis.getName());
                fact.setName("fact"); //$NON-NLS-1$
                fact.setOrder(0);
                fact.setValue(null);
                fact.setFields(fieldsHelper.createFields("xf")); //$NON-NLS-1$
                axis.getMember().add(fact);
            }
        }
    }

    /**
     * <p>
     * For all DMembers of all DAxis of datadef, sets order by incrementing the
     * previous DMembers' order and also, sets DMember axis.
     * @param dataDef
     *            datadef, not null
     */
    public void setOrder(final DataDef dataDef) {
        Validate.notNull(dataDef, Messages.getString("DataDefHelper.4")); //$NON-NLS-1$

        for (DAxis axis : dataDef.getAxis()) {
            // set member's axis name and order
            int i = 0;
            for (DMember member : axis.getMember()) {
                member.setAxis(axis.getName());
                if (member.getOrder() == null) {
                    member.setOrder(i++);
                }
            }
        }
    }

    /**
     * <p>
     * For all DMembers of all DAxis of datadef, adds indexRange {@see Field} if
     * neither indexRange nor breakAfter field is defined. If member index field
     * is null then indexRange is set as 1-1 else it is set as index-index (such
     * as 7-7)
     * @param dataDef
     *            datadef, not null
     * @throws org.codetab.gotz.exception.FieldsParseException
     * @throws FieldsException
     */
    public void addIndexRange(final DataDef dataDef) throws FieldsException {
        Validate.notNull(dataDef, Messages.getString("DataDefHelper.5")); //$NON-NLS-1$

        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                Fields fields = dMember.getFields();
                if (fields == null) {
                    fields = fieldsHelper.createFields("xf"); //$NON-NLS-1$
                    dMember.setFields(fields);
                }
                // xpath - not abs path
                if (!fieldsHelper.isAnyDefined(fields, "//xf:indexRange/@value", //$NON-NLS-1$
                        "//xf:breakAfter/@value")) { //$NON-NLS-1$
                    String defaultIndexRange = "1-1"; //$NON-NLS-1$
                    Integer index = dMember.getIndex();
                    if (index != null) {
                        defaultIndexRange = index + "-" + index; //$NON-NLS-1$
                    }
                    Element node =
                            fieldsHelper.addElement("indexRange", "", fields); //$NON-NLS-1$ //$NON-NLS-2$
                    fieldsHelper.addAttribute("value", defaultIndexRange, node); //$NON-NLS-1$
                }

            }
        }
    }

    /**
     * <p>
     * Set datadef dates. Sets fromDate to runDateTime and toDate to
     * gotz.highDate
     * @param dataDef
     *            datadef, not null
     */
    public void setDates(final DataDef dataDef) {
        Validate.notNull(dataDef, Messages.getString("DataDefHelper.14")); //$NON-NLS-1$
        Validate.validState(configService != null,
                Messages.getString("DataDefHelper.15")); //$NON-NLS-1$

        dataDef.setFromDate(configService.getRunDateTime());
        dataDef.setToDate(configService.getHighDate());
    }

    /**
     * <p>
     * Get DAxis from datadef.
     * @param dataDef
     *            datadef, not null
     * @param axisName
     *            axis name, not null
     * @return DAxis
     */
    public DAxis getAxis(final DataDef dataDef, final AxisName axisName) {
        Validate.notNull(dataDef, Messages.getString("DataDefHelper.16")); //$NON-NLS-1$
        Validate.notNull(axisName, Messages.getString("DataDefHelper.17")); //$NON-NLS-1$

        String axisNameStr = axisName.toString();
        for (DAxis dAxis : dataDef.getAxis()) {
            if (dAxis.getName().equalsIgnoreCase(axisNameStr)) {
                return dAxis;
            }
        }
        return null;
    }

    public List<Fields> getDataDefMemberFields(final String name,
            final Fields fields) throws FieldsException {
        // xpath - not abs path
        String xpath = Util.join("/xf:member[@name='", name, "']"); //$NON-NLS-1$ //$NON-NLS-2$
        return fieldsHelper.split(xpath, fields);
    }

    public String getDataMemberGroup(final Fields fields)
            throws FieldsNotFoundException {
        String xpath = "/xf:fields/xf:member/xf:group"; //$NON-NLS-1$
        String group = fieldsHelper.getLastValue(xpath, fields);
        return group;
    }

    /**
     * Set default Fields.
     * @param dataDef
     * @throws FieldsException
     */
    public void addFields(final DataDef dataDef) throws FieldsException {
        if (dataDef.getFields() == null) {
            Fields fields = new Fields();
            fields.setName(dataDef.getName());
            fields.setClazz(dataDef.getClass().getName());
            dataDef.setFields(fields);
        }
    }

    public synchronized Set<Set<DMember>> generateMemberSets(
            final DataDef dataDef) throws ClassNotFoundException, IOException {
        int axesSize = dataDef.getAxis().size();
        Set<?>[] memberSets = new HashSet<?>[axesSize];
        for (int i = 0; i < axesSize; i++) {
            Set<DMember> members = dataDef.getAxis().get(i).getMember();
            memberSets[i] = members;
        }
        try {
            Set<Set<Object>> cartesianSet = Util.cartesianProduct(memberSets);
            Set<Set<DMember>> dataDefMemberSets = new HashSet<Set<DMember>>();
            for (Set<?> set : cartesianSet) {
                /*
                 * memberSet array contains only Set<Member> as it is populated
                 * by getMemberSet method Hence safe to ignore the warning
                 */
                @SuppressWarnings("unchecked")
                Set<DMember> memberSet = (Set<DMember>) set;
                dataDefMemberSets.add(memberSet);
            }
            return dataDefMemberSets;
        } catch (IllegalStateException e) {
            String message = Util.join(Messages.getString("DataDefHelper.21"), //$NON-NLS-1$
                    dataDef.getName(), "]", //$NON-NLS-1$
                    Messages.getString("DataDefHelper.23")); //$NON-NLS-1$
            throw new IllegalStateException(message, e);
        }
    }

    public Data createDataTemplate(final DataDef dataDef,
            final Set<Set<DMember>> memberSets) {
        Data data = new Data();
        data.setDataDef(dataDef.getName());
        for (Set<DMember> members : memberSets) {
            Member dataMember = new Member();
            dataMember.setName(""); // there is no name for member //$NON-NLS-1$
            dataMember.setFields(new Fields());

            // add axis and its fields
            for (DMember dMember : members) {
                Axis axis = createAxis(dMember);
                dataMember.addAxis(axis);
                try {
                    // fields from DMember level are added in createAxis()
                    // fields from datadef level are added here
                    List<Fields> fieldsList = getDataDefMemberFields(
                            dMember.getName(), dataDef.getFields());
                    for (Fields fields : fieldsList) {
                        dataMember.getFields().getNodes()
                                .addAll(fields.getNodes());
                    }
                } catch (FieldsException e) {
                }
            }
            try {
                String group = getDataMemberGroup(dataMember.getFields());
                dataMember.setGroup(group);
            } catch (FieldsNotFoundException e) {
                // TODO throw critical error
            }
            data.addMember(dataMember);
        }
        return data;
    }

    private Axis createAxis(final DMember dMember) {
        Axis axis = new Axis();
        AxisName axisName = AxisName.valueOf(dMember.getAxis().toUpperCase());
        axis.setName(axisName);
        axis.setOrder(dMember.getOrder());
        axis.setIndex(dMember.getIndex());
        axis.setMatch(dMember.getMatch());
        axis.setValue(dMember.getValue());
        // fields from DMember level
        axis.setFields(dMember.getFields());
        return axis;
    }

    public String getDataStructureTrace(final String dataDefName,
            final Data data) {
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("DataDef [name="); //$NON-NLS-1$
        sb.append(data.getDataDef());
        sb.append(Messages.getString("DataDefHelper.26")); //$NON-NLS-1$
        sb.append(line);
        sb.append(line);
        Collections.sort(data.getMembers(), new RowComparator());
        Collections.sort(data.getMembers(), new ColComparator());
        for (Member member : data.getMembers()) {
            sb.append("Member ["); //$NON-NLS-1$
            sb.append(member.getFields());
            sb.append(line);
            List<Axis> axes = new ArrayList<Axis>(member.getAxes());
            Collections.sort(axes);
            for (Axis axis : axes) {
                sb.append(axis.toString());
                sb.append(line);
            }
            sb.append(line);
        }
        return sb.toString();
    }

    public DataDef cloneDataDef(final DataDef dataDef) {
        return SerializationUtils.clone(dataDef);
    }

}
