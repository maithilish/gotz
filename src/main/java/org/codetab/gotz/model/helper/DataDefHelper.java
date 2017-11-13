package org.codetab.gotz.model.helper;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;

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
    private XFieldHelper xFieldHelper;

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
     * @throws XFieldException
     */
    public void addFact(final DataDef dataDef) throws XFieldException {
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis axis : dataDef.getAxis()) {
            if (axis.getName().equals("fact")) {
                DMember fact = new DMember();
                fact.setAxis(axis.getName());
                fact.setName("fact");
                fact.setOrder(0);
                fact.setValue(null);
                fact.setXfield(xFieldHelper.createXField());
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
     * @throws XFieldException
     */
    public void addIndexRange(final DataDef dataDef) throws XFieldException {
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                XField xField = dMember.getXfield();
                if (!xFieldHelper.isAnyDefined(xField, "//xf:indexRange/@value",
                        "//xf:breakAfter/@value")) {
                    String defaultIndexRange = "1-1";
                    Integer index = dMember.getIndex();
                    if (index != null) {
                        defaultIndexRange = index + "-" + index;
                    }
                    xFieldHelper.addElement("indexRange", defaultIndexRange,
                            xField);
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

    public List<XField> getDataDefMemberFields(final String name,
            final XField xField) throws XFieldException {
        String xpath = Util.buildString("/xf:member[@value='", name, "']");
        return xFieldHelper.split(xpath, xField);
    }

    public String getDataMemberGroup(final XField xField)
            throws XFieldException {
        String xpath = "/xf:member/xf:group";
        String group = xFieldHelper.getLastValue(xpath, xField);
        return group;
    }

    /**
     * Set default XField.
     * @param dataDef
     */
    public void addXField(final DataDef dataDef) {
        if (dataDef.getXfield() == null) {
            XField xfield = new XField();
            xfield.setName(dataDef.getName());
            xfield.setClazz(dataDef.getClass().getName());
            dataDef.setXfield(xfield);
        }
    }
}
