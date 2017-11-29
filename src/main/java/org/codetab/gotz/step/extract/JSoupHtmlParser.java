package org.codetab.gotz.step.extract;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseParser;
import org.codetab.gotz.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSoupHtmlParser extends BaseParser {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlParser.class);

    private Map<Integer, Elements> elementsMap;
    private ScriptEngine jsEngine;

    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private DataDefHelper dataDefHelper;

    // TODO add marker to all trace calls. entire project

    public JSoupHtmlParser() {
        elementsMap = new HashMap<>();
    }

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    protected boolean postInitialize() {
        return true;
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
            NoSuchMethodException, DataFormatException, IOException {
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
                } catch (FieldsException e) {
                    startIndex = 1;
                }
                axis.setIndex(startIndex);
            }
            if (isDocumentLoaded() && axis.getValue() == null) {
                byte[] bytes = documentHelper.getDocumentObject(getDocument());
                String html = new String(bytes);
                Document doc = Jsoup.parse(html);
                String value = getValue(doc, dataDef, member, axis);
                axis.setValue(value);
            }
        }
    }

    /*
     *
     */
    private String getValue(final Document page, final DataDef dataDef,
            final Member member, final Axis axis)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // TODO explore whether query can be made strict so that it has
        // to return value else raise StepRunException
        StringBuilder sb = new StringBuilder(); // to trace query strings
        String value = null;
        Fields fields =
                dataDefHelper.getAxis(dataDef, axis.getName()).getFields();
        try {
            Map<String, String> scripts = new HashMap<>();
            scripts.put("script",
                    fieldsHelper.getLastValue("//xf:script/@script", fields));
            setTraceString(sb, scripts, "<<<");
            fieldsHelper.replaceVariables(scripts, member.getAxisMap());
            setTraceString(sb, scripts, ">>>>");
            LOGGER.trace(getMarker(), "Patch Scripts {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);
            value = queryByScript(scripts);
        } catch (FieldsException e) {
        }

        try {
            Map<String, String> queries = new HashMap<>();
            queries.put("region",
                    fieldsHelper.getLastValue("//xf:query/@region", fields));
            queries.put("field",
                    fieldsHelper.getLastValue("//xf:query/@field", fields));
            try {
                queries.put("attribute", fieldsHelper
                        .getLastValue("//xf:query/@attribute", fields));
            } catch (FieldsException e) {
                queries.put("attribute", "");
            }
            setTraceString(sb, queries, "<<<");
            fieldsHelper.replaceVariables(queries, member.getAxisMap());
            setTraceString(sb, queries, ">>>>");
            LOGGER.trace(getMarker(), "Patch Queries {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);
            value = queryBySelector(page, queries);
        } catch (FieldsException e) {
        }

        try {
            List<String> prefixes =
                    fieldsHelper.getValues("//xf:prefix", fields);
            value = fieldsHelper.prefixValue(value, prefixes);
        } catch (FieldsException e) {
        }

        return value;
    }

    private String queryByScript(final Map<String, String> scripts)
            throws ScriptException, FieldsException {
        // TODO - check whether thread safety is involved
        if (jsEngine == null) {
            initializeScriptEngine();
        }
        LOGGER.trace(getMarker(), "------ query data ------");
        LOGGER.trace(getMarker(), "Scripts {} ", scripts);
        jsEngine.put("configs", configService);
        jsEngine.put("document", getDocument());
        String scriptStr = scripts.get("script");
        Object val = jsEngine.eval(scriptStr);
        String value = ConvertUtils.convert(val);
        LOGGER.trace(getMarker(), "result {}", value);
        LOGGER.trace(getMarker(), "------ query data end ------");
        LOGGER.trace(getMarker(), "");
        return value;
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

    private String queryBySelector(final Document page,
            final Map<String, String> queries) {
        if (queries.size() < 2) {
            LOGGER.warn("Insufficient queries in DataDef [{}]",
                    getDataDefName());
            return null;
        }
        LOGGER.trace(getMarker(), "------ query data ------");
        LOGGER.trace(getMarker(), "Queries {} ", queries);
        String regionXpathExpr = queries.get("region");
        String xpathExpr = queries.get("field");
        // optional attribute, only for jsoup
        String attr = queries.get("attribute");
        String value = getBySelector(page, regionXpathExpr, xpathExpr, attr);
        return value;
    }

    private String getBySelector(final Document page,
            final String regionXpathExpr, final String xpathExpr,
            final String attr) {
        String value = null;
        Elements elements = getRegionNodes(page, regionXpathExpr);
        value = getBySelector(elements, xpathExpr, attr);
        return value;
    }

    private Elements getRegionNodes(final Document page,
            final String xpathExpr) {
        /*
         * regional nodes are cached in HashMap for performance. Map is flushed
         * in loadObject method.
         */
        final int numOfLines = 5;
        Integer hash = xpathExpr.hashCode();
        Elements elements = null;
        if (elementsMap.containsKey(hash)) {
            elements = elementsMap.get(hash);
        } else {
            elements = page.select(xpathExpr);
            elementsMap.put(hash, elements);
        }

        LOGGER.trace(getMarker(), "------Region------");
        LOGGER.trace(getMarker(),
                "Region Nodes " + elements.size() + " for XPATH: " + xpathExpr);
        for (Element element : elements) {
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    "Data Region \n-------------\n", "-------------");
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        return elements;
    }

    private String getBySelector(final Elements elements,
            final String xpathExpr, final String attr) {
        final int numOfLines = 5;
        String value = null;
        Elements subElements = elements.select(xpathExpr);
        LOGGER.trace(getMarker(), "------Node------");
        LOGGER.trace(
                "Nodes " + subElements.size() + " for XPATH: " + xpathExpr);
        for (Element element : subElements) {
            if (StringUtils.isBlank(attr)) {
                value = element.ownText();
            } else {
                value = element.attr(attr); // get value by attribute key
            }
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    "Data Node \n--------\n", "--------");
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        LOGGER.trace(getMarker(), "Node contents : {}", value);
        LOGGER.trace(getMarker(), "------ query data end ------");
        LOGGER.trace(getMarker(), "");
        return value;
    }

    private void setTraceString(final StringBuilder sb,
            final Map<String, String> strings, final String header) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        sb.append(Util.LINE);
        sb.append("  ");
        sb.append(header);
        for (String key : strings.keySet()) {
            sb.append(key);
            sb.append(" : ");
            sb.append(strings.get(key));
            sb.append(Util.LINE);
        }
    }
}
