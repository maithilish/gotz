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
import javax.script.ScriptException;

import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseParser;
import org.codetab.gotz.util.Util;
import org.jsoup.helper.Validate;
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

public class HtmlParser extends BaseParser {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlParser.class);

    private static final int TIMEOUT_MILLIS = 120000;

    private Map<Integer, List<?>> nodeMap;
    private HtmlPage htmlPage;

    @Inject
    private DocumentHelper documentHelper;

    public HtmlParser() {
        nodeMap = new HashMap<Integer, List<?>>();
    }

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    protected boolean postInitialize() {
        WebClient webClient = null;
        try {
            if (!isDocumentLoaded()) {
                throw new IllegalStateException(
                        Messages.getString("HtmlParser.0")); //$NON-NLS-1$
            }
            String html = getDocumentHTML();
            URL url = getDocumentURL();
            StringWebResponse response = new StringWebResponse(html, url);
            webClient = getWebClient();
            htmlPage = HTMLParser.parseHtml(response,
                    webClient.getCurrentWindow());
        } catch (IllegalStateException | IOException | DataFormatException e) {
            String message = Util.join(Messages.getString("HtmlParser.1"), //$NON-NLS-1$
                    HtmlPage.class.getName(),
                    Messages.getString("HtmlParser.2")); //$NON-NLS-1$
            throw new StepRunException(message, e);
        } finally {
            if (webClient != null) {
                webClient.setRefreshHandler(new ImmediateRefreshHandler());
                webClient.close();
            }
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
                } catch (FieldsNotFoundException e) {
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
        String key = "gotz.webClient.timeout"; //$NON-NLS-1$
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            String message = Util.join(Messages.getString("HtmlParser.4"), key, //$NON-NLS-1$
                    Messages.getString("HtmlParser.5"), //$NON-NLS-1$
                    String.valueOf(timeout));
            throw new IllegalStateException(message, e);
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
            url = new URL(new URL("file:"), getDocument().getUrl()); //$NON-NLS-1$
        }
        return url;
    }

    @Override
    protected String queryByQuery(final Object page,
            final Map<String, String> queries) {

        Validate.notNull(page, Messages.getString("HtmlParser.7")); //$NON-NLS-1$
        Validate.notNull(queries, Messages.getString("HtmlParser.8")); //$NON-NLS-1$

        if (page instanceof HtmlPage) {
            return queryByXPath((HtmlPage) page, queries);
        } else {
            throw new IllegalStateException(
                    Util.join(Messages.getString("HtmlParser.9"), //$NON-NLS-1$
                            HtmlPage.class.getName()));

        }
    }

    private String queryByXPath(final HtmlPage page,
            final Map<String, String> queries) {
        String regionXpathExpr = queries.get("region"); //$NON-NLS-1$
        String xpathExpr = queries.get("field"); //$NON-NLS-1$

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

        LOGGER.trace(getMarker(), Messages.getString("HtmlParser.12"), //$NON-NLS-1$
                Util.LINE);
        LOGGER.trace(getMarker(),
                Messages.getString("HtmlParser.13") + nodes.size() //$NON-NLS-1$
                        + Messages.getString("HtmlParser.14") + xpathExpr); //$NON-NLS-1$

        for (Object o : nodes) {
            DomNode node = (DomNode) o;
            String nodeTraceStr = Util.stripe(node.asXml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr); //$NON-NLS-1$
        }

        return nodes;
    }

    private String getByXPath(final DomNode node, final String xpathExpr) {
        final int numOfLines = 5;
        String value = null;
        List<?> nodes = node.getByXPath(xpathExpr);

        LOGGER.trace(getMarker(), Messages.getString("HtmlParser.16"), //$NON-NLS-1$
                Util.LINE);
        LOGGER.trace(getMarker(), Util.join(Messages.getString("HtmlParser.3"), //$NON-NLS-1$
                String.valueOf(nodes.size()),
                Messages.getString("HtmlParser.18"), xpathExpr)); //$NON-NLS-1$

        for (Object o : nodes) {
            DomNode childNode = (DomNode) o;
            value = childNode.getTextContent();
            String nodeTraceStr = Util.stripe(childNode.asXml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr); //$NON-NLS-1$
        }

        LOGGER.trace(getMarker(), Messages.getString("HtmlParser.20"), value, //$NON-NLS-1$
                Util.LINE);
        LOGGER.trace(getMarker(), Messages.getString("HtmlParser.21"), //$NON-NLS-1$
                Util.LINE);

        return value;
    }
}
