package org.codetab.gotz.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.iterator.FieldsIterator;

/**
 * <p>
 * Utility methods for FieldsBase. Model classes hold List<FieldsBase> and
 * FieldsBase may be Field or Fields, For convince, List<FieldsBase> is called
 * as fields instead of fieldsBaseList or fieldsBases.
 * @author Maithilish
 *
 */
public final class FieldsUtil {

    /**
     * <p>
     * private constructor.
     */
    private FieldsUtil() {
    }

    // create methods

    /**
     * <p>
     * Create field.
     * @param name
     *            name
     * @param value
     *            value
     * @return field
     */
    public static Field createField(final String name, final String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    /**
     * <p>
     * Create list of fieldsBase.
     * @param fb
     *            fieldsBase to add
     * @return list of fieldsBase
     */
    public static List<FieldsBase> asList(final FieldsBase fb) {
        List<FieldsBase> list = new ArrayList<>();
        list.add(fb);
        return list;
    }

    /**
     * <p>
     * Deep clone list of FieldsBase.
     * @param fields
     *            list of fieldsBase to clone
     * @return cloned list
     */
    public static List<FieldsBase> deepClone(final List<FieldsBase> fields) {
        List<FieldsBase> list = new ArrayList<>();
        for (FieldsBase fb : fields) {
            list.add(deepClone(fb));
        }
        return list;
    }

    /**
     * <p>
     * Deep clone FieldsBase.
     * @param fieldsBase
     *            to clone
     * @return cloned FieldsBase
     */
    public static FieldsBase deepClone(final FieldsBase fieldsBase) {
        // TODO migrate to manual deep clone
        return SerializationUtils.clone(fieldsBase);
    }

    // get methods

    /**
     * <p>
     * Get first matching FieldsBase value.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return value
     * @throws FieldNotFoundException
     *             if no such field
     */
    public static String getValue(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        if (fields == null) {
            throw new FieldNotFoundException(name);
        }
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase fb = ite.next();
            if (fb.getName().equals(name)) {
                return fb.getValue();
            }
        }
        throw new FieldNotFoundException(name);
    }

