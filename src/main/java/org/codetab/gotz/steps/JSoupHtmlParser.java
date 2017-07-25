package org.codetab.gotz.steps;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
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
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.stepbase.BaseParser;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JSoupHtmlParser extends BaseParser {

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
        List<FieldsBase> list =
                dataDefHelper.getAxis(dataDef, axis.getName()).getFields();
        try {
            List<FieldsBase> scripts = FieldsUtil.filterByGroup(list, "script");
            setTraceString(sb, scripts, "<<<");
            scripts = FieldsUtil.replaceVariables(scripts, member.getAxisMap());
            setTraceString(sb, scripts, ">>>>");
            LOGGER.trace(getMarker(), "Patch Scripts {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);
            value = queryByScript(scripts);
        } catch (FieldNotFoundException e) {
        }

        try {
            List<FieldsBase> queries = FieldsUtil.filterByGroup(list, "query");
            setTraceString(sb, queries, "<<<");
            queries = FieldsUtil.replaceVariables(queries, member.getAxisMap());
            setTraceString(sb, queries, ">>>>");
            LOGGER.trace(getMarker(), "Patch Queries {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);
            value = queryBySelector(page, queries);
        } catch (FieldNotFoundException e) {
        }

        try {
            List<FieldsBase> prefix = FieldsUtil.filterByGroup(list, "prefix");
            value = FieldsUtil.prefixValue(prefix, value);
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
        LOGGER.trace(getMarker(), "------ query data ------");
        LOGGER.trace(getMarker(), "Scripts {} ", scripts);
        jsEngine.put("configs", configService);
        String scriptStr = FieldsUtil.getValue(scripts, "script");
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
            final List<FieldsBase> queries) throws FieldNotFoundException {
        if (FieldsUtil.countField(queries) < 2) {
            LOGGER.warn("Insufficient queries in DataDef [{}]",
                    getDataDefName());
            return null;
        }
        LOGGER.trace(getMarker(), "------ query data ------");
        LOGGER.trace(getMarker(), "Queries {} ", queries);
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
            if (attr == null) {
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
            final List<FieldsBase> fields, final String header) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        sb.append(Util.LINE);
        sb.append("  ");
        sb.append(header);
        for (FieldsBase field : fields) {
            sb.append(field);
        }
    }
}
