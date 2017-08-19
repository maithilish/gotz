package org.codetab.gotz.model.helper;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.FieldsUtil;

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
     */
    public void addFact(final DataDef dataDef) {
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis axis : dataDef.getAxis()) {
            if (axis.getName().equals("fact")) {
                DMember fact = new DMember();
                fact.setAxis(axis.getName());
                fact.setName("fact");
                fact.setOrder(0);
                fact.setValue(null);
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
     */
    public void addIndexRange(final DataDef dataDef) {
        Validate.notNull(dataDef, "dataDef must not be null");

        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                if (!FieldsUtil.isAnyDefined(dMember.getFields(), "indexRange",
                        "breakAfter")) {
                    Field field = new Field();
                    field.setName("indexRange");
                    Integer index = dMember.getIndex();
                    if (index == null) {
                        field.setValue("1-1");
                    } else {
                        field.setValue(index + "-" + index);
                    }
                    dMember.getFields().add(field);
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
}
