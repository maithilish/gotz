package org.codetab.gotz.model.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.misc.SimpleNamespaceContext;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XFieldHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(XFieldHelper.class);

    public String getValue(final String xpathExpression, final Node node)
            throws XFieldException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String ns = node.lookupNamespaceURI(null); // default ns
        NamespaceContext nsc =
                new SimpleNamespaceContext(XMLConstants.DEFAULT_NS_PREFIX, ns);
        xpath.setNamespaceContext(nsc);
        try {
            String value = xpath.evaluate(xpathExpression, node);
            if (StringUtils.isBlank(value)) {
                throw new XFieldException(
                        Util.buildString("blank or no node returned for [",
                                xpathExpression, "]"));
            }
            return value;
        } catch (XPathExpressionException e) {
            throw new XFieldException(xpathExpression, e);
        }
    }

    /**
     * Returns first non blank value, otherwise, throws XFieldException.
     *
     * @param xpathExpression
     * @param nodes
     * @return
     * @throws XFieldException
     *             on XPath error or if no matching node
     */
    public String getFirstValue(final String xpathExpression,
            final XField xField) throws XFieldException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);

            try {
                String value = xpath.evaluate(xpathExpression, node);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (XPathExpressionException e) {
                throw new XFieldException(xpathExpression, e);
            }

        }
        throw new XFieldException(Util.buildString(
                "blank or no node returned for [", xpathExpression, "]"));
    }

    /**
     * Returns last non blank value, otherwise, throws XFieldException.
     *
     * @param xpathExpression
     * @param nodes
     * @return
     * @throws XFieldException
     *             on XPath error or if no matching node
     */
    public String getLastValue(final String xpathExpression,
            final XField xField) throws XFieldException {
        String value = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);
            String val;
            try {
                val = xpath.evaluate(xpathExpression, node);
            } catch (XPathExpressionException e) {
                throw new XFieldException(xpathExpression, e);
            }
            if (StringUtils.isNotBlank(val)) {
                value = val;
            }
        }
        if (StringUtils.isBlank(value)) {
            throw new XFieldException(Util.buildString(
                    "blank or no node returned for [", xpathExpression, "]"));
        }
        return value;
    }

    public List<String> getValues(final String xpathExpression,
            final XField xField) throws XFieldException {
        List<String> values = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);

            String value;
            try {
                value = xpath.evaluate(xpathExpression, node);
            } catch (XPathExpressionException e) {
                throw new XFieldException(e);
            }
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values;
    }

    public boolean isDefined(final String xpathExpression, final XField xField)
            throws XFieldException {
        /*
         * getFirstValue returns non blank value or throws XFieldException when
         * value is blank/null or when parse error. If it returns value then
         * methods returns true, if throws exception and cause is Parse error
         * methods throws exception, else return false.
         *
         */
        try {
            getFirstValue(xpathExpression, xField);
            return true;
        } catch (XFieldException e) {
            if (e.getCause() instanceof XPathExpressionException) {
                throw e;
            }
        }
        return false;
    }

    public boolean isTrue(final String xpathExpression, final XField xField)
            throws XFieldException {
        String value = getLastValue(xpathExpression, xField);
        return StringUtils.equalsIgnoreCase(value, "true");
    }

    public String getLabel(final XField xField) throws XFieldException {
        String xpath = "/:xfield/:label";
        return getLastValue(xpath, xField);
    }

    // others

    /**
     * Splits xfield using xpath. Creates a list of new xfields from the nodes
     * returned by xpath. For each node, it creates deep copy of xfield and sets
     * the node returned by xpath.
     *
     * <pre>
     *     <xfield>
     *        <tasks>
     *           <task name="a">...</task>
     *           <task name="b">...</task>
     *
     * for xpath /xfield/tasks/task, returns two xfield - one with task a
     * and another with task b
     * </pre>
     *
     * @param xpathExpression
     * @param xField
     * @return
     * @throws XFieldException
     */
    public List<XField> split(final String xpathExpression, final XField xField)
            throws XFieldException {
        List<XField> xFieldList = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);
            try {
                NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression,
                        node, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Document doc =
                            XmlUtils.createDocument(nodeList.item(i), "xfield");
                    XField copy = new XField();
                    copy.setName(xField.getName());
                    copy.setClazz(xField.getClazz());
                    copy.setGroup(xField.getGroup());
                    copy.getNodes().add(doc);
                    xFieldList.add(copy);
                }

            } catch (XPathExpressionException
                    | ParserConfigurationException e) {
                throw new XFieldException("unable to split", e);
            }
        }
        return xFieldList;
    }

    public XField deepCopy(final XField xField) throws XFieldException {
        Validate.notNull(xField, "xField must not be null");

        try {
            XField copy = new XField();
            copy.setName(xField.getName());
            copy.setClazz(xField.getClazz());
            copy.setGroup(xField.getGroup());
            for (Node node : xField.getNodes()) {
                copy.getNodes().add(XmlUtils.deepCopy(node));
            }
            return copy;
        } catch (ParserConfigurationException e) {
            throw new XFieldException("unable to clone xfield", e);
        }
    }

    public Optional<Node> getLastNode(final XField xField) {
        Validate.notNull(xField, "xField must not be null");

        Node node = null;
        int last = xField.getNodes().size() - 1;
        if (last >= 0) {
            node = xField.getNodes().get(last);
        }
        Optional<Node> lastNode = Optional.ofNullable(node);
        return lastNode;
    }

    public Optional<Node> getFirstNode(final XField xField) {
        Validate.notNull(xField, "xfield must not be null");

        Node node = null;
        int first = 0;
        if (xField.getNodes().size() > 0) {
            node = xField.getNodes().get(first);
        }
        Optional<Node> firstNode = Optional.ofNullable(node);
        return firstNode;
    }

    public void addElement(final String name, final String text,
            final XField xField) {
        Optional<Node> node = getLastNode(xField);
        if (node.isPresent()) {
            Document doc = null;
            if (node.get() instanceof Document) {
                doc = (Document) node.get();
            } else {
                doc = node.get().getOwnerDocument();
            }
            if (doc == null) {
                LOGGER.warn(
                        "unable to add new element [{}][{}]. owner document is null",
                        name, text);
            } else {
                Element element =
                        doc.createElementNS(doc.lookupNamespaceURI(null), name);
                element.setTextContent(text);
                doc.getDocumentElement().appendChild(element);
            }
        } else {
            LOGGER.warn(
                    "unable to add new element [{}][{}]. xfield has no nodes",
                    name, text);
        }
    }

}
