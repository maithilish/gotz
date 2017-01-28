package in.m.picks.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Afield;
import in.m.picks.model.Afields;
import in.m.picks.model.Axis;
import in.m.picks.model.Member;

public final class AccessUtil {

	final static Logger logger = LoggerFactory.getLogger(AccessUtil.class);

	public static String getStringValue(Afields afields, String name)
			throws AfieldNotFoundException {
		try {
			Afield afield = afields.getAfield(name);
			return afield.getValue();
		} catch (NullPointerException e) {
			throw new AfieldNotFoundException(name);
		}
	}

	public static int getIntValue(Afields afields, String name)
			throws AfieldNotFoundException {
		try {
			return Integer.parseInt(getStringValue(afields, name));
		} catch (NumberFormatException e) {
			logger.warn("AField [{}] {} {}", name, e, afields);
			throw e;
		}
	}

	public static Range<Integer> getRange(Afields afields, String name)
			throws AfieldNotFoundException, NumberFormatException {
		String value = getStringValue(afields, name);
		String[] tokens = StringUtils.split(value, '-');
		if (tokens.length < 1 || tokens.length > 2) {
			NumberFormatException e = new NumberFormatException(
					"Invalid Range " + value);
			logger.warn("AField [{}] {} {}", name, e, afields);
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
			logger.warn("AField [{}] {} {}", name, e, afields);
			throw e;
		}
		return Range.between(min, max);
	}

	public static boolean isAfieldTrue(Afields afields, String name)
			throws AfieldNotFoundException {
		Afield afield = getAfield(afields, name);
		if (StringUtils.equalsIgnoreCase(afield.getValue(), "true")) {
			return true;
		}
		return false;
	}

	public static boolean isAfieldTrue(Afields afields, String group, String name)
			throws AfieldNotFoundException {
		Afield afield = getAfield(afields, group, name);
		if (StringUtils.equalsIgnoreCase(afield.getValue(), "true")) {
			return true;
		}
		return false;
	}

	public static Afield getAfield(Afields afields, String group, String name)
			throws AfieldNotFoundException {
		for (Afield afield : afields.getAfields()) {
			if (StringUtils.equals(afield.getGroup(), group)
					&& StringUtils.equals(afield.getName(), name)) {
				return afield;
			}
		}
		throw new AfieldNotFoundException(
				"Group [" + group + "] Name [" + name + "]");
	}

	public static Afield getAfield(Afields afields, String name)
			throws AfieldNotFoundException {
		for (Afield afield : afields.getAfields()) {
			if (StringUtils.equals(afield.getName(), name)) {
				return afield;
			}
		}
		throw new AfieldNotFoundException("Name [" + name + "]");
	}

	public static void updateAfield(Afields afields, String name, String value)
			throws AfieldNotFoundException {
		try {
			Afield afield = afields.getAfield(name);
			afield.setValue(value);
		} catch (NullPointerException e) {
			AfieldNotFoundException ae = new AfieldNotFoundException(name);
			throw ae;
		}
	}

	public static String prefix(Afields afields, String value) {
		String prefixedValue = value;
		for (Afield afield : afields.getAfields()) {
			prefixedValue = afield.getValue() + prefixedValue;
		}
		return prefixedValue;
	}

	public static void replaceVariables(Afields afields, Member member) {
		for (Afield afield : afields.getAfields()) {
			String str = afield.getValue();
			Map<String, String> valueMap = getValueMap(str, member);
			String patchedStr = StrSubstitutor.replace(str, valueMap, "%{", "}");
			afield.setValue(patchedStr);
		}
	}

	private static Map<String, String> getValueMap(String str, Member member) {
		String[] keys = StringUtils.substringsBetween(str, "%{", "}");
		if (keys == null) {
			return null;
		}
		Map<String, String> valueMap = new HashMap<String, String>();
		for (String key : keys) {
			String[] parts = key.split("\\.");
			String axisName = parts[0];
			String property = parts[1];
			Axis axis = member.getAxis(axisName);
			try {
				// call getter and type convert to String
				Object o = PropertyUtils.getProperty(axis, property);
				valueMap.put(key, ConvertUtils.convert(o));
			} catch (Exception e) {
				logger.warn("Unable construct valueMap to replace variables {}", e);
			}
		}
		return valueMap;
	}

}
