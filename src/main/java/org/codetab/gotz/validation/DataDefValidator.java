package org.codetab.gotz.validation;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;

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
    private XFieldHelper xFieldHelper;

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
        List<XField> xFields = getAllXFields(dataDef);
        for (XField xField : xFields) {
            try {
                if (xField != null) {
                    xFieldHelper.getRange("//xf:indexRange/@value", xField);
                }
            } catch (NumberFormatException e) {
                valid = false;
            } catch (XFieldException e) {
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
    private List<XField> getAllXFields(final DataDef dataDef) {
        List<XField> lists = new ArrayList<>();

        lists.add(dataDef.getXfield());
        for (DAxis axis : dataDef.getAxis()) {
            lists.add(axis.getXfield());
            for (DMember member : axis.getMember()) {
                lists.add(member.getXfield());
            }
            DFilter filter = axis.getFilter();
            if (filter != null) {
                lists.add(filter.getXfield());
            }
        }
        return lists;
    }
}
