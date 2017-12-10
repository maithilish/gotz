package org.codetab.gotz.validation;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.util.Util;

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
     * @throws InvalidDataDefException
     */
    public boolean validate(final DataDef dataDef)
            throws InvalidDataDefException {
        Validate.notNull(dataDef, "dataDef must not be null");

        validateIndexRange(dataDef);
        validateQueries(dataDef);
        validateFactQueries(dataDef);
        return true;
    }

    /**
     * Script or query is optional in axis (except fact). In case either script
     * or query is defined then attribute [script] or [region,field] should not
     * be empty.
     * @param dataDef
     * @return
     * @throws InvalidDataDefException
     */
    private boolean validateQueries(final DataDef dataDef)
            throws InvalidDataDefException {
        String message = Util.join(
                "missing script or query (region and field) in axis of [",
                dataDef.getName(), "]");
        for (DAxis axis : dataDef.getAxis()) {
            boolean valid = true;

            Fields fields = axis.getFields();
            if (fields == null) {
                continue;
            }

            if (fieldsHelper.isDefined("/xf:script", true, fields)) {
                try {
                    fieldsHelper.getLastValue("/xf:script/@script", fields);
                } catch (FieldsNotFoundException e) {
                    valid = false;
                }
            }

            if (fieldsHelper.isDefined("/xf:query", true, fields)) {
                try {
                    fieldsHelper.getLastValue("/xf:query/@region", fields);
                    fieldsHelper.getLastValue("/xf:query/@field", fields);
                } catch (FieldsNotFoundException e) {
                    valid = false;
                }
            }

            if (!valid) {
                throw new InvalidDataDefException(message);
            }
        }
        return true;
    }

    /**
     * Script or query is mandatory for fact axis and should be properly
     * defined.
     * @param dataDef
     * @return
     * @throws InvalidDataDefException
     */
    private boolean validateFactQueries(final DataDef dataDef)
            throws InvalidDataDefException {
        String message = Util.join(
                "missing script or query (region and field) in fact axis of [",
                dataDef.getName(), "]");
        for (DAxis axis : dataDef.getAxis()) {
            if (!axis.getName().equalsIgnoreCase(AxisName.FACT.toString())) {
                continue;
            }

            Fields fields = axis.getFields();

            if (fields == null) {
                throw new InvalidDataDefException(message);
            }

            boolean valid = false;

            try {
                fieldsHelper.getLastValue("/xf:script/@script", fields);
                valid = true;
            } catch (FieldsNotFoundException e) {

            }

            try {
                fieldsHelper.getLastValue("/xf:query/@region", fields);
                fieldsHelper.getLastValue("/xf:query/@field", fields);
                valid = true;
            } catch (FieldsNotFoundException e) {
            }

            if (!valid) {
                throw new InvalidDataDefException(message);
            }
        }
        return true;
    }

    /**
     * <p>
     * Validate indexRange field. Checks indexRange field at all levels -
     * dataDef, dAxis, dMember and dFilter levels.
     * @param dataDef
     *            dataDef which contains indexRange field
     * @return true, if indexRange fields are valid
     * @throws InvalidDataDefException
     */
    private boolean validateIndexRange(final DataDef dataDef)
            throws InvalidDataDefException {
        boolean valid = true;
        List<Fields> fieldsList = getAllFields(dataDef);
        for (Fields fields : fieldsList) {
            try {
                if (fields != null) {
                    // xpath - not abs path
                    fieldsHelper.getRange("//xf:indexRange/@value", fields);
                }
            } catch (NumberFormatException e) {
                String message = Util.join("invalid indexRange in [",
                        dataDef.getName(), "]");
                throw new InvalidDataDefException(message, e);
            } catch (FieldsNotFoundException e) {
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
    private List<Fields> getAllFields(final DataDef dataDef) {
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
