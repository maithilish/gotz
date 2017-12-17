package org.codetab.gotz.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

public interface IXoc {

    <T> List<T> unmarshall(String xmlFile, Class<T> ofClass)
            throws JAXBException, IOException;

    <T> List<T> unmarshall(InputStream xmlStream, Class<T> ofClass)
            throws JAXBException;

    List<Object> unmarshall(String xmlFile, String packageName)
            throws JAXBException, IOException;

    List<Object> unmarshall(InputStream xmlStream, String packageName)
            throws JAXBException;

    StringWriter marshall(JAXBElement<?> e, Object o) throws JAXBException;

}
