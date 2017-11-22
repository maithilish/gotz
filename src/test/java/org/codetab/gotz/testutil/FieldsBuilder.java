package org.codetab.gotz.testutil;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codetab.gotz.model.Fields;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FieldsBuilder {

    private StringBuilder sb = new StringBuilder();
    private Fields instance = new Fields();

    public FieldsBuilder add(final String xmlSnippet) {
        sb.append(xmlSnippet);
        return this;
    }

    public Fields build(final String nsPrefix) {
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
            sb.insert(0, "<fields xmlns='http://codetab.org/fields'>");
            sb.append("</fields>");
        } else {
            // prefix
            String str = "<" + nsPrefix + ":fields xmlns:" + nsPrefix
                    + "='http://codetab.org/fields'>";
            sb.insert(0, str);
            sb.append("</");
            sb.append(nsPrefix);
            sb.append(":fields>");
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
