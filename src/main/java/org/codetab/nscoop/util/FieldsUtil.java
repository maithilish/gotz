package org.codetab.nscoop.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.codetab.nscoop.exception.FieldNotFoundException;
import org.codetab.nscoop.model.Field;
import org.codetab.nscoop.model.Fields;
import org.codetab.nscoop.model.FieldsBase;

public final class FieldsUtil {

    private FieldsUtil() {
    }

    /*
     * models hold List<FieldsBase> and FieldsBase may be Field or Fields for convince
     * List<FieldsBase> is called as fields instead of fieldsBaseList or fieldsBases
     */

    // return first matching field value
    public static String getValue(final List<FieldsBase> fields, final String name)
            throws FieldNotFoundException {
        if (fields == null) {
            throw new FieldNotFoundException(name);
        }
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field && f.getName().equals(name)) {
                return f.getValue();
            }
        }
        throw new FieldNotFoundException(name);
    }

    public static String getValue(final FieldsBase fields, final String name)
            throws FieldNotFoundException {
        Iterator<FieldsBase> ite = fields.iterator();
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field && f.getName().equals(name)) {
                return f.getValue();
            }
        }
        throw new FieldNotFoundException(name);
    }

    public static int getIntValue(final List<FieldsBase> fields, final String name)
            throws FieldNotFoundException {
        return Integer.parseInt(getValue(fields, name));
    }

    public static Range<Integer> getRange(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException, NumberFormatException {
        String value = getValue(fields, name);
        String[] tokens = StringUtils.split(value, '-');
        if (tokens.length < 1 || tokens.length > 2) {
            NumberFormatException e = new NumberFormatException("Invalid Range " + value);
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

    public static boolean isFieldTrue(final List<FieldsBase> fields, final String group,
            final String name) throws FieldNotFoundException {
        List<FieldsBase> fc = FieldsUtil.getGroupFields(fields, group);
        if (StringUtils.equalsIgnoreCase(getValue(fc, name), "true")) {
            return true;
        }
        return false;
    }

    public static boolean isFieldTrue(final List<FieldsBase> fields, final String name)
            throws FieldNotFoundException {
        FieldsBase field = getField(fields, name);
        if (StringUtils.equalsIgnoreCase(field.getValue(), "true")) {
            return true;
        }
        return false;
    }

    public static boolean isAnyFieldDefined(final List<FieldsBase> fields,
            final String... names) {
        for (String name : names) {
            try {
                FieldsUtil.getValue(fields, name);
                return true;
            } catch (FieldNotFoundException e) {
            }
        }
        return false;
    }

    public static boolean isAllFieldsDefined(final List<FieldsBase> fields,
            final String... names) {
        boolean allFieldsDefined = true;
        for (String name : names) {
            try {
                FieldsUtil.getValue(fields, name);
            } catch (FieldNotFoundException e) {
                allFieldsDefined = false;
            }
        }
        return allFieldsDefined;
    }

    public static Field getField(final List<FieldsBase> fields, final String name)
            throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field && f.getName().equals(name)) {
                return (Field) f;
            }
        }
        throw new FieldNotFoundException("Name [" + name + "]");
    }

    public static List<FieldsBase> getFieldList(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        List<FieldsBase> list = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field && f.getName().equals(name)) {
                list.add(f);
            }
        }
        if (list.size() == 0) {
            throw new FieldNotFoundException("Name [" + name + "]");
        }
        return list;
    }

    public static List<FieldsBase> getFieldList(final List<FieldsBase> fields)
            throws FieldNotFoundException {
        List<FieldsBase> list = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field) {
                list.add(f);
            }
        }
        return list;
    }

    public static List<FieldsBase> getGroupFields(final List<FieldsBase> fields,
            final String group) throws FieldNotFoundException {
        List<FieldsBase> groupFields = new ArrayList<>();
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Fields) {
                if (StringUtils.equals(f.getName(), "group")
                        && StringUtils.equals(f.getValue(), group)) {
                    groupFields.add(f);
                }
            }
        }
        if (groupFields.size() == 0) {
            throw new FieldNotFoundException("name [group] value [" + group + "]");
        }
        return groupFields;
    }

    public static List<FieldsBase> getGroupFields(final FieldsBase fields,
            final String group) throws FieldNotFoundException {
        List<FieldsBase> groupFields = new ArrayList<>();
        Iterator<FieldsBase> ite = fields.iterator();
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Fields) {
                if (StringUtils.equals(f.getName(), "group")
                        && StringUtils.equals(f.getValue(), group)) {
                    groupFields.add(f);
                }
            }
        }
        if (groupFields.size() == 0) {
            throw new FieldNotFoundException("name [group] value [" + group + "]");
        }
        return groupFields;
    }

    public static FieldsBase getFieldsByValue(final List<FieldsBase> fields,
            final String name, final String value) throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Fields) {
                if (StringUtils.equals(f.getName(), name)
                        && StringUtils.equals(f.getValue(), value)) {
                    return f;
                }
            }
        }
        throw new FieldNotFoundException("name [" + name + "] value [" + value + "]");
    }

    public static String prefixFieldValue(final List<FieldsBase> prefixes,
            final String value) {
        String prefixedValue = value;
        Iterator<FieldsBase> ite = new FieldsIterator(prefixes);
        while (ite.hasNext()) {
            FieldsBase prefix = ite.next();
            if (prefix instanceof Field) {
                prefixedValue = prefix.getValue() + prefixedValue;
            }
        }
        return prefixedValue;
    }

    public static int fieldCount(final List<FieldsBase> fields) {
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

    public static Field createField(final String name, final String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public static String getFormattedFields(final List<FieldsBase> fields) {
        if (fields == null) {
            return "Fields=null";
        }
        FieldsIterator ite = new FieldsIterator(fields);
        if (!ite.hasNext()) {
            return "Fields=[]";
        }
        StringBuilder sb = new StringBuilder("fields=");
        String line = System.lineSeparator();
        while (ite.hasNext()) {
            sb.append(line);
            FieldsBase f = ite.next();
            if (f instanceof Fields) {
                sb.append(" Fields [name=");
            }
            if (f instanceof Field) {
                sb.append("  Field [name=");
            }
            sb.append(f.getName());
            sb.append(", value=");
            sb.append(f.getValue());
            sb.append("]");
        }
        return sb.toString();
    }

    public static List<FieldsBase> replaceVariables(final List<FieldsBase> fields,
            final Map<String, ?> map) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Iterator<FieldsBase> ite = new FieldsIterator(fields);
        List<FieldsBase> patchedFields = new ArrayList<>();
        while (ite.hasNext()) {
            FieldsBase field = ite.next();
            if (field instanceof Field) {
                String str = field.getValue();
                Map<String, String> valueMap = getValueMap(str, map);
                String patchedStr = StrSubstitutor.replace(str, valueMap, "%{", "}");
                Field patchedField = new Field();
                patchedField.setName(field.getName());
                patchedField.setValue(patchedStr);
                patchedFields.add(patchedField);
            }
        }
        return patchedFields;
    }

    static Map<String, String> getValueMap(final String str, final Map<String, ?> map)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        String[] keys = StringUtils.substringsBetween(str, "%{", "}");
        if (keys == null) {
            return null;
        }
        Map<String, String> valueMap = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split("\\.");
            String axisName = parts[0];
            String property = parts[1];
            Object axis = map.get(axisName.toUpperCase());
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(axis, property);
            valueMap.put(key, ConvertUtils.convert(o));
        }
        return valueMap;
    }

    public static List<FieldsBase> asList(final FieldsBase f) {
        List<FieldsBase> fields = new ArrayList<>();
        fields.add(f);
        return fields;
    }
}
