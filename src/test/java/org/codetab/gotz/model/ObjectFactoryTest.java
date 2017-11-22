package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.Beans;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;

public class ObjectFactoryTest {

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        of = new ObjectFactory();
    }

    @Test
    public void testCreateWrapper() {
        Wrapper actual = of.createWrapper();
        assertThat(actual).isInstanceOf(Wrapper.class);
        assertThat(actual).isNotSameAs(of.createWrapper());
    }

    @Test
    public void testCreateWrapperWrapper() {
        Wrapper value = of.createWrapper();
        JAXBElement<Wrapper> actual = of.createWrapper(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createWrapper(value));
    }

    @Test
    public void testCreateXField() {
        XField actual = of.createXField();
        assertThat(actual).isInstanceOf(XField.class);
        assertThat(actual).isNotSameAs(of.createXField());
    }

    @Test
    public void testCreateXFields() {
        XField value = of.createXField();
        JAXBElement<XField> actual = of.createXfield(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createXfield(value));
    }

    @Test
    public void testCreateLocator() {
        Locator actual = of.createLocator();
        assertThat(actual).isInstanceOf(Locator.class);
        assertThat(actual).isNotSameAs(of.createLocator());
    }

    @Test
    public void testCreateLocatorLocator() {
        Locator value = of.createLocator();
        JAXBElement<Locator> actual = of.createLocator(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createLocator(value));
    }

    @Test
    public void testCreateLocators() {
        Locators actual = of.createLocators();
        assertThat(actual).isInstanceOf(Locators.class);
        assertThat(actual).isNotSameAs(of.createLocators());
    }

    @Test
    public void testCreateLocatorsLocators() {
        Locators value = of.createLocators();
        JAXBElement<Locators> actual = of.createLocators(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createLocators(value));
    }

    @Test
    public void testCreateDocument() {
        Document actual = of.createDocument();
        assertThat(actual).isInstanceOf(Document.class);
        assertThat(actual).isNotSameAs(of.createLocators());
    }

    @Test
    public void testCreateDocumentDocument() {
        Document value = of.createDocument();
        JAXBElement<Document> actual = of.createDocument(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createDocument(value));
    }

    @Test
    public void testCreateBean() {
        Bean actual = of.createBean();
        assertThat(actual).isInstanceOf(Bean.class);
        assertThat(actual).isNotSameAs(of.createBean());
    }

    @Test
    public void testCreateBeanBean() {
        Bean value = of.createBean();
        JAXBElement<Bean> actual = of.createBean(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createBean(value));
    }

    @Test
    public void testCreateBeans() {
        Beans actual = of.createBeans();
        assertThat(actual).isInstanceOf(Beans.class);
        assertThat(actual).isNotSameAs(of.createBeans());
    }

    @Test
    public void testCreateDataDef() {
        DataDef actual = of.createDataDef();
        assertThat(actual).isInstanceOf(DataDef.class);
        assertThat(actual).isNotSameAs(of.createDataDef());
    }

    @Test
    public void testCreateDataDefDataDef() {
        DataDef value = of.createDataDef();
        JAXBElement<DataDef> actual = of.createDatadef(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createDatadef(value));
    }

    @Test
    public void testCreateDAxis() {
        DAxis actual = of.createDAxis();
        assertThat(actual).isInstanceOf(DAxis.class);
        assertThat(actual).isNotSameAs(of.createDAxis());
    }

    @Test
    public void testCreateDAxisDAxis() {
        DAxis value = of.createDAxis();
        JAXBElement<DAxis> actual = of.createAxis(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createAxis(value));
    }

    @Test
    public void testCreateDMember() {
        DMember actual = of.createDMember();
        assertThat(actual).isInstanceOf(DMember.class);
        assertThat(actual).isNotSameAs(of.createDMember());
    }

    @Test
    public void testCreateDMemberDMember() {
        DMember value = of.createDMember();
        JAXBElement<DMember> actual = of.createMember(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createMember(value));
    }

    @Test
    public void testCreateDFilter() {
        DFilter actual = of.createDFilter();
        assertThat(actual).isInstanceOf(DFilter.class);
        assertThat(actual).isNotSameAs(of.createDFilter());
    }

    @Test
    public void testCreateDFilterDFilter() {
        DFilter value = of.createDFilter();
        JAXBElement<DFilter> actual = of.createFilter(value);
        assertThat(actual).isInstanceOf(JAXBElement.class);
        assertThat(actual).isNotSameAs(of.createFilter(value));
    }

}
