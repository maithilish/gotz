package org.codetab.gotz.validation;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.util.ResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLValidator {

    static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class);

    @Inject
    private ResourceStream resourceStream;

    public boolean validate(final String xmlFile, final String schemaFile)
            throws JAXBException, IOException, SAXException {
        try (InputStream xmlStream = resourceStream.getInputStream(xmlFile);
                InputStream schemaStream =
                        resourceStream.getInputStream(schemaFile)) {
            validate(xmlStream, schemaStream);
        }
        return true;
    }

    public boolean validate(final InputStream xmlStream,
            final InputStream schemaStream)
            throws JAXBException, IOException, SAXException {
        LOGGER.debug("validate : [{}] with [{}]", xmlStream, schemaStream);
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema =
                    schemaFactory.newSchema(new StreamSource(schemaStream));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ValidationErrorHandler());
            validator.validate(new StreamSource(xmlStream));
            LOGGER.debug("validated Bean file [{}] with [{}]", xmlStream,
                    schemaStream);
            return true;
        } catch (SAXException e) {
            throw new SAXException("XML validation failed [" + xmlStream + "] ["
                    + schemaStream + "]");
        }
    }

    private static class ValidationErrorHandler implements ErrorHandler {
        static final Logger LOGGER = LoggerFactory.getLogger("Validator");

        @Override
        public void error(final SAXParseException exception)
                throws SAXException {
            String message = exception.getLocalizedMessage();
            LOGGER.error("{}", message);
            if (StringUtils.startsWith(message, "cvc-elt.1.a")) {
                LOGGER.warn("{} {}",
                        "possible cause : validated file does not have namespace",
                        "(xmlns) defined even though schema has targetNamespace");
            }

            throw exception;
        }

        @Override
        public void fatalError(final SAXParseException exception)
                throws SAXException {
            LOGGER.error("{}", exception.getLocalizedMessage());
            throw exception;
        }

        @Override
        public void warning(final SAXParseException exception)
                throws SAXException {
            LOGGER.warn("{}", exception.getLocalizedMessage());
            throw exception;
        }

    }
}
