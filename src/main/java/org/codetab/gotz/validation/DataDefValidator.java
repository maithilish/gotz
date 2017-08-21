package org.codetab.gotz.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.util.FieldsUtil;

/**
 * <p>
 * DateDef validator. DataDef XML is validated with schema but is it not
 * possible to validate elements such as indexRange through schema validation
 * and they are validated here.
 * @author Maithilish
 *
 */
public class DataDefValidator {

    /**
     * <p>
     * Validate datadef. Elements such as indexRange which are not validated at
     * schema validation stage are validated by this method.
     * @param dataDef
     *            datadef to validate, not null
     * @return true, if valid
     */
    public boolean validate(final DataDef dataDef) {
        Validate.notNull(dataDef, "dataDef must not be null");
        boolean valid = true;
        valid = validateIndexRange(dataDef);
        return valid;
    }

    /**
     * <p>
     * Validate indexRange field. Checks indexRange field at all levels -
     * dataDef, dAxis, dMember and dFilter levels.
     * @param dataDef
     *            dataDef which contains indexRange field
     * @return true, if indexRange fields are valid
     */
    private boolean validateIndexRange(final DataDef dataDef) {
        boolean valid = true;
        for (List<FieldsBase> fc : getAllFields(dataDef)) {
            try {
                FieldsUtil.getRange(fc, "indexRange");
            } catch (NumberFormatException e) {
                valid = false;
            } catch (FieldNotFoundException e) {
            }
        }
        return valid;
    }

    /**
     * <p>
     * Get indexRange fields from all levels - dataDef, dAxis, dMember and
     * dFilter levels.
     * @param dataDef
     *            which contains indexRange fields
     * @return list of indexRange fields
     */
    private List<List<FieldsBase>> getAllFields(final DataDef dataDef) {
        List<List<FieldsBase>> lists = new ArrayList<>();

        lists.add(dataDef.getFields());
        for (DAxis axis : dataDef.getAxis()) {
            lists.add(axis.getFields());
            for (DMember member : axis.getMember()) {
                lists.add(member.getFields());
            }
            DFilter filter = axis.getFilter();
            if (filter != null) {
                lists.add(filter.getFields());
            }
        }
        return lists;
    }
}
