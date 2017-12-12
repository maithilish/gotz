package org.codetab.gotz.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Validate XML with schema.
 * @author Maithilish
 *
 */
public class XMLValidator {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(XMLValidator.class);

    /**
     * ResourceStream.
     */
    @Inject
    private IOHelper ioHelper;

    /**
     * <p>
     * Validate XML file with schema file.
     * <p>
     * File name should start with / and available in classpath. For example,
     * /bean.xml and /schema/gotz.xsd validates bean.xml at root of classpath
     * with gotz.xsd that exists in schema folder in classpath.
     * @param xmlFile
     *            XML file name starts with / and exists in classpath
     * @param schemaFile
     *            schema file name starts with / and exists in classpath
     * @return true if valid XML
     * @throws SAXException
     *             on parse error
     * @throws IOException
     *             on IO error
     */
    public boolean validate(final String xmlFile, final String schemaFile) {
        LOGGER.debug("validate : [{}] with [{}]", xmlFile, schemaFile);

        Validate.notNull(xmlFile, "xmlFile must not be null");
        Validate.notNull(schemaFile, "schemaFile must not be null");
        Validate.validState(ioHelper != null, "ioHelper is null");

        try (InputStream xmlStream = ioHelper.getInputStream(xmlFile);) {
            URL schemaURL = ioHelper.getURL(schemaFile);
            validate(xmlStream, schemaURL);
            LOGGER.info("validation passed {} + {}", xmlFile, schemaFile);
        } catch (SAXException | IOException e) {
            throw new CriticalException("XML validation failed [" + xmlFile
                    + "] [" + schemaFile + "]", e);
        }
        return true;
    }

    /**
     * <p>
     * Validate XML stream with schema stream.
     * @param xmlStream
     *            XML stream
     * @param schemaStream
     *            schema stream
     * @return true if valid XML
     * @throws SAXException
     *             on parse error
     * @throws IOException
     *             on IO error
     */
    public boolean validate(final InputStream xmlStream,
            final InputStream schemaStream) throws IOException, SAXException {
        Validate.notNull("xmlStream", "xmlStream must not be null");
        Validate.notNull("schemaStream", "schemaStream must not be null");

        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ValidationErrorHandler());
        validator.validate(new StreamSource(xmlStream));
        return true;
    }

    /**
     * <p>
     * Validate XML stream with schema URL. Use this for xsd with
     * imports/includes.
     * @param xmlStream
     *            XML stream
     * @param schemaStream
     *            schema stream
     * @return true if valid XML
     * @throws SAXException
     *             on parse error
     * @throws IOException
     *             on IO error
     */
    public boolean validate(final InputStream xmlStream, final URL schemaURL)
            throws IOException, SAXException {
        Validate.notNull("xmlStream", "xmlStream must not be null");
        Validate.notNull("schemaStream", "schemaStream must not be null");

        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaURL);
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ValidationErrorHandler());
        validator.validate(new StreamSource(xmlStream));
        return true;
    }

    /**
     * <p>
     * ErrorHandler to log custom descriptive error message.
     * @author Maithilish
     *
     */
    public static class ValidationErrorHandler implements ErrorHandler {
        /**
         * logger.
         */
        private static final Logger LOGGER =
                LoggerFactory.getLogger("Validator");

        @Override
        public void error(final SAXParseException exception)
                throws SAXException {
            String message = exception.getLocalizedMessage();
            LOGGER.error("{}", message);
            if (StringUtils.startsWith(message, "cvc-elt.1.a")) {
                message = Util.join(
                        "possible cause : document does not have namespace (xmlns) ",
                        "while schema has targetNamespace");
            }
            if (StringUtils.contains(message, "nonEmptyString")) {
                message =
                        "possible cause : some element or attribute contains empty string";
            }
            throw new SAXException(message, exception);
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
