package org.codetab.gotz.model.helper;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.util.FieldsUtil;

/**
 * <p>
 * Helper and wrapper methods to handle FieldsBase.
 * @author Maithilish
 *
 */
public class FieldsHelper {

    /**
     * <p>
     * Wrapper to FieldsUtil.clone() static method. Useful as test mocks.
     * @param fieldsBase
     *            to clone, not null
     * @return clone of fieldsBase
     * @see FieldsUtil#deepClone(FieldsBase)
     */
    public FieldsBase deepClone(final FieldsBase fieldsBase) {
        Validate.notNull(fieldsBase, "fieldsBase must not be null");
        return FieldsUtil.deepClone(fieldsBase);
    }

    /**
     * <p>
     * Wrapper to FieldsUtil.clone() static method. Useful as test mocks.
     * @param fields
     *            list of FieldsBase to clone, not null
     * @return clone of fieldsBase
     * @see FieldsUtil#deepClone(List)
     */
    public List<FieldsBase> deepClone(final List<FieldsBase> fields) {
        Validate.notNull(fields, "fields must not be null");
        return FieldsUtil.deepClone(fields);
    }
}
