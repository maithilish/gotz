package in.m.picks.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.misc.FieldsIterator;
import in.m.picks.model.Field;
import in.m.picks.model.FieldsBase;

public final class AccessUtil {

	final static Logger logger = LoggerFactory.getLogger(AccessUtil.class);

	public static String getValue(List<FieldsBase> fields, String name)
			throws FieldNotFoundException {
		FieldsIterator ite = new FieldsIterator(fields);
		while (ite.hasNext()) {
			FieldsBase f = ite.next();
			if (f instanceof Field && f.getName().equals(name)) {
				return f.getValue();
			}
		}
		throw new FieldNotFoundException(name);
	}

	public static void replaceVariables(List<FieldsBase> fields,
			Map<String, ?> map) {
		Iterator<FieldsBase> ite = new FieldsIterator(fields);
		while (ite.hasNext()) {
			FieldsBase f = ite.next();
			if (f instanceof Field) {
				Field field = (Field) f;
				String str = field.getValue();
				Map<String, String> valueMap = getValueMap(str, map);
				String patchedStr = StrSubstitutor.replace(str, valueMap, "%{", "}");
				field.setValue(patchedStr);
			}
		}
	}

	private static Map<String, String> getValueMap(String str, Map<String, ?> map) {
		String[] keys = StringUtils.substringsBetween(str, "%{", "}");
		if (keys == null) {
			return null;
		}
		Map<String, String> valueMap = new HashMap<String, String>();
		for (String key : keys) {
			String[] parts = key.split("\\.");
			String axisName = parts[0];
			String property = parts[1];
			Object axis = map.get(axisName);
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
