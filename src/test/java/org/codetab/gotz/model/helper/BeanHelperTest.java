package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.IXoc;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.validation.XMLValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

/**
 * <p>
 * BeanHelper tests.
 * @author Maithilish
 *
 */
public class BeanHelperTest {

    @Mock
    private XMLValidator xmlValidator;

    @Mock
    private IXoc xoc;

    @InjectMocks
    private BeanHelper beanHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetFiles() throws IllegalAccessException {
        // given
        String beanFile = "x.xml";
        String schemaFile = "x.xsd";

        // when
        beanHelper.setFiles(beanFile, schemaFile);

        // then
        String actualBeanFile = (String) FieldUtils
                .readDeclaredField(beanHelper, "beanFile", true);
        String actualSchemaFile = (String) FieldUtils
                .readDeclaredField(beanHelper, "schemaFile", true);

        assertThat(actualBeanFile).isEqualTo(beanFile);
        assertThat(actualSchemaFile).isEqualTo(schemaFile);
    }

    @Test
    public void testSetFilesNullParams() {
        try {
            beanHelper.setFiles(null, "x");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("beanFileName must not be null");
        }

        try {
            beanHelper.setFiles("x", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("schemaFileName must not be null");
        }
    }

    @Test
    public void testValidateBeanFile() throws ConfigNotFoundException,
            JAXBException, IOException, SAXException {
        // given
        String beanFile = "x.xml";
        String schemaFile = "x.xsd";
        beanHelper.setFiles(beanFile, schemaFile);

        // when
        beanHelper.validateBeanFile();

        // then
        verify(xmlValidator).validate(beanFile, schemaFile);
    }

    @Test
    public void testValidateBeanFileIllegalState()
            throws JAXBException, IOException, SAXException,
            ConfigNotFoundException, IllegalAccessException {
        try {
            beanHelper.validateBeanFile();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("beanFile is null");
        }

        FieldUtils.writeDeclaredField(beanHelper, "beanFile", "x", true);

        try {
            beanHelper.validateBeanFile();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("schemaFile is null");
        }
    }

    @Test
    public void testValidateBeanFiles()
            throws JAXBException, SAXException, IOException {
        // given
        List<Bean> testBeanFiles = getTestBeanFiles();

        // when
        beanHelper.validateBeanFiles(testBeanFiles);

        // then
        InOrder inOrder = inOrder(xmlValidator);
        inOrder.verify(xmlValidator).validate("a.xml", null);
        inOrder.verify(xmlValidator).validate("b.xml", "b.xsd");

        verifyNoMoreInteractions(xmlValidator);
    }

    @Test
    public void testValidateBeanFilesNullParams()
            throws JAXBException, SAXException, IOException {
        try {
            beanHelper.validateBeanFiles(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("beanFiles must not be null");
        }
    }

    @Test
    public void testGetBeanFiles()
            throws JAXBException, IOException, SAXException {
        // given
        String beanFile = "/org/example/x.xml";
        String schemaFile = "/com/example/x.xsd";
        beanHelper.setFiles(beanFile, schemaFile);

        List<Bean> testBeanFiles = getTestBeanFiles();

        given(xoc.unmarshall(any(String.class), eq(Bean.class)))
                .willReturn(testBeanFiles);

        // when
        List<Bean> actualBeanFiles = beanHelper.getBeanFiles();

        // then
        Bean actualBean = actualBeanFiles.get(0);
        assertThat(actualBean.getName()).isEqualTo("a");
        assertThat(actualBean.getClassName()).isEqualTo("org.codetab.A");
        assertThat(actualBean.getXmlFile()).isEqualTo("/org/example/a.xml");
        assertThat(actualBean.getSchemaFile()).isEqualTo("/com/example/x.xsd");

        actualBean = actualBeanFiles.get(1);
        assertThat(actualBean.getName()).isEqualTo("b");
        assertThat(actualBean.getClassName()).isEqualTo("org.codetab.B");
        assertThat(actualBean.getXmlFile()).isEqualTo("/org/example/b.xml");
        assertThat(actualBean.getSchemaFile()).isEqualTo("b.xsd");
    }

    @Test
    public void testGetBeanFilesIllegalState()
            throws JAXBException, IOException, SAXException,
            ConfigNotFoundException, IllegalAccessException {
        try {
            beanHelper.getBeanFiles();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("beanFile is null");
        }

        FieldUtils.writeDeclaredField(beanHelper, "beanFile", "x", true);

        try {
            beanHelper.getBeanFiles();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("schemaFile is null");
        }
    }

    @Test
    public void testUnmarshalBeanFile() throws JAXBException, IOException {
        // given
        String xmlFile = "x.xml";
        List<Bean> beans = new ArrayList<>();
        given(xoc.unmarshall(xmlFile, Bean.class)).willReturn(beans);

        // when
        List<Bean> actual = beanHelper.unmarshalBeanFile(xmlFile, Bean.class);

        // then
        verify(xoc).unmarshall(xmlFile, Bean.class);
        assertThat(actual).isSameAs(beans);
    }

    @Test
    public void testUnmarshalBeanFileNullParams()
            throws JAXBException, SAXException, IOException {
        try {
            beanHelper.unmarshalBeanFile(null, Locator.class);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fileName must not be null");
        }

        try {
            beanHelper.unmarshalBeanFile("x", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("clz must not be null");
        }
    }

    private List<Bean> getTestBeanFiles() {
        List<Bean> beans;
        beans = new ArrayList<>();
        Bean bean = new Bean();
        bean.setName("a");
        bean.setClassName("org.codetab.A");
        bean.setXmlFile("a.xml");
        beans.add(bean);

        bean = new Bean();
        bean.setName("b");
        bean.setClassName("org.codetab.B");
        bean.setXmlFile("b.xml");
        bean.setSchemaFile("b.xsd");
        beans.add(bean);
        return beans;
    }
}
