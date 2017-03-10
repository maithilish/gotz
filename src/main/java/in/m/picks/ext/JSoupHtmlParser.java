package in.m.picks.ext;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.DataDef;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.ConfigService;
import in.m.picks.step.Parser;
import in.m.picks.util.DataDefUtil;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class JSoupHtmlParser extends Parser {

	final static Logger logger = LoggerFactory.getLogger(JSoupHtmlParser.class);

	private Map<Integer, Elements> elementsMap;
	private ScriptEngine jsEngine;

	public JSoupHtmlParser() {
		elementsMap = new HashMap<>();
	}

	@Override
	protected void setValue(DataDef dataDef, Member member)
			throws ScriptException, NumberFormatException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		for (AxisName axisName : AxisName.values()) {
			Axis axis = member.getAxis(axisName);
			if (axis == null) {
				continue;
			}
			if (axis.getIndex() == null) {
				Integer startIndex;
				try {
					startIndex = getStartIndex(axis.getFields());
				} catch (FieldNotFoundException e) {
					startIndex = 1;
				}
				axis.setIndex(startIndex);
			}
			if (isDocumentLoaded() && axis.getValue() == null) {
				String docHtml = (String) getDocument().getDocumentObject();
				Document doc = Jsoup.parse(docHtml);
				String value = getValue(doc, dataDef, member, axis);
				axis.setValue(value);
			}
		}
	}

	protected String getValue(Document page, DataDef dataDef, Member member,
			Axis axis) throws ScriptException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		StringBuilder sb = new StringBuilder(); // to trace query strings
		String value = null;
		List<FieldsBase> list = DataDefUtil.getAxis(dataDef, axis.getName())
				.getFields();
		try {
			List<FieldsBase> scripts = FieldsUtil.getGroupFields(list,
					"script");
			setTraceString(sb, scripts, "--- Script ---");
			scripts = FieldsUtil.replaceVariables(scripts, member.getAxisMap());
			setTraceString(sb, scripts, "-- Patched --");
			logger.trace("{}", sb);
			Util.logState(logger, "parser-" + dataDefName, "", fields, sb);
			value = queryByScript(scripts);
		} catch (FieldNotFoundException e) {
		}

		try {
			List<FieldsBase> queries = FieldsUtil.getGroupFields(list, "query");
			setTraceString(sb, queries, "--- Query ---");
			queries = FieldsUtil.replaceVariables(queries, member.getAxisMap());
			setTraceString(sb, queries, "-- Patched --");
			logger.trace("{}", sb);
			Util.logState(logger, "parser-" + dataDefName, "", fields, sb);
			value = queryBySelector(page, queries);
		} catch (FieldNotFoundException e) {
		}

		try {
			List<FieldsBase> prefix = FieldsUtil.getGroupFields(list, "prefix");
			value = FieldsUtil.prefixFieldValue(prefix, value);
		} catch (FieldNotFoundException e) {
		}

		return value;
	}

	private String queryByScript(List<FieldsBase> scripts)
			throws ScriptException, FieldNotFoundException {
		// TODO - check whether thread safety is involved
		if (jsEngine == null) {
			initializeScriptEngine();
		}
		logger.trace("------Query Data------");
		logger.trace("Scripts {} ", scripts);
		jsEngine.put("configs", ConfigService.INSTANCE);
		String scriptStr = FieldsUtil.getValue(scripts, "script");
		Object value = jsEngine.eval(scriptStr);
		return ConvertUtils.convert(value);
	}

	private void initializeScriptEngine() {
		logger.debug("{}", "Initializing script engine");
		ScriptEngineManager scriptEngineMgr = new ScriptEngineManager();
		jsEngine = scriptEngineMgr.getEngineByName("JavaScript");
		if (jsEngine == null) {
			throw new NullPointerException(
					"No script engine found for JavaScript");
		}
	}

	private String queryBySelector(Document page, List<FieldsBase> queries)
			throws FieldNotFoundException {
		if (FieldsUtil.fieldCount(queries) < 2) {
			logger.warn("Insufficient queries in DataDef [{}]", dataDefName);
			return null;
		}
		logger.trace("------Query Data------");
		logger.trace("Queries {} ", queries);
		String regionXpathExpr = FieldsUtil.getValue(queries, "region");
		String xpathExpr = FieldsUtil.getValue(queries, "field");
		String attr = null;
		try {
			attr = FieldsUtil.getValue(queries, "attribute"); // optional
		} catch (FieldNotFoundException e) {
		}
		String value = getBySelector(page, regionXpathExpr, xpathExpr, attr);
		return value;
	}

	private String getBySelector(Document page, String regionXpathExpr,
			String xpathExpr, String attr) {
		String value = null;
		Elements elements = getRegionNodes(page, regionXpathExpr);
		value = getBySelector(elements, xpathExpr, attr);
		return value;
	}

	private Elements getRegionNodes(Document page, String xpathExpr) {
		/*
		 * regional nodes are cached in HashMap for performance. Map is flushed
		 * in loadObject method.
		 */
		Integer hash = xpathExpr.hashCode();
		Elements elements = null;
		if (elementsMap.containsKey(hash)) {
			elements = elementsMap.get(hash);
		} else {
			elements = page.select(xpathExpr);
			elementsMap.put(hash, elements);
		}
		logger.trace(
				"Region Nodes " + elements.size() + " for XPATH: " + xpathExpr);
		for (Element element : elements) {
			String nodeTraceStr = Util.stripe(element.outerHtml(), 5,
					"Data Region \n-------------\n", "-------------");
			logger.trace("{}", nodeTraceStr);
			Util.logState(logger, "parser-" + dataDefName, "", fields,
					nodeTraceStr);
		}
		return elements;
	}

	private String getBySelector(Elements elements, String xpathExpr,
			String attr) {
		String value = null;
		Elements subElements = elements.select(xpathExpr);
		logger.trace(
				"Nodes " + subElements.size() + " for XPATH: " + xpathExpr);
		for (Element element : subElements) {
			if (attr == null) {
				value = element.ownText();
			} else {
				value = element.attr(attr); // get value by attribute key
			}
			String nodeTraceStr = Util.stripe(element.outerHtml(), 5,
					"Data Node \n--------\n", "--------");
			logger.trace("{}", nodeTraceStr);
			Util.logState(logger, "parser-" + dataDefName, "", fields,
					nodeTraceStr);
		}
		logger.trace("Text Content of the node: " + value);
		return value;
	}

	private void setTraceString(StringBuilder sb, List<FieldsBase> fields,
			String header) {
		if (!logger.isTraceEnabled()) {
			return;
		}
		String line = "\n";
		sb.append(line);
		sb.append(header);
		sb.append(line);
		for (FieldsBase field : fields) {
			sb.append(field);
			sb.append(line);
		}
	}

}
