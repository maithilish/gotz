package org.codetab.gotz.model.helper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.StrSubstitutor;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.misc.SimpleNamespaceContext;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FieldsHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FieldsHelper.class);

    public String getValue(final String xpathExpression, final Node node)
            throws FieldsException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext(node));
        try {
            String value = xpath.evaluate(xpathExpression, node);
            if (StringUtils.isBlank(value)) {
                throw new FieldsException(
                        Util.buildString("blank or no node returned for [",
                                xpathExpression, "]"));
            }
            return value;
        } catch (XPathExpressionException e) {
            throw new FieldsException(xpathExpression, e);
        }
    }

    /**
     * Returns first non blank value, otherwise, throws XFieldException.
     *
     * @param xpathExpression
     * @param nodes
     * @return
     * @throws FieldsException
     *             on XPath error or if no matching node
     */
    public String getFirstValue(final String xpathExpression,
            final XField xField) throws FieldsException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                String value = xpath.evaluate(xpathExpression, node);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (XPathExpressionException e) {
                throw new FieldsException(xpathExpression, e);
            }

        }
        throw new FieldsException(Util.buildString(
                "blank or no node returned for [", xpathExpression, "]"));
    }

    /**
     * Returns last non blank value, otherwise, throws XFieldException.
     *
     * @param xpathExpression
     * @param nodes
     * @return
     * @throws FieldsException
     *             on XPath error or if no matching node
     */
    public String getLastValue(final String xpathExpression,
            final XField xField) throws FieldsException {
        String value = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            String val;
            try {
                val = xpath.evaluate(xpathExpression, node);
            } catch (XPathExpressionException e) {
                throw new FieldsException(xpathExpression, e);
            }
            if (StringUtils.isNotBlank(val)) {
                value = val;
            }
        }
        if (StringUtils.isBlank(value)) {
            throw new FieldsException(Util.buildString(
                    "blank or no node returned for [", xpathExpression, "]"));
        }
        return value;
    }

    public List<String> getValues(final String xpathExpression,
            final XField xField) throws FieldsException {
        List<String> values = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                NodeList items = getNodes(node, xpathExpression);
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    String value = item.getTextContent();
                    values.add(value);
                }
            } catch (XPathExpressionException e) {
                throw new FieldsException(e);
            }
        }
        return values;
    }

    private NamespaceContext getNamespaceContext(final Node node) {
        String prefix = node.getPrefix();
        String ns = node.lookupNamespaceURI(prefix);
        // node is document then get prefix and ns from root element
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            Element root = ((Document) node).getDocumentElement();
            prefix = root.getPrefix();
            ns = root.lookupNamespaceURI(prefix);
        }
        SimpleNamespaceContext nsc = new SimpleNamespaceContext(prefix, ns);
        return nsc;
    }

    public boolean isDefined(final String xpathExpression,
            final XField xField) {
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
        } catch (FieldsException e) {
            return false;
        }
    }

    public boolean isAnyDefined(final XField xField,
            final String... xpathExpressions) {
        for (String xpathExpression : xpathExpressions) {
            if (isDefined(xpathExpression, xField)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTrue(final String xpathExpression, final XField xField)
            throws FieldsException {
        String value = getLastValue(xpathExpression, xField);
        return StringUtils.equalsIgnoreCase(value, "true");
    }

    public String getLabel(final XField xField) throws FieldsException {
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
     * @throws FieldsException
     */
    public List<XField> split(final String xpathExpression, final XField xField)
            throws FieldsException {
        // TODO try for optimization (in same or separate method) deep copy or
        // reference to nodes

        List<XField> xFieldList = new ArrayList<>();
        if (xField.getNodes().isEmpty()) {
            return xFieldList;
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : xField.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression,
                        node, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node splitNode = nodeList.item(i);
                    String prefix = splitNode.getPrefix();
                    String ns = splitNode.lookupNamespaceURI(prefix);
                    Document doc = XmlUtils.createDocument(nodeList.item(i),
                            "xfield", prefix, ns);

                    XField copy = new XField();
                    copy.setName(xField.getName());
                    copy.setClazz(xField.getClazz());
                    copy.setGroup(xField.getGroup());
                    copy.getNodes().add(doc);
                    xFieldList.add(copy);
                }

            } catch (XPathExpressionException
                    | ParserConfigurationException e) {
                throw new FieldsException("unable to split xfield", e);
            }
        }
        return xFieldList;
    }

    public XField deepCopy(final XField xField) throws FieldsException {
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
            throw new FieldsException("unable to clone xfield", e);
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

    public Element addElement(final String namespacePrefix, final String name,
            final String text, final String parentNodeXPath,
            final XField xField) throws FieldsException {
        Optional<Node> node = getLastNode(xField);
        if (node.isPresent()) {
            Document doc = null;
            if (node.get() instanceof Document) {
                doc = (Document) node.get();
            } else {
                doc = node.get().getOwnerDocument();
            }
            if (doc == null) {
                String message = Util.buildString("unable to add new element [",
                        name, "][", text, "]. owner document is null");
                throw new FieldsException(message);
            } else {
                String qName = name;
                if (namespacePrefix != null) {
                    qName = Util.buildString(namespacePrefix, ":", name);
                }
                Element element = doc.createElementNS(
                        doc.lookupNamespaceURI(namespacePrefix), qName);
                element.setTextContent(text);
                if (parentNodeXPath == null) {
                    doc.getDocumentElement().appendChild(element);
                } else {
                    try {
                        NodeList nodes = getNodes(doc, parentNodeXPath);
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node location = nodes.item(i);
                            location.appendChild(element);
                        }
                    } catch (XPathExpressionException e) {
                        throw new FieldsException(Util.buildString(
                                "unable to add element [", name, "][", text,
                                "] at xpath [", parentNodeXPath, "]"), e);
                    }
                }
                return element;
            }
        } else {
            String message = Util.buildString("unable to add new element [",
                    name, "][", text, "]. xfield has no nodes");
            throw new FieldsException(message);
        }
    }

    public Element addElement(final String name, final String text,
            final XField xField) throws FieldsException {
        // default namespace
        return addElement(null, name, text, null, xField);
    }

    public Element addElement(final String name, final String text,
            final Node parent) {
        Document doc = null;
        if (parent instanceof Document) {
            doc = (Document) parent;
        } else {
            doc = parent.getOwnerDocument();
        }
        if (doc == null) {
            LOGGER.warn(
                    "unable to add new element [{}][{}]. owner document is null",
                    name, text);
        } else {
            Element element = doc.createElementNS(
                    doc.lookupNamespaceURI(parent.getPrefix()), name);
            element.setTextContent(text);
            parent.appendChild(element);
            return element;
        }
        return null;
    }

    public void addAttribute(final String name, final String text,
            final Node node) {
        Document doc = null;
        doc = node.getOwnerDocument();
        if (doc == null) {
            LOGGER.warn(
                    "unable to add new element [{}][{}]. owner document is null",
                    name, text);
        } else {
            // ((Element) node).setAttributeNS(doc.lookupNamespaceURI(null),
            // name,
            // text);
            ((Element) node).setAttribute(name, text);
        }
    }

    private NodeList getNodes(final Node node, final String xpathExpression)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext(node));
        return (NodeList) xpath.evaluate(xpathExpression, node,
                XPathConstants.NODESET);
    }

    /**
     * <p>
     * Get value as Range.
     * @param fields
     *            list
     * @param name
     *            field name
     * @return range
     * @throws FieldsException
     *             if no such field
     * @throws NumberFormatException
     *             value is not range or minimum is greater than maximum
     */
    public Range<Integer> getRange(final String xpathExpression,
            final XField xField) throws FieldsException, NumberFormatException {
        String value = getLastValue(xpathExpression, xField);

        if (value.startsWith("-")) {
            NumberFormatException e =
                    new NumberFormatException("Invalid Range " + value);
            throw e;
        }
        String[] tokens = StringUtils.split(value, '-');
        if (tokens.length < 1 || tokens.length > 2) {
            NumberFormatException e =
                    new NumberFormatException("Invalid Range " + value);
            throw e;
        }
        Integer min = 0, max = 0;
        if (tokens.length == 1) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[0]);
        }
        if (tokens.length == 2) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[1]);

        }
        if (min > max) {
            NumberFormatException e = new NumberFormatException(
                    "Invalid Range [min > max] " + value);
            throw e;
        }
        return Range.between(min, max);
    }

    public XField createXField() throws FieldsException {
        // no prefix
        return createXField(null);
    }

    public XField createXField(final String namespacePrefix)
            throws FieldsException {
        Document doc;
        try {
            doc = XmlUtils.createDocument("xfield", namespacePrefix,
                    "http://codetab.org/xfield");
            XField xField = new XField();
            xField.getNodes().add(doc);
            return xField;
        } catch (ParserConfigurationException e) {
            throw new FieldsException("unable to create xfield", e);
        }
    }

    public void replaceVariables(final Map<String, String> queries,
            final Map<String, Axis> axisMap) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        // TODO provide some examples and full explanation in javadoc

        for (String key : queries.keySet()) {
            String str = queries.get(key);
            Map<String, String> valueMap = getValueMap(str, axisMap);
            StrSubstitutor ss = new StrSubstitutor(valueMap);
            ss.setVariablePrefix("%{");
            ss.setVariableSuffix("}");
            ss.setEscapeChar('%');
            String patchedStr = ss.replace(str);
            queries.put(key, patchedStr);
        }
    }

    /**
     * <p>
     * Get value map.
     * @param str
     *            string to parse
     * @param map
     *            axis map
     * @return axis value map
     * @throws IllegalAccessException
     *             on error
     * @throws InvocationTargetException
     *             on error
     * @throws NoSuchMethodException
     *             on error
     */
    private Map<String, String> getValueMap(final String str,
            final Map<String, ?> map) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String[] keys = StringUtils.substringsBetween(str, "%{", "}");
        if (keys == null) {
            return null;
        }
        Map<String, String> valueMap = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split("\\.");
            String objKey = parts[0];
            String property = parts[1];
            Object obj = map.get(objKey.toUpperCase());
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(obj, property);
            valueMap.put(key, ConvertUtils.convert(o));
        }
        return valueMap;
    }

    /**
     * <p>
     * Values from prefix list are concated in reverse order and prefixed to
     * input string.
     * <p>
     * Example : for value xyz and two prefixes foo and bar, it returns string
     * barfooxyz.
     * @param prefixes
     *            input list
     * @param value
     *            string to prefix
     * @return string prefixed concated values
     */
    public String prefixValue(final String value, final List<String> prefixes) {
        String pValue = value;
        for (String prefix : prefixes) {
            pValue = prefix + pValue;
        }
        return pValue;
    }

}
