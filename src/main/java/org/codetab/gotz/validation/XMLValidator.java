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
import org.codetab.gotz.messages.Messages;
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
        LOGGER.debug(Messages.getString("XMLValidator.0"), xmlFile, schemaFile); //$NON-NLS-1$

        Validate.notNull(xmlFile, Messages.getString("XMLValidator.1")); //$NON-NLS-1$
        Validate.notNull(schemaFile, Messages.getString("XMLValidator.2")); //$NON-NLS-1$
        Validate.validState(ioHelper != null,
                Messages.getString("XMLValidator.3")); //$NON-NLS-1$

        try (InputStream xmlStream = ioHelper.getInputStream(xmlFile);) {
            URL schemaURL = ioHelper.getURL(schemaFile);
            validate(xmlStream, schemaURL);
            LOGGER.info(Messages.getString("XMLValidator.4"), xmlFile, //$NON-NLS-1$
                    schemaFile);
        } catch (SAXException | IOException e) {
            throw new CriticalException(
                    Messages.getString("XMLValidator.5") + xmlFile //$NON-NLS-1$
                            + "] [" + schemaFile + "]", //$NON-NLS-1$ //$NON-NLS-2$
                    e);
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
        Validate.notNull(xmlStream, Messages.getString("XMLValidator.9")); //$NON-NLS-1$ //$NON-NLS-2$
        Validate.notNull(schemaStream, Messages.getString("XMLValidator.11")); //$NON-NLS-1$ //$NON-NLS-2$

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
        Validate.notNull(xmlStream, Messages.getString("XMLValidator.13")); //$NON-NLS-1$ //$NON-NLS-2$
        Validate.notNull(schemaURL, Messages.getString("XMLValidator.15")); //$NON-NLS-1$ //$NON-NLS-2$

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
                LoggerFactory.getLogger(XMLValidator.class); // $NON-NLS-1$

        @Override
        public void error(final SAXParseException exception)
                throws SAXException {
            String message = exception.getLocalizedMessage();
            LOGGER.error("{}", message); //$NON-NLS-1$
            if (StringUtils.startsWith(message, "cvc-elt.1.a")) { //$NON-NLS-1$
                message = Util.join(Messages.getString("XMLValidator.19"), //$NON-NLS-1$
                        Messages.getString("XMLValidator.20")); //$NON-NLS-1$
            }
            if (StringUtils.contains(message, "nonEmptyString")) { //$NON-NLS-1$
                message = Messages.getString("XMLValidator.22"); //$NON-NLS-1$
            }
            throw new SAXException(message, exception);
        }

        @Override
        public void fatalError(final SAXParseException exception)
                throws SAXException {
            LOGGER.error("{}", exception.getLocalizedMessage()); //$NON-NLS-1$
            throw exception;
        }

        @Override
        public void warning(final SAXParseException exception)
                throws SAXException {
            LOGGER.warn("{}", exception.getLocalizedMessage()); //$NON-NLS-1$
            throw exception;
        }

    }
}
