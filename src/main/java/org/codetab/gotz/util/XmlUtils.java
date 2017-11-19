package org.codetab.gotz.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XmlUtils {

    /**
     * <p>
     * private constructor.
     */
    private XmlUtils() {
    }

    public static Document createDocument(final List<Node> nodes,
            final String rootTagName, final String prefix,
            final String namespace) throws ParserConfigurationException {
        Document document = createDocument(rootTagName, prefix, namespace);
        Element root = document.getDocumentElement();
        for (Node node : nodes) {
            Node importedNode = document.importNode(node, true);
            root.appendChild(importedNode);
        }
        return document;
    }

    public static Document createDocument(final List<Node> nodes,
            final String rootTagName) throws ParserConfigurationException {
        return createDocument(nodes, rootTagName, null, null);
    }

    public static Document createDocument(final NodeList nodes,
            final String rootTagName) throws ParserConfigurationException {
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i));
        }
        return createDocument(list, rootTagName);
    }

    public static Document createDocument(final Node node,
            final String rootTagName) throws ParserConfigurationException {
        return createDocument(node, rootTagName, null, null);
    }

    public static Document createDocument(final Node node,
            final String rootTagName, final String prefix,
            final String namespace) throws ParserConfigurationException {
        List<Node> list = new ArrayList<>();
        list.add(node);
        return createDocument(list, rootTagName, prefix, namespace);
    }

    public static Document createDocument(final String rootTagName,
            final String prefix, final String namespace)
            throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // qname of root element
        String qName = rootTagName;
        if (prefix != null) {
            qName = Util.buildString(prefix, ":", rootTagName);
        }

        Element root = document.createElementNS(namespace, qName);
        document.appendChild(root);
        return document;
    }

    public static Node deepCopy(final Node node)
            throws ParserConfigurationException {
        // transformer.transform adds ns to all nodes so use builder instead
        if (node instanceof Document) {
            Document doc = (Document) node;
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document copy = builder.newDocument();
            Node importedNode = copy.importNode(doc.getDocumentElement(), true);
            copy.appendChild(importedNode);
            return copy;
        } else {
            return node.cloneNode(true);
        }
    }

    public static String toXML(final Document document) { //
        DOMImplementation domImplementation = document.getImplementation();

        DOMImplementationLS domImplementationLS =
                (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
        LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("format-pretty-print",
                Boolean.TRUE);
        LSOutput lsOutput = domImplementationLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");
        StringWriter stringWriter = new StringWriter();
        lsOutput.setCharacterStream(stringWriter);
        lsSerializer.write(document, lsOutput);
        return stringWriter.toString();
    }

    public static String toXML(final NodeList nodes, final boolean pretty,
            final int indent) throws TransformerException {
        Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
        if (pretty) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    String.valueOf(indent));
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
        }
        StreamResult result = new StreamResult(new StringWriter());
        for (int i = 0; i < nodes.getLength(); i++) {
            DOMSource source = new DOMSource(nodes.item(i));
            transformer.transform(source, result);
        }
        return result.getWriter().toString();
    }

    public static String toXML(final List<Node> nodes, final boolean pretty,
            final int indent) throws TransformerException {

        NodeList nodeList = new NodeList() {
            @Override
            public Node item(final int index) {
                return nodes.get(index);
            }

            @Override
            public int getLength() {
                return nodes.size();
            }
        };

        return toXML(nodeList, pretty, indent);
    }

    public static String toXML(final List<Node> nodes)
            throws TransformerException {
        return toXML(nodes, false, 0);
    }

    public static String getDefaultNs(final Node node) {
        return node.lookupNamespaceURI(null);
    }

}
