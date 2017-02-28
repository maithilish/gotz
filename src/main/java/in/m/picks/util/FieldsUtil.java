package in.m.picks.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Field;
import in.m.picks.model.Fields;
import in.m.picks.model.FieldsBase;

public class FieldsUtil {

	/*
	 * models hold List<FieldsBase> and FieldsBase may be Field or Fields for
	 * convince List<FieldsBase> is called as fields instead of fieldsBaseList
	 * or fieldsBases
	 */

	// return first matching field value
	public static String getValue(List<FieldsBase> fields, String name)
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

	public static String getValue(FieldsBase fields, String name)
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

	public static int getIntValue(List<FieldsBase> fields, String name)
			throws FieldNotFoundException {
		try {
			return Integer.parseInt(getValue(fields, name));
		} catch (NumberFormatException e) {
			AccessUtil.logger.warn("Field [{}] {}", name, e);
			throw e;
		}
	}

	public static Range<Integer> getRange(List<FieldsBase> fields, String name)
			throws FieldNotFoundException, NumberFormatException {
		String value = getValue(fields, name);
		String[] tokens = StringUtils.split(value, '-');
		if (tokens.length < 1 || tokens.length > 2) {
			NumberFormatException e = new NumberFormatException(
					"Invalid Range " + value);
			AccessUtil.logger.warn("AField [{}] {} {}", name, e, fields);
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
			AccessUtil.logger.warn("AField [{}] {} {}", name, e, fields);
			throw e;
		}
		return Range.between(min, max);
	}

	public static boolean isFieldTrue(List<FieldsBase> fields, String group,
			String name) throws FieldNotFoundException {
		List<FieldsBase> fc = FieldsUtil.getGroupFields(fields, group);
		if (StringUtils.equalsIgnoreCase(getValue(fc, name), "true")) {
			return true;
		}
		return false;
	}

	public static boolean isFieldTrue(List<FieldsBase> fields, String name)
			throws FieldNotFoundException {
		FieldsBase field = getField(fields, name);
		if (StringUtils.equalsIgnoreCase(field.getValue(), "true")) {
			return true;
		}
		return false;
	}

	public static boolean isAnyFieldDefined(List<FieldsBase> fields,
			String... names) {
		for (String name : names) {
			try {
				FieldsUtil.getValue(fields, name);
				return true;
			} catch (FieldNotFoundException e) {
			}
		}
		return false;
	}

	public static boolean isAllFieldsDefined(List<FieldsBase> fields,
			String... names) {
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

	public static Field getField(List<FieldsBase> fields, String name)
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

	public static List<FieldsBase> getFieldList(List<FieldsBase> fields, String name)
			throws FieldNotFoundException {
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

	public static List<FieldsBase> getGroupFields(List<FieldsBase> fields,
			String group) throws FieldNotFoundException {
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

	public static List<FieldsBase> getGroupFields(FieldsBase fields, String group)
			throws FieldNotFoundException {
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

	public static FieldsBase getFieldsByValue(List<FieldsBase> fields, String name,
			String value) throws FieldNotFoundException {
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
		throw new FieldNotFoundException(
				"name [" + name + "] value [" + value + "]");
	}

	public static String prefixFieldValue(List<FieldsBase> prefixes, String value) {
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

	public static int fieldCount(List<FieldsBase> fields) {
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

	public static String getFormattedFields(List<FieldsBase> fields) {
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
}
