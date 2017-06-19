package org.codetab.gotz.helper;

import javax.inject.Inject;

import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.FieldsUtil;

public class DataDefDefaults {

    @Inject
    BeanService beanService;
    @Inject
    ConfigService configService;

    public void addFact(final DataDef dataDef) {
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

    public void setOrder(final DataDef dataDef) {
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

    public void addIndexRange(final DataDef dataDef) {
        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                if (!FieldsUtil.isAnyFieldDefined(dMember.getFields(), "indexRange",
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

    public void setDates(final DataDef dataDef) {
        dataDef.setFromDate(configService.getRunDateTime());
        dataDef.setToDate(configService.getHighDate());
    }

}
