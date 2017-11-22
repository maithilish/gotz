package org.codetab.gotz.testutil;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codetab.gotz.model.XField;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XFieldBuilder {

    private StringBuilder sb = new StringBuilder();
    private XField instance = new XField();

    public XFieldBuilder add(final String xmlSnippet) {
        sb.append(xmlSnippet);
        return this;
    }

    public XField build(final String nsPrefix) {
        try {
            Node node = createNode(nsPrefix);
            instance.getNodes().add(node);
        } catch (Exception e) {
        }
        return instance;
    }

    private Node createNode(final String nsPrefix)
            throws ParserConfigurationException, SAXException, IOException {

        if (nsPrefix == null) {
            // default
            sb.insert(0, "<xfield xmlns='http://codetab.org/xfield'>");
            sb.append("</xfield>");
        } else {
            // prefix
            String str = "<" + nsPrefix + ":xfield xmlns:" + nsPrefix
                    + "='http://codetab.org/xfield'>";
            sb.insert(0, str);
            sb.append("</");
            sb.append(nsPrefix);
            sb.append(":xfield>");
        }

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(sb.toString()));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(is);
        return doc;
    }

}
