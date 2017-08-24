package org.codetab.gotz.validation;

import org.codetab.gotz.validation.XMLValidator.ValidationErrorHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * ValidationErrorHandler tests.
 * @author Maithilish
 *
 */
public class ValidationErrorHandlerTest {

    private ValidationErrorHandler errorHandler;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        errorHandler = new ValidationErrorHandler();
    }

    @Test
    public void testError() throws SAXException {

        SAXParseException exception = new SAXParseException("x", null);

        testRule.expect(SAXParseException.class);
        errorHandler.error(exception);
    }

    @Test
    public void testErrorNameSpace() throws SAXException {

        SAXParseException exception =
                new SAXParseException("cvc-elt.1.a", null);

        testRule.expect(SAXParseException.class);
        errorHandler.error(exception);
    }

    @Test
    public void testFatalError() throws SAXException {

        SAXParseException exception = new SAXParseException("x", null);

        testRule.expect(SAXParseException.class);
        errorHandler.fatalError(exception);
    }

    @Test
    public void testWarning() throws SAXException {

        SAXParseException exception = new SAXParseException("x", null);

        testRule.expect(SAXParseException.class);
        errorHandler.warning(exception);
    }
}
