package org.codetab.gotz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.helper.IOHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.xml.sax.SAXException;

/**
 * <p>
 * XMLValidator tests.
 * @author Maithilish
 *
 */
public class XMLValidatorTest {

    @Spy
    private IOHelper ioHelper;

    @InjectMocks
    private XMLValidator validator;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidate() throws JAXBException, IOException, SAXException {

        boolean actual = validator.validate("/testdefs/validation/valid.xml",
                "/testdefs/validation/schema/valid.xsd");

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateNonexistentFile() throws IOException {

        try {
            validator.validate("/testdefs/validation/valid.xml",
                    "/testdefs/validation/schema/xyz.xsd");
        } catch (CriticalException e) {
            testRule.expect(CriticalException.class);
            validator.validate("/testdefs/validation/xyz.xml",
                    "/testdefs/validation/schema/valid.xsd");
        }
    }

    @Test
    public void testValidateResourceClose() throws IOException {
        String xmlFile = "/testdefs/validation/valid.xml";
        String schemaFile = "/testdefs/validation/schema/valid.xsd";

        InputStream xmlStream = Mockito.mock(InputStream.class);
        InputStream schemaStream = Mockito.mock(InputStream.class);

        given(ioHelper.getInputStream(xmlFile)).willReturn(xmlStream);
        given(ioHelper.getInputStream(schemaFile)).willReturn(schemaStream);

        try {
            validator.validate(xmlFile, schemaFile);
        } catch (CriticalException e) {
            verify(xmlStream, times(2)).close();
            // verify(schemaStream, times(2)).close();
        }
    }

    @Test
    public void testValidateInvalidXmlShouldThrowException()
            throws IOException {

        testRule.expect(CriticalException.class);
        validator.validate("/testdefs/validation/invalid.xml",
                "/testdefs/validation/schema/valid.xsd");
    }

    @Test
    public void testValidateNoNamespaceShouldThrowException()
            throws IOException {

        testRule.expect(CriticalException.class);
        validator.validate("/testdefs/validation/noNS.xml",
                "/testdefs/validation/schema/valid.xsd");
    }

    @Test
    public void testValidateNullParams() throws IOException {
        try {
            validator.validate(null, "x");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("xmlFile must not be null");
        }

        try {
            validator.validate("x", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("schemaFile must not be null");
        }
    }

    @Test
    public void testValidateIllegalState()
            throws IOException, IllegalAccessException {
        FieldUtils.writeDeclaredField(validator, "ioHelper", null, true);
        try {
            validator.validate("x", "y");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("ioHelper is null");
        }
    }

}
