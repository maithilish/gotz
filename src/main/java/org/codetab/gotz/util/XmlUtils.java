package org.codetab.gotz.util;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

    // public static Document createDocument(final List<Object> nodes,
    // final String rootTagName, final String ns)
    // throws ParserConfigurationException {
    // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // factory.setNamespaceAware(true);
    // DocumentBuilder builder = factory.newDocumentBuilder();
    // Document document = builder.newDocument();
    // Element root = null;
    // if (ns != null) {
    // root = document.createElementNS(ns, rootTagName);
    // } else {
    // root = document.createElement(rootTagName);
    // }
    // for (Object o : nodes) {
    // Element e = (Element) o;
    // Node importedNode = document.importNode(e, true);
    // root.appendChild(importedNode);
    // }
    // document.appendChild(root);
    // return document;
    // }

    public static Document createDocument(final List<Node> nodes,
            final String rootTagName) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = null;
        if (nodes.size() == 0) {
            root = document.createElement(rootTagName);
        } else {
            String ns = ((Element) nodes.get(0)).lookupNamespaceURI(null);
            if (ns == null) {
                root = document.createElement(rootTagName);
            } else {
                root = document.createElementNS(ns, rootTagName);
            }
            for (Node node : nodes) {
                Node importedNode = document.importNode(node, true);
                root.appendChild(importedNode);
            }
        }
        System.out.println("---" + document.getNamespaceURI());
        document.appendChild(root);

        return document;
    }

    // public static Document deepCopy(final Document document)
    // throws TransformerException {
    // TransformerFactory tfactory = TransformerFactory.newInstance();
    // Transformer tx = tfactory.newTransformer();
    // DOMSource source = new DOMSource(document);
    // DOMResult result = new DOMResult();
    // tx.transform(source, result);
    // return (Document) result.getNode();
    // }

    public static Node deepCopy(final Node node)
            throws ParserConfigurationException {
        // transformer.transform adds ns to all nodes so we use builder
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
}
