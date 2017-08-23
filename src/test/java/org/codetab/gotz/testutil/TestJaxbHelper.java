package org.codetab.gotz.testutil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJaxbHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TestJaxbHelper.class);

    public <T> List<T> unmarshall(final String xmlFile, final Class<T> ofClass)
            throws JAXBException, IOException {
        try (InputStream xmlStream = getInputStream(xmlFile)) {
            return unmarshall(xmlStream, ofClass);
        }
    }

    public <T> List<T> unmarshall(final InputStream xmlStream,
            final Class<T> ofClass) throws JAXBException {
        String packageName = ofClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller um = jc.createUnmarshaller();
        LOGGER.debug("unmarshall : type [{}]", ofClass);
        StreamSource xmlSource = new StreamSource(xmlStream);
        Wrapper wrapper = um.unmarshal(xmlSource, Wrapper.class).getValue();
        List<T> list = new ArrayList<T>();
        for (Object e : wrapper.getAny()) {
            @SuppressWarnings("unchecked")
            T t = (T) JAXBIntrospector.getValue(e);
            list.add(t);
        }
        LOGGER.debug("model objects created [{}]", list.size());
        return list;
    }

    public StringWriter marshall(final JAXBElement<?> e, final Object o)
            throws JAXBException {
        String packageName = o.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller jm = jc.createMarshaller();
        jm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter result = new StringWriter();
        jm.marshal(e, result);
        LOGGER.debug(result.toString());
        return result;
    }

    public InputStream getInputStream(final String fileName)
            throws FileNotFoundException {
        InputStream stream = IOHelper.class.getResourceAsStream(fileName);
        if (stream == null) {
            throw new FileNotFoundException(fileName);
        }
        return stream;
    }
}
