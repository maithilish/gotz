package org.codetab.gotz.validation;

import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.util.FieldsUtil;

public final class DataDefValidator {

    private DataDef dataDef;

    public void setDataDef(final DataDef dataDef) {
        this.dataDef = dataDef;
    }

    public boolean validate() {
        boolean valid = true;
        valid = validateIndexRange();
        return valid;
    }

    private boolean validateIndexRange() {
        boolean valid = true;
        for (List<FieldsBase> fc : getAllFields()) {
            try {
                FieldsUtil.getRange(fc, "indexRange");
            } catch (NumberFormatException e) {
                valid = false;
            } catch (FieldNotFoundException e) {
            }
        }
        return valid;
    }

    private List<List<FieldsBase>> getAllFields() {
        List<List<FieldsBase>> lists = new ArrayList<>();

        lists.add(dataDef.getFields());
        for (DAxis axis : dataDef.getAxis()) {
            lists.add(axis.getFields());
            for (DMember member : axis.getMember()) {
                if (member != null) {
                    lists.add(member.getFields());
                }
            }
            DFilter filter = axis.getFilter();
            if (filter != null) {
                lists.add(filter.getFields());
            }
        }
        return lists;
    }
}
