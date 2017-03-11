package in.m.picks.validation;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import in.m.picks.util.Util;

public final class XMLValidator {

    static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class);

    private String xmlFile;
    private String schemaFile;

    public String getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(final String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public String getSchemaFile() {
        return schemaFile;
    }

    public void setSchemaFile(final String schemaFile) {
        this.schemaFile = schemaFile;
    }

    public boolean validate() throws JAXBException, IOException, SAXException {
        InputStream xmlStream = Util.getResourceAsStream(xmlFile);
        InputStream schemaStream = Util.getResourceAsStream(schemaFile);

        LOGGER.debug("validate : [{}] with [{}]", xmlFile, schemaFile);

        boolean validXML = true;
        try {
            SchemaFactory sf = SchemaFactory
                    .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new StreamSource(schemaStream));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ValidationErrorHandler());
            validator.validate(new StreamSource(xmlStream));
        } catch (SAXException e) {
            validXML = false;
        }
        if (!validXML) {
            throw new SAXException(
                    "XML validation failed [" + xmlFile + "] [" + schemaFile + "]");
        }
        LOGGER.debug("validated Bean file [{}] with [{}]", xmlFile, schemaFile);
        return false;
    }

    private static class ValidationErrorHandler implements ErrorHandler {
        static final Logger LOGGER = LoggerFactory.getLogger("Validator");

        @Override
        public void error(final SAXParseException exception) throws SAXException {
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
        public void fatalError(final SAXParseException exception) throws SAXException {
            LOGGER.error("{}", exception.getLocalizedMessage());
            throw exception;
        }

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            LOGGER.warn("{}", exception.getLocalizedMessage());
            throw exception;
        }

    }
}
