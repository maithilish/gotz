package org.codetab.nscoop.validation;

import java.util.ArrayList;
import java.util.List;

import org.codetab.nscoop.exception.FieldNotFoundException;
import org.codetab.nscoop.model.DAxis;
import org.codetab.nscoop.model.DFilter;
import org.codetab.nscoop.model.DMember;
import org.codetab.nscoop.model.DataDef;
import org.codetab.nscoop.model.FieldsBase;
import org.codetab.nscoop.util.FieldsUtil;

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
