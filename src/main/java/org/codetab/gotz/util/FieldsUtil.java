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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;

public final class FieldsUtil {

    private FieldsUtil() {
    }

    /*
     * models hold List<FieldsBase> and FieldsBase may be Field or Fields for
     * convince List<FieldsBase> is called as fields instead of fieldsBaseList
     * or fieldsBases
     */

    // create methods
    public static Field createField(final String name, final String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public static List<FieldsBase> asList(final FieldsBase fb) {
        List<FieldsBase> list = new ArrayList<>();
        list.add(fb);
        return list;
    }

    // get methods

    // return first matching FieldsBase value
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

    public static Range<Integer> getRange(final List<FieldsBase> fields,
            final String name)
            throws FieldNotFoundException, NumberFormatException {
        String value = FieldsUtil.getValue(fields, name);
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

    public static List<FieldsBase> filterByName(final List<FieldsBase> fields,
            final String group, final String name)
            throws FieldNotFoundException {
        List<FieldsBase> groupFields = filterByGroup(fields, group);
        return filterByName(groupFields, name);
    }

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

    public static String prefixValue(final List<FieldsBase> prefixes,
            final String str) {
        String prefixedByValue = str;
        Iterator<FieldsBase> ite = new FieldsIterator(prefixes);
        while (ite.hasNext()) {
            FieldsBase prefix = ite.next();
            if (prefix instanceof Field) {
                prefixedByValue = prefix.getValue() + prefixedByValue;
            }
        }
        return prefixedByValue;
    }

    public static List<FieldsBase> replaceVariables(
            final List<FieldsBase> fields, final Map<String, ?> map)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Iterator<FieldsBase> ite = new FieldsIterator(fields);
        List<FieldsBase> patchedFields = new ArrayList<>();
        while (ite.hasNext()) {
            FieldsBase field = ite.next();
            if (field instanceof Field) {
                String str = field.getValue();
                Map<String, String> valueMap = getValueMap(str, map);
                String patchedStr =
                        StrSubstitutor.replace(str, valueMap, "%{", "}");
                Field patchedField = new Field();
                patchedField.setName(field.getName());
                patchedField.setValue(patchedStr);
                patchedFields.add(patchedField);
            }
        }
        return patchedFields;
    }

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
            String axisName = parts[0];
            String property = parts[1];
            Object axis = map.get(axisName.toUpperCase());
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(axis, property);
            valueMap.put(key, ConvertUtils.convert(o));
        }
        return valueMap;
    }

    // boolean methods

    // contains field or fields with matching name/value
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

    // contains field or fields with matching name/value
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

    public static boolean isTrue(final List<FieldsBase> fields,
            final String group, final String name)
            throws FieldNotFoundException {
        List<FieldsBase> fg = filterByGroup(fields, group);
        return isTrue(fg, name);
    }

    public static boolean isTrue(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsBase fb = getField(fields, name);
        if (StringUtils.equalsIgnoreCase(fb.getValue(), "true")) {
            return true;
        }
        return false;
    }

    public static boolean isDefined(final List<FieldsBase> fields,
            final String name) {
        try {
            getValue(fields, name);
            return true;
        } catch (FieldNotFoundException e) {
            return false;
        }
    }

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

    // misc methods

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
