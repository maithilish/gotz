package org.codetab.gotz.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

public interface IXoc {

    List<Object> unmarshall(String xmlFile, String packageName)
            throws JAXBException, IOException;

    List<Object> unmarshall(InputStream xmlStream, String packageName)
            throws JAXBException;

    StringWriter marshall(JAXBElement<?> e, Object o) throws JAXBException;

}
