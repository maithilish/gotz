package org.codetab.gotz.model.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.misc.SimpleNamespaceContext;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XFieldHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(XFieldHelper.class);

    public String getValue(final String xpathExpression, final Node node)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String ns = node.lookupNamespaceURI(null); // default ns
        NamespaceContext nsc =
                new SimpleNamespaceContext(XMLConstants.DEFAULT_NS_PREFIX, ns);
        xpath.setNamespaceContext(nsc);

        String value = xpath.evaluate(xpathExpression, node);
        return value;
    }

    public String getFirstValue(final String xpathExpression,
            final List<Node> nodes) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : nodes) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);

            String value = xpath.evaluate(xpathExpression, node);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    public String getLastValue(final String xpathExpression,
            final List<Node> nodes) throws XPathExpressionException {
        String value = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : nodes) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);
            String val = xpath.evaluate(xpathExpression, node);
            if (StringUtils.isNotBlank(val)) {
                value = val;
            }
        }
        return value;
    }

    public List<String> getValues(final String xpathExpression,
            final List<Node> nodes) throws XPathExpressionException {
        List<String> values = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : nodes) {
            String ns = node.lookupNamespaceURI(null); // default ns
            NamespaceContext nsc = new SimpleNamespaceContext(
                    XMLConstants.DEFAULT_NS_PREFIX, ns);
            xpath.setNamespaceContext(nsc);

            String value = xpath.evaluate(xpathExpression, node);
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values;
    }

    public boolean isDefined(final String xpathExpression, final List<Node> nodes)
            throws XPathExpressionException {
        String value = getFirstValue(xpathExpression, nodes);
        return StringUtils.isNotBlank(value);
    }

    public XField deepCopy(final XField xField) {
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
            throw new StepRunException("unable to clone xfield", e);
        }
    }

    public Optional<Node> getLast(final List<Node> nodes) {
        Validate.notNull(nodes, "nodes must not be null");

        Node node = null;
        int last = nodes.size() - 1;
        if (last >= 0) {
            node = nodes.get(last);
        }
        Optional<Node> lastNode = Optional.ofNullable(node);
        return lastNode;
    }

    public Optional<Node> getFirst(final List<Node> nodes) {
        Validate.notNull(nodes, "nodes must not be null");

        Node node = null;
        int first = 0;
        if (nodes.size() > 0) {
            node = nodes.get(first);
        }
        Optional<Node> firstNode = Optional.ofNullable(node);
        return firstNode;
    }

    public void addElement(final String name, final String text,
            final Node node) {
        Document doc = null;
        if (node instanceof Document) {
            doc = (Document) node;
        } else {
            doc = node.getOwnerDocument();
        }
        if (doc == null) {
            LOGGER.warn(
                    "unable to add new element {} as owner document is null",
                    name);
        } else {
            Element element =
                    doc.createElementNS(doc.lookupNamespaceURI(null), name);
            element.setTextContent(text);
            doc.getDocumentElement().appendChild(element);
        }
    }

}
