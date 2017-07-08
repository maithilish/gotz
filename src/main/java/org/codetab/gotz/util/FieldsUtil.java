package org.codetab.gotz.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;

public final class FieldsUtil {

    private FieldsUtil() {
    }

    public static Field createField(final String name, final String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public static Field getField(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Field && f.getName().equals(name)) {
                return (Field) f;
            }
        }
        throw new FieldNotFoundException("Name [" + name + "]");
    }

    public static Fields getFields(final List<FieldsBase> fields,
            final String name) throws FieldNotFoundException {
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f instanceof Fields && f.getName().equals(name)) {
                return (Fields) f;
            }
        }
        throw new FieldNotFoundException("Name [" + name + "]");
    }

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

    // contains field or fields with matching name/value
    public static boolean contains(final List<FieldsBase> fields,
            final String name, final String value) {
        if (fields == null) {
            return false;
        }
        FieldsIterator ite = new FieldsIterator(fields);
        while (ite.hasNext()) {
            FieldsBase f = ite.next();
            if (f.getName().equals(name) && f.getValue().equals(value)) {
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
            FieldsBase f = ite.next();
            if (f.getName().equals(name) && f.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFieldDefined(final List<FieldsBase> fields,
            final String name) {
        try {
            OFieldsUtil.getValue(fields, name);
            return true;
        } catch (FieldNotFoundException e) {
            return false;
        }
    }

}