    /**
     * <p>
     * Get value as Range.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return range
     * @throws FieldNotFoundException
     *             if no such field
     * @throws NumberFormatException
     *             value is not range or minimum is greater than maximum
     */
    public static Range<Integer> getRange(final List<FieldsBase> fields,
            final String name)
            throws FieldNotFoundException, NumberFormatException {
        String value = FieldsUtil.getValue(fields, name);

        if (value.startsWith("-")) {
            NumberFormatException e =
                    new NumberFormatException("Invalid Range " + value);
            throw e;
        }
        String[] tokens = StringUtils.split(value, '-');
        if (tokens.length < 1 || tokens.length > 2) {
            NumberFormatException e =
                    new NumberFormatException("Invalid Range " + value);
            throw e;
        }
        Integer min = 0, max = 0;
        if (tokens.length == 1) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[0]);
        }
        if (tokens.length == 2) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[1]);

        }
        if (min > max) {
            NumberFormatException e = new NumberFormatException(
                    "Invalid Range [min > max] " + value);
            throw e;
        }
        return Range.between(min, max);
    }

    /**
     * <p>
     * Get the first field by name which is instance of Field.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return field
     * @throws FieldNotFoundException
     *             if no such field
     */
    public static Field getField(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase fb = ite.next();
            if (fb instanceof Field && fb.getName().equals(name)) {
                return (Field) fb;
            }
        }
        throw new FieldNotFoundException("Name [" + name + "]");
    }

    /**
     * <p>
     * Get the first fields by name which is instance of Fields.
     * @param fields
     *            list
     * @param name
     *            fields name
     * @return fields
     * @throws FieldNotFoundException
     *             if no such fields
     */
    public static Fields getFields(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase fb = ite.next();
            if (fb instanceof Fields && fb.getName().equals(name)) {
                return (Fields) fb;
            }
        }
        throw new FieldNotFoundException("Name [" + name + "]");
    }

    // filter methods

    /**
     * <p>
     * Filter by name.
     * @param fields
     *            list
     * @param name
     *            name to match
     * @return list of filtered items
     * @throws FieldNotFoundException
     *             if no matching FieldsBase
     */
    public static List<FieldsBase> filterByName(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        List<FieldsBase> list = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (StringUtils.equals(f.getName(), name)) {
                list.add(f);
            }
        }
        if (list.size() == 0) {
            throw new FieldNotFoundException("name [" + name + "]");
        }
        return list;
    }

    /**
     * <p>
     * Filter by name and value.
     * @param fields
     *            list
     * @param name
     *            name to match
     * @param value
     *            value to match
     * @return list of filtered items
     * @throws FieldNotFoundException
     *             if no matching FieldsBase
     */
    public static List<FieldsBase> filterByValue(final List<FieldsBase> fields,
            final String name, final String value)
            throws FieldNotFoundException {
        List<FieldsBase> list = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (StringUtils.equals(f.getName(), name)
                    && StringUtils.equals(f.getValue(), value)) {
                list.add(f);
            }
        }
        if (list.size() == 0) {
            throw new FieldNotFoundException(
                    "name [" + name + "] value [" + value + "]");
        }
        return list;
    }

    /**
     * <p>
     * Filter by group and name. It first filters by group and then by name.
     * @param fields
     *            list
     * @param group
     *            group name
     * @param name
     *            name
     * @return list of filtered items.
     * @throws FieldNotFoundException
     *             if no matching FieldsBase
     */
    public static List<FieldsBase> filterByGroupName(
            final List<FieldsBase> fields, final String group,
            final String name) throws FieldNotFoundException {
        List<FieldsBase> groupFields = filterByGroup(fields, group);
        return filterByName(groupFields, name);
    }

    /**
     * <p>
     * Filter children of Fields by name. Children of fields are
     * Fields.getFields().
     * @param fields
     *            list
     * @param name
     *            name
     * @return list of filtered items.
     * @throws FieldNotFoundException
     *             if no matching FieldsBase
     */

    public static List<FieldsBase> filterChildrenByName(
            final List<FieldsBase> fields, final String name)
            throws FieldNotFoundException {
        List<FieldsBase> list = new ArrayList<>();
        for (FieldsBase fb : fields) {
            if (fb instanceof Fields) {
                for (FieldsBase f : ((Fields) fb).getFields()) {
                    if (StringUtils.equals(f.getName(), name)) {
                        list.add(f);
                    }
                }
            }
        }
        if (list.size() == 0) {
            throw new FieldNotFoundException("name [" + name + "]");
        }
        return list;
    }

    /**
     * <p>
     * Filter items where name == "group" and value == group.
     * @param fields
     *            list
     * @param group
     *            group name (compared with value)
     * @return list of filtered items.
     * @throws FieldNotFoundException
     *             if no matching items
     */
    public static List<FieldsBase> filterByGroup(final List<FieldsBase> fields,
            final String group) throws FieldNotFoundException {
        List<FieldsBase> groupFields = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (StringUtils.equals(f.getName(), "group")
                    && StringUtils.equals(f.getValue(), group)) {
                groupFields.add(f);
            }
        }
        if (groupFields.size() == 0) {
            throw new FieldNotFoundException(
                    "name [group] value [" + group + "]");
        }
        return groupFields;
    }

    /**
     * <p>
     * Filter items where name == "group" and value == group. Only Fields are
     * included and returned as List<Fields>.
     * @param fields
     *            list
     * @param group
     *            group name (compared with value)
     * @return filtered items as list of fields
     * @throws FieldNotFoundException
     *             if no matching items
     */
    public static List<Fields> filterByGroupAsFields(
            final List<FieldsBase> fields, final String group)
            throws FieldNotFoundException {
        List<Fields> groupFields = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Fields) {
                if (StringUtils.equals(f.getName(), "group")
                        && StringUtils.equals(f.getValue(), group)) {
                    groupFields.add((Fields) f);
                }
            }
        }
        if (groupFields.size() == 0) {
            throw new FieldNotFoundException(
                    "name [group] value [" + group + "]");
        }
        return groupFields;
    }

    // get modified value methods

    /**
     * <p>
     * From the input list, Field values are concated in reverse order and
     * suffixed with input string.
     * <p>
     * Example : for suffix xyz and two fields with value foo and bar, it
     * returns string barfooxyz.
     * @param fields
     *            input list
     * @param str
     *            string to prefix
     * @return string prefixed concated values
     */
    public static String suffixValue(final List<FieldsBase> fields,
            final String str) {
        String suffixedValues = str;
        Iterator<FieldsBase> ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase field = ite.next();
            if (field instanceof Field) {
                suffixedValues = field.getValue() + suffixedValues;
            }
        }
        return suffixedValues;
    }

    /**
     * <p>
     * Replace replaceable variables in Field value with value returned by a
     * getter. Fields are ignored.
     * <p>
     * Replaces %{objName.getterMethodName} by the value returned by the getter
     * method of the object.
     * <p>
     * examples : %{col.value} is replaced with value returned by getValue() of
     * col axis and %{row.match} is replaced with value returned by getMatch()
     * of row axis.
     * @param fields
     *            list
     * @param map
     *            map of object name and object
     * @return patched list of field (fields are ignored and not returned)
     * @throws IllegalAccessException
     *             on error
     * @throws InvocationTargetException
     *             on error
     * @throws NoSuchMethodException
     *             on error
     */
    public static List<FieldsBase> replaceVariables(
            final List<FieldsBase> fields, final Map<String, ?> map)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        // TODO provide some examples and full explanation in javadoc

        Iterator<FieldsBase> ite = new FieldsIterator(fields);
        List<FieldsBase> patchedFields = new ArrayList<>();
        while (ite.hasNext()) {
            FieldsBase field = ite.next();
            if (field instanceof Field) {
                String str = field.getValue();
                Map<String, String> valueMap = getValueMap(str, map);
                StrSubstitutor ss = new StrSubstitutor(valueMap);
                ss.setVariablePrefix("%{");
                ss.setVariableSuffix("}");
                ss.setEscapeChar('%');
                String patchedStr = ss.replace(str);
                Field patchedField = new Field();
                patchedField.setName(field.getName());
                patchedField.setValue(patchedStr);
                patchedFields.add(patchedField);
            }
        }
        return patchedFields;
    }

    /**
     * <p>
     * Get value map.
     * @param str
     *            string to parse
     * @param map
     *            axis map
     * @return axis value map
     * @throws IllegalAccessException
     *             on error
     * @throws InvocationTargetException
     *             on error
     * @throws NoSuchMethodException
     *             on error
     */
    private static Map<String, String> getValueMap(final String str,
            final Map<String, ?> map) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String[] keys = StringUtils.substringsBetween(str, "%{", "}");
        if (keys == null) {
            return null;
        }
        Map<String, String> valueMap = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split("\\.");
            String objKey = parts[0];
            String property = parts[1];
            Object obj = map.get(objKey.toUpperCase());
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(obj, property);
            valueMap.put(key, ConvertUtils.convert(o));
        }
        return valueMap;
    }

    // boolean methods

    /**
     * <p>
     * Is List contains field or fields with matching name and value.
     * @param fields
     *            list
     * @param name
     *            field name
     * @param value
     *            field value
     * @return true if matching item exists
     */
    public static boolean contains(final List<FieldsBase> fields,
            final String name, final String value) {
        if (fields == null) {
            return false;
        }
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase fb = ite.next();
            if (fb.getName().equals(name) && fb.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Is Fields contains field or fields with matching name and value.
     * @param fields
     *            fields
     * @param name
     *            field name
     * @param value
     *            field value
     * @return true if matching item exists
     */
    public static boolean contains(final Fields fields, final String name,
            final String value) {
        if (fields == null) {
            return false;
        }
        Iterator<FieldsBase> ite = fields.iterator();
        while (ite.hasNext()) {
            FieldsBase fb = ite.next();
            if (fb.getName().equals(name) && fb.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Is value of a field is true.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return if value of the field is true
     * @throws FieldNotFoundException
     *             if no such field
     */
    public static boolean isTrue(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsBase fb = getField(fields, name);
        if (StringUtils.equalsIgnoreCase(fb.getValue(), "true")) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Is value of a field in group is true.
     * @param fields
     *            list
     * @param group
     *            group name
     * @param name
     *            field name
     * @return if value of field in the group is true
     * @throws FieldNotFoundException
     *             if no such field
     */
    public static boolean isTrue(final List<FieldsBase> fields,
            final String group, final String name)
            throws FieldNotFoundException {
        List<FieldsBase> fg = filterByGroup(fields, group);
        return isTrue(fg, name);
    }

    /**
     * <p>
     * Is field by name is defined.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return true if named field exists
     */
    public static boolean isDefined(final List<FieldsBase> fields,
            final String name) {
        try {
            getValue(fields, name);
            return true;
        } catch (FieldNotFoundException e) {
            return false;
        }
    }

    /**
     * <p>
     * Is any one field from list of names is defined.
     * @param fields
     *            list
     * @param names
     *            list of names
     * @return true if any one named field exists
     */
    public static boolean isAnyDefined(final List<FieldsBase> fields,
            final String... names) {
        for (String name : names) {
            try {
                getValue(fields, name);
                return true;
            } catch (FieldNotFoundException e) {
            }
        }
        return false;
    }

    /**
     * <p>
     * Is all fields from list of names is defined.
     * @param fields
     *            list
     * @param names
     *            list of names
     * @return true if all named field exists
     */
    public static boolean isAllDefined(final List<FieldsBase> fields,
            final String... names) {
        for (String name : names) {
            try {
                getValue(fields, name);
            } catch (FieldNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    // miscellaneous methods

    /**
     * <p>
     * Iterate over list of fields and count of number of fields.
     * @param fields
     *            list
     * @return count
     */
    public static int countField(final List<FieldsBase> fields) {
        int count = 0;
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field) {
                count++;
            }
        }
        return count;
    }

}
