package org.codetab.gotz.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.codetab.gotz.model.Wrapper;
import org.codetab.gotz.util.ResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbXoc implements IXoc {

    private final Logger logger = LoggerFactory.getLogger(JaxbXoc.class);

    @Inject
    private ResourceStream resourceStream;

    @Inject
    private JaxbXoc() {
    }

    @Override
    public <T> List<T> unmarshall(final String xmlFile, final Class<T> ofClass)
            throws JAXBException, IOException {
        try (InputStream xmlStream = resourceStream.getInputStream(xmlFile)) {
            return unmarshall(xmlStream, ofClass);
        }
    }

    @Override
    public <T> List<T> unmarshall(final InputStream xmlStream,
            final Class<T> ofClass) throws JAXBException {
        String packageName = ofClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller um = jc.createUnmarshaller();
        logger.debug("unmarshall : type [{}]", ofClass);
        StreamSource xmlSource = new StreamSource(xmlStream);
        Wrapper wrapper = um.unmarshal(xmlSource, Wrapper.class).getValue();
        List<T> list = new ArrayList<T>();
        for (Object e : wrapper.getAny()) {
            @SuppressWarnings("unchecked")
            T t = (T) JAXBIntrospector.getValue(e);
            list.add(t);
        }
        logger.debug("model objects created [{}]", list.size());
        return list;
    }

    @Override
    public StringWriter marshall(final JAXBElement<?> e, final Object o)
            throws JAXBException {
        String packageName = o.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller jm = jc.createMarshaller();
        jm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter result = new StringWriter();
        jm.marshal(e, result);
        logger.debug(result.toString());
        return result;
    }
}
