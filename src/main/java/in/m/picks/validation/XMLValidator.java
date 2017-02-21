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

public class XMLValidator {

	final Logger logger = LoggerFactory.getLogger(XMLValidator.class);

	private String xmlFile;
	private String schemaFile;

	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	public String getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	public boolean validate() throws JAXBException, IOException, SAXException {
		InputStream xmlStream = Util.getResourceAsStream(xmlFile);
		InputStream schemaStream = Util.getResourceAsStream(schemaFile);

		logger.debug("validate : [{}] with [{}]", xmlFile, schemaFile);

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
		logger.debug("validated Bean file [{}] with [{}]", xmlFile, schemaFile);
		return false;
	}

	private static class ValidationErrorHandler implements ErrorHandler {
		final Logger logger = LoggerFactory.getLogger("Validator");

		@Override
		public void error(SAXParseException exception) throws SAXException {
			String message = exception.getLocalizedMessage();
			logger.error("{}", message);
			if (StringUtils.startsWith(message, "cvc-elt.1.a")) {
				logger.warn(
						"possible cause : validated file does not have namespace (xmlns) defined even though schema has targetNamespace");
			}

			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			logger.error("{}", exception.getLocalizedMessage());
			throw exception;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			logger.warn("{}", exception.getLocalizedMessage());
			throw exception;
		}

	}
}
