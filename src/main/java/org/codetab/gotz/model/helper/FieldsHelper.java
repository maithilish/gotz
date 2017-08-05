package org.codetab.gotz.model.helper;

import java.util.List;

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
     *            to clone
     * @return clone of fieldsBase
     * @see FieldsUtil#deepClone(FieldsBase)
     */
    public FieldsBase deepClone(final FieldsBase fieldsBase) {
        return FieldsUtil.deepClone(fieldsBase);
    }

    /**
     * <p>
     * Wrapper to FieldsUtil.clone() static method. Useful as test mocks.
     * @param fields
     *            list of FieldsBase to clone
     * @return clone of fieldsBase
     * @see FieldsUtil#deepClone(List)
     */
    public List<FieldsBase> deepClone(final List<FieldsBase> fields) {
        return FieldsUtil.deepClone(fields);
    }
}
