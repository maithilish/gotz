package org.codetab.gotz.step.extract;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseParser;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class HtmlParser extends BaseParser {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlParser.class);

    private static final int TIMEOUT_MILLIS = 120000;

    private Map<Integer, List<?>> nodeMap;
    private ScriptEngine jsEngine;
    private HtmlPage htmlPage;

    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private DataDefHelper dataDefHelper;

    public HtmlParser() {
        nodeMap = new HashMap<Integer, List<?>>();
    }

    @Override
    protected boolean postInitialize() {
        WebClient webClient = null;
        try {
            if (!isDocumentLoaded()) {
                throw new IllegalStateException("document not loaded");
            }
            String html = getDocumentHTML();
            URL url = getDocumentURL();
            StringWebResponse response = new StringWebResponse(html, url);
            webClient = getWebClient();
            htmlPage = HTMLParser.parseHtml(response,
                    webClient.getCurrentWindow());
        } catch (IllegalStateException | IOException | DataFormatException e) {
            String givenUpMessage =
                    "unable to create HtmlUnit.HtmlPage from document byte[]";
            LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
            throw new StepRunException(givenUpMessage, e);
        } finally {
            webClient.setRefreshHandler(new ImmediateRefreshHandler());
            webClient.close();
        }
        setStepState(StepState.INIT);
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
            NoSuchMethodException, IOException {
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
                    startIndex = getStartIndex(axis.getXField());
                } catch (XFieldException e) {
                    startIndex = 1;
                }
                axis.setIndex(startIndex);
            }
            if (htmlPage != null && axis.getValue() == null) {
                String value = getValue(htmlPage, dataDef, member, axis);
                axis.setValue(value);
            }
        }
    }

    private WebClient getWebClient() {
        int timeout = TIMEOUT_MILLIS;
        String key = "gotz.webClient.timeout";
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            // TODO add activity or update config with default
            String msg =
                    "for config [" + key + "] using default value " + timeout;
            LOGGER.warn("{}. {}", e, msg);
        }

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.setRefreshHandler(new ThreadedRefreshHandler());

        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setTimeout(timeout);
        return webClient;
    }

    private String getDocumentHTML() throws DataFormatException, IOException {
        byte[] bytes = documentHelper.getDocumentObject(getDocument());
        String html = new String(bytes);
        return html;
    }

    private URL getDocumentURL() throws MalformedURLException {
        URL url;
        if (UrlValidator.getInstance().isValid(getDocument().getUrl())) {
            url = new URL(getDocument().getUrl());
        } else {
            url = new URL(new URL("file:"), getDocument().getUrl());
        }
        return url;
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
        XField xField =
                dataDefHelper.getAxis(dataDef, axis.getName()).getXfield();
        try {
            Map<String, String> scripts = new HashMap<>();
            scripts.put("script",
                    xFieldHelper.getLastValue("//xf:script/@value", xField));
            setTraceString(sb, scripts, "<<<");
            xFieldHelper.replaceVariables(scripts, member.getAxisMap());
            setTraceString(sb, scripts, ">>>>");
            LOGGER.trace(getMarker(), "Patch Scripts {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);
            value = queryByScript(scripts);
        } catch (XFieldException e) {
        }

        try {
            Map<String, String> queries = new HashMap<>();
            queries.put("region",
                    xFieldHelper.getLastValue("//xf:query/@region", xField));
            queries.put("field",
                    xFieldHelper.getLastValue("//xf:query/@field", xField));
            try {
                queries.put("attribute", xFieldHelper
                        .getLastValue("//xf:query/@attribute", xField));
            } catch (XFieldException e) {
                queries.put("attribute", "");
            }
            setTraceString(sb, queries, "<<<");
            xFieldHelper.replaceVariables(queries, member.getAxisMap());
            setTraceString(sb, queries, ">>>>");
            LOGGER.trace(getMarker(), "Patch Queries {}{}{}", Util.LINE,
                    sb.toString(), Util.LINE);

            value = queryByXPath(page, queries);
        } catch (XFieldException e) {
        }

        try {
            List<String> prefixes =
                    xFieldHelper.getValues("//xf:prefix", xField);
            value = xFieldHelper.prefixValue(value, prefixes);
        } catch (XFieldException e) {
        }

        return value;
    }

    private String queryByScript(final Map<String, String> scripts)
            throws ScriptException, XFieldException {
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

    private String queryByXPath(final HtmlPage page,
            final Map<String, String> queries) throws XFieldException {
        if (queries.size() < 2) {
            LOGGER.warn("Insufficient queries in DataDef [{}]",
                    getDataDefName());
            return null;
        }
        LOGGER.trace(getMarker(), "------ query data ------");
        LOGGER.trace(getMarker(), "Queries {} ", queries);
        String regionXpathExpr = queries.get("region");
        String xpathExpr = queries.get("field");
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
        LOGGER.trace(getMarker(), "------Region------");
        LOGGER.trace(getMarker(),
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
        LOGGER.trace(getMarker(), "------Nodes------");
        LOGGER.trace(getMarker(), Util.buildString("Nodes ",
                String.valueOf(nodes.size()), " for XPATH: ", xpathExpr));
        for (Object o : nodes) {
            DomNode childNode = (DomNode) o;
            value = childNode.getTextContent();
            String nodeTraceStr = Util.stripe(childNode.asXml(), numOfLines,
                    "Data Node \n--------\n", "--------");
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        LOGGER.trace(getMarker(), "node contents : {}", value);
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
