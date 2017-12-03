package org.codetab.gotz.model.helper;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.FieldsParseException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
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
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis axis : dataDef.getAxis()) {
            if (axis.getName().equals("fact")) {
                DMember fact = new DMember();
                fact.setAxis(axis.getName());
                fact.setName("fact");
                fact.setOrder(0);
                fact.setValue(null);
                fact.setFields(fieldsHelper.createFields("xf"));
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
        Validate.notNull(dataDef, "dataDef must not be null");

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
     * @throws FieldsParseException
     * @throws FieldsException
     */
    public void addIndexRange(final DataDef dataDef) throws FieldsException {
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                Fields fields = dMember.getFields();
                if (fields == null) {
                    fields = fieldsHelper.createFields("xf");
                    dMember.setFields(fields);
                }
                if (!fieldsHelper.isAnyDefined(fields, "//xf:indexRange/@value",
                        "//xf:breakAfter/@value")) {
                    String defaultIndexRange = "1-1";
                    Integer index = dMember.getIndex();
                    if (index != null) {
                        defaultIndexRange = index + "-" + index;
                    }
                    Element node =
                            fieldsHelper.addElement("indexRange", "", fields);
                    fieldsHelper.addAttribute("value", defaultIndexRange, node);
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
        Validate.notNull(dataDef, "dataDef must not be null");
        Validate.validState(configService != null, "configService is null");

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
        Validate.notNull(dataDef, "dataDef must not be null");
        Validate.notNull(axisName, "axisName must not be null");

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
        String xpath = Util.join("/xf:member[@name='", name, "']");
        return fieldsHelper.split(xpath, fields);
    }

    public String getDataMemberGroup(final Fields fields)
            throws FieldsNotFoundException {
        String xpath = "/xf:fields/xf:member/xf:group";
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
}
