package org.codetab.gotz.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Gotz;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XOBuilder<T> {

    private StringBuilder sb = new StringBuilder();

    public XOBuilder<T> add(final String xml) {
        sb.append(xml);
        return this;
    }

    public List<T> build(final Class<T> ofClass) {
        sb.insert(0,
                "<gotz xmlns='http://codetab.org/gotz' xmlns:xf='http://codetab.org/xfields' >");
        sb.append("</gotz>");
        try {
            return unmarshall(sb.toString(), ofClass);
        } catch (JAXBException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Fields buildField(final String nsPrefix) {
        Fields fields = new Fields();
        try {
            Node node = createNode(nsPrefix);
            fields.getNodes().add(node);
        } catch (Exception e) {
        }
        return fields;
    }

    private List<T> unmarshall(final String xmlString, final Class<T> ofClass)
            throws JAXBException, IOException {
        InputStream xmlStream = IOUtils.toInputStream(xmlString, "UTF-8");
        String packageName = ofClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller um = jc.createUnmarshaller();

        StreamSource xmlSource = new StreamSource(xmlStream);
        Gotz wrapper = um.unmarshal(xmlSource, Gotz.class).getValue();
        List<T> list = new ArrayList<T>();
        for (Object e : wrapper.getAny()) {
            @SuppressWarnings("unchecked")
            T t = (T) JAXBIntrospector.getValue(e);
            list.add(t);
        }

        return list;
    }

    private Node createNode(final String nsPrefix)
            throws ParserConfigurationException, SAXException, IOException {

        if (nsPrefix == null) {
            // default
            sb.insert(0, "<fields xmlns='http://codetab.org/xfields'>");
            sb.append("</fields>");
        } else {
            // prefix
            String str = "<" + nsPrefix + ":fields xmlns:" + nsPrefix
                    + "='http://codetab.org/xfields'>";
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
