package org.codetab.gotz.step.extract;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseParser;
import org.codetab.gotz.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
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
                } catch (FieldsNotFoundException e) {
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

    @Override
    protected String queryByQuery(final Object page,
            final Map<String, String> queries) {

        Validate.notNull(page, "page must not be null");
        Validate.notNull(queries, "queries must not be null");

        if (page instanceof Document) {
            return queryBySelector((Document) page, queries);
        } else {
            throw new IllegalStateException(Util.join(
                    "page must be instance of ", Document.class.getName()));
        }
    }

    private String queryBySelector(final Document page,
            final Map<String, String> queries) {
        if (queries.size() < 2) {
            String message = Util.join("insufficient queries in dataDef [",
                    getDataDefName(), "], unable to get value");
            throw new StepRunException(message);
        }

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

        LOGGER.trace(getMarker(), "<< Query [Region] >>{}", Util.LINE);
        LOGGER.trace(getMarker(), Util.join("Region Nodes ",
                String.valueOf(elements.size()), " for XPATH: ", xpathExpr));

        for (Element element : elements) {
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        return elements;
    }

    private String getBySelector(final Elements elements,
            final String xpathExpr, final String attr) {
        final int numOfLines = 5;
        String value = null;

        Elements subElements = elements.select(xpathExpr);
        LOGGER.trace(getMarker(), "<< Query [Field] >>{}", Util.LINE);

        LOGGER.trace(getMarker(), Util.join("Nodes ",
                String.valueOf(subElements.size()), " for XPATH: ", xpathExpr));

        for (Element element : subElements) {
            if (StringUtils.isBlank(attr)) {
                value = element.ownText();
            } else {
                value = element.attr(attr); // get value by attribute key
            }
            String nodeTraceStr = Util.stripe(element.outerHtml(), numOfLines,
                    getBlockBegin(), getBlockEnd());
            LOGGER.trace(getMarker(), "{}", nodeTraceStr);
        }
        LOGGER.trace(getMarker(), "Node contents : {}{}", value, Util.LINE);
        LOGGER.trace(getMarker(), "<<< Query End >>>{}", Util.LINE);

        return value;
    }

}
