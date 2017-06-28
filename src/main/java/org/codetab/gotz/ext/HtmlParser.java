package org.codetab.gotz.ext;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.Parser;
import org.codetab.gotz.util.DataDefUtil;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class HtmlParser extends Parser {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlParser.class);

    private Map<Integer, List<?>> nodeMap;
    private ScriptEngine jsEngine;

    public HtmlParser() {
        nodeMap = new HashMap<Integer, List<?>>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.codetab.gotz.step.Parser#setValue(org.codetab.gotz.model.DataDef,
     * org.codetab.gotz.model.Member)
     */
    @Override
    protected void setValue(final DataDef dataDef, final Member member)
            throws ScriptException, NumberFormatException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            try {
                axis = member.getAxis(axisName);
            } catch (NoSuchElementException e) {
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
                HtmlPage documentObject =
                        (HtmlPage) getDocument().getDocumentObject();
                String value = getValue(documentObject, dataDef, member, axis);
                axis.setValue(value);
            }
        }
    }

    /*
     *
     */
    private String getValue(final HtmlPage page, final DataDef dataDef,
            final Member member, final Axis axis)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder(); // to trace query strings
        String value = null;
        List<FieldsBase> list =
                DataDefUtil.getAxis(dataDef, axis.getName()).getFields();
        try {
            List<FieldsBase> scripts =
                    OFieldsUtil.getGroupFields(list, "script");
            setTraceString(sb, scripts, "--- Script ---");
            scripts =
                    OFieldsUtil.replaceVariables(scripts, member.getAxisMap());
            setTraceString(sb, scripts, "-- Patched --");
            LOGGER.trace(getMarker(), "{}", sb);
            value = queryByScript(scripts);
        } catch (FieldNotFoundException e) {
        }

        try {
            List<FieldsBase> queries =
                    OFieldsUtil.getGroupFields(list, "query");
            setTraceString(sb, queries, "--- Query ---");
            queries =
                    OFieldsUtil.replaceVariables(queries, member.getAxisMap());
            setTraceString(sb, queries, "-- Patched --");
            LOGGER.trace(getMarker(), "{}", sb);
            value = queryByXPath(page, queries);
        } catch (FieldNotFoundException e) {
        }

        try {
            List<FieldsBase> prefix =
                    OFieldsUtil.getGroupFields(list, "prefix");
            value = OFieldsUtil.prefixFieldValue(prefix, value);
        } catch (FieldNotFoundException e) {
        }

        return value;
    }

    private String queryByScript(final List<FieldsBase> scripts)
            throws ScriptException, FieldNotFoundException {
        // TODO - check whether thread safety is involved
        if (jsEngine == null) {
            initializeScriptEngine();
        }
        LOGGER.trace("------Query Data------");
        LOGGER.trace("Scripts {} ", scripts);
        jsEngine.put("configs", configService);
        String scriptStr = OFieldsUtil.getValue(scripts, "script");
        Object value = jsEngine.eval(scriptStr);
        return ConvertUtils.convert(value);
    }

    private void initializeScriptEngine() {
        LOGGER.debug("{}", "Initializing script engine");
        ScriptEngineManager scriptEngineMgr = new ScriptEngineManager();
        jsEngine = scriptEngineMgr.getEngineByName("JavaScript");
        if (jsEngine == null) {
            throw new NullPointerException(
                    "No script engine found for JavaScript");
        }
    }

    private String queryByXPath(final HtmlPage page,
            final List<FieldsBase> queries) throws FieldNotFoundException {
        if (OFieldsUtil.fieldCount(queries) < 2) {
            LOGGER.warn("Insufficient queries in DataDef [{}]",
                    getDataDefName());
            return null;
        }
        LOGGER.trace("------Query Data------");
        LOGGER.trace("Queries {} ", queries);
        String regionXpathExpr = OFieldsUtil.getValue(queries, "region");
        String xpathExpr = OFieldsUtil.getValue(queries, "field");
        String value = getByXPath(page, regionXpathExpr, xpathExpr);
        return value;
    }

    private String getByXPath(final HtmlPage page, final String regionXpathExpr,
            final String xpathExpr) {
        String value = null;
        List<?> nodes = getRegionNodes(page, regionXpathExpr);
        for (Object o : nodes) {
            DomNode node = (DomNode) o;
            value = getByXPath(node, xpathExpr);
        }
        return value;
    }

    private List<?> getRegionNodes(final HtmlPage page,
            final String xpathExpr) {
        /*
         * regional nodes are cached in HashMap for performance. Map is flushed
         * in loadObject method.
         */
        final int numOfLines = 5;
        Integer hash = xpathExpr.hashCode();
        List<?> nodes = null;
        if (nodeMap.containsKey(hash)) {
            nodes = nodeMap.get(hash);
        } else {
            nodes = page.getByXPath(xpathExpr);
            nodeMap.put(hash, nodes);
        }
        LOGGER.trace(
                "Region Nodes " + nodes.size() + " for XPATH: " + xpathExpr);
        for (Object o : nodes) {
            DomNode node = (DomNode) o;
            String nodeTraceStr = Util.stripe(node.asXml(), numOfLines,
                    "Data Region \n-------------\n", "-------------");
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        return nodes;
    }

    private String getByXPath(final DomNode node, final String xpathExpr) {
        final int numOfLines = 5;
        String value = null;
        List<?> nodes = node.getByXPath(xpathExpr);
        LOGGER.trace("Nodes " + nodes.size() + " for XPATH: " + xpathExpr);
        for (Object o : nodes) {
            DomNode childNode = (DomNode) o;
            value = childNode.getTextContent();
            String nodeTraceStr = Util.stripe(childNode.asXml(), numOfLines,
                    "Data Node \n--------\n", "--------");
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        LOGGER.trace("Text Content of the node: " + value);
        return value;
    }

    private void setTraceString(final StringBuilder sb,
            final List<FieldsBase> fields, final String header) {
        if (!LOGGER.isTraceEnabled()) {
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
