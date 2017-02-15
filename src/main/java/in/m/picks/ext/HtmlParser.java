package in.m.picks.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.DataDef;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.ConfigService;
import in.m.picks.step.Parser;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.DataDefUtil;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class HtmlParser extends Parser {

	final static Logger logger = LoggerFactory.getLogger(HtmlParser.class);

	private Map<Integer, List<?>> nodeMap;
	private ScriptEngine jsEngine;

	public HtmlParser() {
		nodeMap = new HashMap<Integer, List<?>>();
	}

	protected String getValue(HtmlPage page, DataDef dataDef, Member member,
			Axis axis) throws ScriptException {
		StringBuilder sb = new StringBuilder(); // to trace query strings
		String value = null;
		List<FieldsBase> list = DataDefUtil.getAxis(dataDef, axis.getName())
				.getFields();
		try {
			List<FieldsBase> scripts = FieldsUtil.getGroupFields(list, "script");
			setTraceString(sb, scripts, "--- Script ---");
			AccessUtil.replaceVariables(scripts, member.getAxisMap());
			setTraceString(sb, scripts, "-- Patched --");
			logger.trace("{}", sb);
			value = queryByScript(scripts);
		} catch (FieldNotFoundException e) {
		}

		try {
			List<FieldsBase> queries = FieldsUtil.getGroupFields(list, "query");
			setTraceString(sb, queries, "--- Query ---");
			AccessUtil.replaceVariables(queries, member.getAxisMap());
			setTraceString(sb, queries, "-- Patched --");
			logger.trace("{}", sb);
			value = queryByXPath(page, queries);
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
			throw new NullPointerException("No script engine found for JavaScript");
		}
	}

	private String queryByXPath(HtmlPage page, List<FieldsBase> queries)
			throws FieldNotFoundException {
		if (queries.size() < 2) {
			logger.warn("Insufficient queries in DataDef [{}]", dataDefName);
			return null;
		}
		logger.trace("------Query Data------");
		logger.trace("Queries {} ", queries);
		String regionXpathExpr = FieldsUtil.getValue(queries, "region");
		String xpathExpr = FieldsUtil.getValue(queries, "field");
		String value = getByXPath(page, regionXpathExpr, xpathExpr);
		return value;
	}

	private String getByXPath(HtmlPage page, String regionXpathExpr,
			String xpathExpr) {
		String value = null;
		List<?> nodes = getRegionNodes(page, regionXpathExpr);
		for (Object o : nodes) {
			DomNode node = (DomNode) o;
			value = getByXPath(node, xpathExpr);
		}
		return value;
	}

	private List<?> getRegionNodes(HtmlPage page, String xpathExpr) {
		/*
		 * regional nodes are cached in HashMap for performance. Map is flushed
		 * in loadObject method.
		 */
		Integer hash = xpathExpr.hashCode();
		List<?> nodes = null;
		if (nodeMap.containsKey(hash)) {
			nodes = nodeMap.get(hash);
		} else {
			nodes = page.getByXPath(xpathExpr);
			nodeMap.put(hash, nodes);
		}
		logger.trace("Region Nodes " + nodes.size() + " for XPATH: " + xpathExpr);
		for (Object o : nodes) {
			DomNode node = (DomNode) o;
			logger.trace(Util.stripe(node.asXml(), 5,
					"Data Region \n-------------\n", "-------------"));
		}
		return nodes;
	}

	private String getByXPath(DomNode node, String xpathExpr) {
		String value = null;
		List<?> nodes = node.getByXPath(xpathExpr);
		logger.trace("Nodes " + nodes.size() + " for XPATH: " + xpathExpr);
		for (Object o : nodes) {
			DomNode childNode = (DomNode) o;
			value = childNode.getTextContent();
			logger.trace(Util.stripe(childNode.asXml(), 5, "Data Node \n--------\n",
					"--------"));
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
