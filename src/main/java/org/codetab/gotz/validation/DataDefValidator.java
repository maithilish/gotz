package org.codetab.gotz.validation;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;

/**
 * <p>
 * DateDef validator. DataDef XML is validated with schema but is it not
 * possible to validate elements such as indexRange through schema validation
 * and they are validated here.
 * @author Maithilish
 *
 */
public class DataDefValidator {

    @Inject
    private FieldsHelper fieldsHelper;

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
        List<Fields> xFields = getAllXFields(dataDef);
        for (Fields fields : xFields) {
            try {
                if (fields != null) {
                    fieldsHelper.getRange("//xf:indexRange/@value", fields);
                }
            } catch (NumberFormatException e) {
                valid = false;
            } catch (FieldsException e) {
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
    private List<Fields> getAllXFields(final DataDef dataDef) {
        List<Fields> lists = new ArrayList<>();

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
