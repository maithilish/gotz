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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JAXB XML Object converter methods.
 * @author Maithilish
 *
 */
public class JaxbXoc implements IXoc {

    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(JaxbXoc.class);

    /**
     * Resource helper.
     */
    @Inject
    private IOHelper ioHelper;

    /**
     * Unmarshal XML file to class.
     * @param <T>
     *            class type
     * @param xmlFile
     *            XML file name
     * @param ofClass
     *            class of objects
     * @return list of objects of type T
     * @throws JAXBException
     *             parse error
     * @throws IOException
     *             IO error
     */
    @Override
    public <T> List<T> unmarshall(final String xmlFile, final Class<T> ofClass)
            throws JAXBException, IOException {
        InputStream xmlStream = null;
        xmlStream = ioHelper.getInputStream(xmlFile);
        List<T> list = unmarshall(xmlStream, ofClass);
        if (xmlStream != null) {
            xmlStream.close();
        }
        return list;
    }

    /**
     * Unmarshal XML stream to class.
     * @param <T>
     *            class type
     * @param xmlStream
     *            XML stream
     * @param ofClass
     *            class of objects
     * @return list of objects of type T
     * @throws JAXBException
     *             parse error
     * @throws IOException
     *             IO error
     */
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
            Object value = JAXBIntrospector.getValue(e);
            if (ofClass.isInstance(value)) {
                @SuppressWarnings("unchecked")
                T t = (T) value;
                list.add(t);
            } else {
                logger.error("unmarshall type {}, but found obj of type {}",
                        ofClass.getName(), value.getClass().getName());
            }
        }
        logger.debug("model objects created [{}]", list.size());
        return list;
    }

    /**
     * Marshal objects to XML string.
     * @param element
     *            JAXBElement
     * @param obj
     *            object
     * @return stringWriter
     * @throws JAXBException
     *             parse error
     */
    @Override
    public StringWriter marshall(final JAXBElement<?> element, final Object obj)
            throws JAXBException {
        String packageName = obj.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller jm = jc.createMarshaller();
        jm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter result = new StringWriter();
        jm.marshal(element, result);
        logger.debug(result.toString());
        return result;
    }
}
