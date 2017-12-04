package org.codetab.gotz.testutil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.misc.SimpleNamespaceContext;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class TestUtil {

    private TestUtil() {
    }

    public static Fields buildFields(final String xml, final String nsPrefix) {
        return new FieldsBuilder().add(xml).build(nsPrefix);
    }

    public static Fields createFields() throws FieldsException {
        // no prefix
        return createFields(null);
    }

    public static Fields createFields(final String namespacePrefix)
            throws FieldsException {
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

    // no fields root
    public static Fields createFieldsWithNamedRoot(final String rootTag,
            final String namespacePrefix) throws FieldsException {
        Document doc;
        try {
            doc = XmlUtils.createDocument(rootTag, namespacePrefix,
                    "http://codetab.org/xfields");
            Fields fields = new Fields();
            fields.getNodes().add(doc);
            return fields;
        } catch (ParserConfigurationException e) {
            throw new FieldsException("unable to create fields", e);
        }
    }

    public static Fields createFields(final String name, final String value)
            throws FieldsException {
        return createFields(name, value, null);
    }

    public static Fields createFields(final String name, final String value,
            final String namespacePrefix) throws FieldsException {
        Fields fields = createFields(namespacePrefix);
        addElement(name, value, namespacePrefix, fields);
        return fields;
    }

    public static Fields createFields(final String name, final String attrName,
            final String value, final String namespacePrefix)
            throws FieldsException {
        Fields fields = createFields(namespacePrefix);
        Node node = addElement(name, "", namespacePrefix, fields);
        addAttribute(attrName, value, node);
        return fields;
    }

    public static Element addElement(final String namespacePrefix,
            final String name, final String text, final String parentNodeXPath,
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
                String message = Util.join("unable to add new element [", name,
                        "][", text, "]. owner document is null");
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
                        throw new FieldsException(Util.join(
                                "unable to add element [", name, "][", text,
                                "] at xpath [", parentNodeXPath, "]"), e);
                    }
                }
                return element;
            }
        } else {
            String message = Util.join("unable to add new element [", name,
                    "][", text, "]. fields has no nodes");
            throw new FieldsException(message);
        }
    }

    public static Element addElement(final String name, final String text,
            final String namespacePrefix, final Fields fields)
            throws FieldsException {
        // default namespace
        return addElement(namespacePrefix, name, text, null, fields);
    }

    public static Element addElement(final String name, final String text,
            final Fields fields) throws FieldsException {
        // default namespace
        return addElement(null, name, text, null, fields);
    }

    public static Element addElement(final String name, final String text,
            final Node parent) {
        Document doc = null;
        if (parent instanceof Document) {
            doc = (Document) parent;
        } else {
            doc = parent.getOwnerDocument();
        }
        if (doc == null) {
        } else {
            Element element = doc.createElementNS(
                    doc.lookupNamespaceURI(parent.getPrefix()), name);
            element.setTextContent(text);
            parent.appendChild(element);
            return element;
        }
        return null;
    }

    public static void addAttribute(final String name, final String text,
            final Node node) {
        Document doc = null;
        doc = node.getOwnerDocument();
        if (doc != null) {
            ((Element) node).setAttribute(name, text);
        }
    }

    public static Node getRootElement(final Fields fields) {
        Optional<Node> last = getLastNode(fields);
        if (last.isPresent()) {
            Node node = last.get();
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                return ((Document) node).getDocumentElement();
            }
        }
        return null;
    }

    public static Optional<Node> getLastNode(final Fields fields) {
        Validate.notNull(fields, "fields must not be null");

        Node node = null;
        int last = fields.getNodes().size() - 1;
        if (last >= 0) {
            node = fields.getNodes().get(last);
        }
        Optional<Node> lastNode = Optional.ofNullable(node);
        return lastNode;
    }

    private static NodeList getNodes(final Node node,
            final String xpathExpression) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext(node));
        return (NodeList) xpath.evaluate(xpathExpression, node,
                XPathConstants.NODESET);
    }

    private static NamespaceContext getNamespaceContext(final Node node) {
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

    public static List<String> readFileAsList(final String fileName) {
        try {
            InputStream is = TestUtil.class.getResourceAsStream(fileName);
            return IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }

    public static void writeListToFile(final List<Object> list,
            final String fileName) throws IOException {
        try (Writer wr = new FileWriter(fileName)) {
            IOUtils.writeLines(list, null, wr);
        }
    }

    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Assert.assertTrue("class must be final",
                Modifier.isFinal(clazz.getModifiers()));
        Assert.assertEquals("There must be only one constructor", 1,
                clazz.getDeclaredConstructors().length);
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible()
                || !Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                Assert.fail("there exists a non-static method:" + method);
            }
        }
    }
}
