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
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.FieldsParseException;
import org.codetab.gotz.misc.SimpleNamespaceContext;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Methods to query or manipulate fields.
 * <p>
 * Xpath query returns blank value when node text is blank or when no matching
 * node is found.
 * </p>
 * <p>
 * When matched node is empty or when no matching nodes are found :
 * <ul>
 * <li>methods documented as lenient, returns blank value.</li>
 * <li>other methods throws {@see FieldsNotFoundException}</li>
 * </ul>
 * </p>
 * <p>
 * In both cases, when xpath expression has errors, then
 * {@see FieldsParseException} is thrown.
 * </p>
 * <p>
 * {@see FieldsException} is thrown by non query methods such as deep copy,
 * fields creation etc.,
 * </p>
 * <p>
 * FieldsParseException is unchecked exception and others are checked exception.
 * </p>
 * @author Maithilish
 *
 */
public class FieldsHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FieldsHelper.class);

    /**
     * Returns last value including blank, otherwise, throws FieldsException.
     * <p>
     * <b>Lenient</b>
     * </p>
     * @param xpathExpression
     * @param nodes
     * @return last value or blank
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public String getValue(final String xpathExpression, final Fields fields) {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        String value = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : fields.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                value = xpath.evaluate(xpathExpression, node);
            } catch (XPathExpressionException e) {
                throw new FieldsParseException(xpathExpression, e);
            }
        }
        return value;
    }

    /**
     * Returns first non blank value, otherwise, throws FieldsException.
     *
     * @param xpathExpression
     * @param nodes
     * @return first non blank value
     * @throws FieldsException
     *             on XPath expression error
     * @throws FieldsNotFoundException
     *             no matching node with non blank content
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public String getFirstValue(final String xpathExpression,
            final Fields fields) throws FieldsNotFoundException {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : fields.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                String value = xpath.evaluate(xpathExpression, node);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (XPathExpressionException e) {
                throw new FieldsParseException(xpathExpression, e);
            }

        }
        throw new FieldsNotFoundException(xpathExpression);
    }

    /**
     * Returns last non blank value, otherwise, throws FieldsException.
     *
     * @param xpathExpression
     * @param nodes
     * @return last non blank value
     * @throws FieldsException
     *             on XPath expression error
     * @throws FieldsNotFoundException
     *             no matching node with non blank content
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public String getLastValue(final String xpathExpression,
            final Fields fields) throws FieldsNotFoundException {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        String value = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : fields.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            String val;
            try {
                val = xpath.evaluate(xpathExpression, node);
            } catch (XPathExpressionException e) {
                throw new FieldsParseException(xpathExpression, e);
            }
            if (StringUtils.isNotBlank(val)) {
                value = val;
            }
        }
        if (StringUtils.isBlank(value)) {
            throw new FieldsNotFoundException(xpathExpression);
        }
        return value;
    }

    /**
     * Returns list of contents (non blank) of matching nodes.
     * @param xpathExpression
     * @param fields
     * @return list of contents of matching nodes
     * @throws FieldsException
     *             on XPath expression error
     * @throws FieldsNotFoundException
     *             when values list is empty
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public List<String> getValues(final String xpathExpression,
            final boolean includeBlanks, final Fields fields)
            throws FieldsNotFoundException {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        List<String> values = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : fields.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                NodeList items = getNodes(node, xpathExpression);
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    String value = item.getTextContent();
                    if (includeBlanks) {
                        values.add(value);
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            values.add(value);
                        }
                    }
                }
            } catch (XPathExpressionException e) {
                throw new FieldsParseException(xpathExpression, e);
            }
        }
        if (values.size() == 0) {
            throw new FieldsNotFoundException(xpathExpression);
        } else {
            return values;
        }
    }

    /**
     * When countEmptyElements is true, then method returns true if matching
     * node is found even if it is empty, otherwise return true only if matching
     * node is found which is non empty.
     * @param xpathExpression
     * @param countEmptyElements
     * @param fields
     * @return boolean
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public boolean isDefined(final String xpathExpression,
            final boolean countEmptyElements, final Fields fields) {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        if (countEmptyElements) {
            String xpath = "boolean(" + xpathExpression + ")";
            try {
                String val = getFirstValue(xpath, fields);
                return Boolean.valueOf(val);
            } catch (FieldsNotFoundException e) {
                return false;
            }
        } else {
            /*
             * getFirstValue returns non blank value or throws
             * FieldsNotFoundException when value is blank/null. If it returns
             * value then methods returns true, otherwise return false.
             *
             */
            try {
                getFirstValue(xpathExpression, fields);
                return true;
            } catch (FieldsNotFoundException e) {
                return false;
            }
        }
    }

    public boolean isDefined(final String xpathExpression,
            final Fields fields) {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        return isDefined(xpathExpression, false, fields);
    }

    /**
     * Returns true if any one of xpath expression is defined.
     * @param fields
     * @param xpathExpressions
     * @return boolean
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public boolean isAnyDefined(final Fields fields,
            final String... xpathExpressions) {

        Validate.notNull(xpathExpressions, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        for (String xpathExpression : xpathExpressions) {
            if (isDefined(xpathExpression, false, fields)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if node is defined and content is true else returns false.
     * @param xpathExpression
     * @param fields
     * @return boolean
     * @throws FieldsException
     *             on XPath expression error
     * @throws FieldsNotFoundException
     *             when no matching node is found
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public boolean isTrue(final String xpathExpression, final Fields fields)
            throws FieldsNotFoundException {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        String value = getLastValue(xpathExpression, fields);
        return StringUtils.equalsIgnoreCase(value, "true");
    }

    // others

    /**
     * Splits fields using xpath. Creates a list of new fields from the nodes
     * returned by xpath. For each node, it creates deep copy of fields and sets
     * the node returned by xpath.
     *
     * <pre>
     *     <fields>
     *        <tasks>
     *           <task name="a">...</task>
     *           <task name="b">...</task>
     *
     * for xpath /fields/tasks/task, returns two fields - one with task a
     * and another with task b
     * </pre>
     *
     * @param xpathExpression
     * @param fields
     * @return
     * @throws FieldsException
     *             if fields nodes are empty or unable to split
     * @throws FieldsParseException
     *             on XPath expression error
     */
    public List<Fields> split(final String xpathExpression, final Fields fields)
            throws FieldsException {
        // TODO try for optimization (in same or separate method) deep copy or
        // reference to nodes

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        if (fields.getNodes().isEmpty()) {
            throw new FieldsException(
                    Util.join("no nodes in fields, unable to split [",
                            xpathExpression, "]"));
        }

        List<Fields> fieldsList = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Node node : fields.getNodes()) {
            xpath.setNamespaceContext(getNamespaceContext(node));
            try {
                NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression,
                        node, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node splitNode = nodeList.item(i);
                    String prefix = splitNode.getPrefix();
                    String ns = splitNode.lookupNamespaceURI(prefix);
                    Document doc = XmlUtils.createDocument(nodeList.item(i),
                            "fields", prefix, ns);

                    Fields copy = new Fields();
                    copy.setName(fields.getName());
                    copy.setClazz(fields.getClazz());
                    copy.setGroup(fields.getGroup());
                    copy.getNodes().add(doc);
                    fieldsList.add(copy);
                }

            } catch (XPathExpressionException
                    | ParserConfigurationException e) {
                throw new FieldsParseException("unable to split fields", e);
            }
        }
        if (fieldsList.isEmpty()) {
            throw new FieldsException(Util.join(
                    "unable to split fields [", xpathExpression, "]"));
        }
        return fieldsList;
    }

    public Fields deepCopy(final Fields fields) throws FieldsException {

        Validate.notNull(fields, "fields must not be null");

        try {
            Fields copy = new Fields();
            copy.setName(fields.getName());
            copy.setClazz(fields.getClazz());
            copy.setGroup(fields.getGroup());
            for (Node node : fields.getNodes()) {
                copy.getNodes().add(XmlUtils.deepCopy(node));
            }
            return copy;
        } catch (ParserConfigurationException e) {
            throw new FieldsException("unable to clone fields", e);
        }
    }

    public Optional<Node> getLastNode(final Fields fields) {

        Validate.notNull(fields, "fields must not be null");

        Node node = null;
        int last = fields.getNodes().size() - 1;
        if (last >= 0) {
            node = fields.getNodes().get(last);
        }
        Optional<Node> lastNode = Optional.ofNullable(node);
        return lastNode;
    }

    public Optional<Node> getFirstNode(final Fields fields) {

        Validate.notNull(fields, "fields must not be null");

        Node node = null;
        int first = 0;
        if (fields.getNodes().size() > 0) {
            node = fields.getNodes().get(first);
        }
        Optional<Node> firstNode = Optional.ofNullable(node);
        return firstNode;
    }

    /**
     *
     * @param namespacePrefix
     * @param name
     * @param text
     * @param parentNodeXPath
     * @param fields
     * @return
     * @throws FieldsException
     *             when unable to get document from fields nodes
     * @throws FieldsParseException
     *             on XPath expression error
     */
    private Element addElement(final String namespacePrefix, final String name,
            final String text, final String parentNodeXPath,
            final Fields fields) throws FieldsException {
        Optional<Node> node = getLastNode(fields);
        if (node.isPresent()) {
            Document doc = null;
            if (node.get() instanceof Document) {
                doc = (Document) node.get();
            } else {
                doc = node.get().getOwnerDocument();
            }
            if (doc == null) {
                String message = Util.join("unable to add new element [",
                        name, "][", text, "]. owner document is null");
                throw new FieldsException(message);
            } else {
                String qName = name;
                if (namespacePrefix != null) {
                    qName = Util.join(namespacePrefix, ":", name);
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
                        throw new FieldsParseException(Util.join(
                                "unable to add element [", name, "][", text,
                                "] at xpath [", parentNodeXPath, "]"), e);
                    }
                }
                return element;
            }
        } else {
            String message = Util.join("unable to add new element [",
                    name, "][", text, "]. fields has no nodes");
            throw new FieldsException(message);
        }
    }

    /**
     *
     * @param name
     * @param text
     * @param fields
     * @return
     * @throws FieldsParseException
     *             on XPath expression error
     * @throws FieldsException
     *             when unable to get document from fields nodes
     */
    public Element addElement(final String name, final String text,
            final Fields fields) throws FieldsException {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(text, "text must not be null");
        Validate.notNull(fields, "fields must not be null");

        // prefixed ns
        return addElement("xf", name, text, null, fields);
    }

    /**
     *
     * @param name
     * @param text
     * @param parent
     * @return
     */
    public Element addElement(final String name, final String text,
            final Node parent) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(text, "text must not be null");
        Validate.notNull(parent, "parent node must not be null");

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

        Validate.notNull(name, "name must not be null");
        Validate.notNull(text, "text must not be null");
        Validate.notNull(node, "node must not be null");

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

        Validate.notNull(node, "node must not be null");
        Validate.notNull(xpathExpression, "xpath must not be null");

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
     * @throws FieldsParseException
     *             on XPath expression error
     * @throws FieldsNotFoundException
     *             when no matching node is found
     */
    public Range<Integer> getRange(final String xpathExpression,
            final Fields fields)
            throws FieldsNotFoundException, NumberFormatException {

        Validate.notNull(xpathExpression, "xpath must not be null");
        Validate.notNull(fields, "fields must not be null");

        String value = getLastValue(xpathExpression, fields);

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

    public Fields createFields() throws FieldsException {
        return createFields("xf");
    }

    public Fields createFields(final String namespacePrefix)
            throws FieldsException {

        Validate.notNull(namespacePrefix, "namespacePrefix must not be null");

        Document doc;
        try {
            doc = XmlUtils.createDocument("fields", namespacePrefix,
                    "http://codetab.org/xfields");
            Fields fields = new Fields();
            fields.getNodes().add(doc);
            return fields;
        } catch (ParserConfigurationException e) {
            throw new FieldsException("unable to create fields", e);
        }
    }

    public void replaceVariables(final Map<String, String> queries,
            final Map<String, Axis> axisMap) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        // TODO provide some examples and full explanation in javadoc

        Validate.notNull(queries, "queries must not be null");
        Validate.notNull(axisMap, "axisMap must not be null");

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

        Validate.notNull(value, "value must not be null");
        Validate.notNull(prefixes, "prefixes must not be null");

        String pValue = value;
        for (String prefix : prefixes) {
            pValue = prefix + pValue;
        }
        return pValue;
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

}
