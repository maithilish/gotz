package org.codetab.gotz.step.extract;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Member;
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

    @Inject
    private DocumentHelper documentHelper;

    private Document doc;

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
        try {
            if (!isDocumentLoaded()) {
                throw new IllegalStateException(
                        Messages.getString("JSoupHtmlParser.16")); //$NON-NLS-1$
            }
            InputStream html = getDocumentHTML();
            doc = Jsoup.parse(html, null, "");
            return true;
        } catch (DataFormatException | IOException | IllegalStateException e) {
            String message = Messages.getString("JSoupHtmlParser.17"); //$NON-NLS-1$
            throw new StepRunException(message, e);
        }
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
            if (axis.getValue() == null) {
                String value = getValue(doc, dataDef, member, axis);
                axis.setValue(value);
            }
        }
    }

    @Override
    protected String queryByQuery(final Object page,
            final Map<String, String> queries) {

        Validate.notNull(page, Messages.getString("JSoupHtmlParser.0")); //$NON-NLS-1$
        Validate.notNull(queries, Messages.getString("JSoupHtmlParser.1")); //$NON-NLS-1$

        if (page instanceof Document) {
            return queryBySelector((Document) page, queries);
        } else {
            throw new IllegalStateException(
                    Util.join(Messages.getString("JSoupHtmlParser.2"), //$NON-NLS-1$
                            Document.class.getName()));
        }
    }

    private String queryBySelector(final Document page,
            final Map<String, String> queries) {
        String regionXpathExpr = queries.get("region"); //$NON-NLS-1$
        String xpathExpr = queries.get("field"); //$NON-NLS-1$
        // optional attribute, only for jsoup
        String attr = queries.get("attribute"); //$NON-NLS-1$

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

        LOGGER.trace(getMarker(), Messages.getString("JSoupHtmlParser.6"), //$NON-NLS-1$
                Util.LINE);
        LOGGER.trace(getMarker(),
                Util.join(Messages.getString("JSoupHtmlParser.7"), //$NON-NLS-1$
                        String.valueOf(elements.size()),
                        Messages.getString("JSoupHtmlParser.8"), xpathExpr)); //$NON-NLS-1$

        for (Element element : elements) {
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr); //$NON-NLS-1$
        }
        return elements;
    }

    private String getBySelector(final Elements elements,
            final String xpathExpr, final String attr) {
        final int numOfLines = 5;
        String value = null;

        Elements subElements = elements.select(xpathExpr);
        LOGGER.trace(getMarker(), Messages.getString("JSoupHtmlParser.10"), //$NON-NLS-1$
                Util.LINE);

        LOGGER.trace(getMarker(),
                Util.join(Messages.getString("JSoupHtmlParser.11"), //$NON-NLS-1$
                        String.valueOf(subElements.size()),
                        Messages.getString("JSoupHtmlParser.12"), xpathExpr)); //$NON-NLS-1$

        for (Element element : subElements) {
            if (StringUtils.isBlank(attr)) {
                value = element.ownText();
            } else {
                value = element.attr(attr); // get value by attribute key
            }
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr); //$NON-NLS-1$
        }
        LOGGER.trace(getMarker(), Messages.getString("JSoupHtmlParser.14"), //$NON-NLS-1$
                value, Util.LINE);
        LOGGER.trace(getMarker(), Messages.getString("JSoupHtmlParser.15"), //$NON-NLS-1$
                Util.LINE);

        return value;
    }

    private InputStream getDocumentHTML()
            throws DataFormatException, IOException {
        byte[] bytes = documentHelper.getDocumentObject(getDocument());
        ByteArrayInputStream html = new ByteArrayInputStream(bytes);
        return html;
    }
}
