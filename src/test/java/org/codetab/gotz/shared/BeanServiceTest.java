package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.BeanHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

public class BeanServiceTest {

    @Mock
    private BeanHelper beanFiles;

    @InjectMocks
    private BeanService beanService;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        // given
        DInjector dInjector = new DInjector().instance(DInjector.class);

        // when
        BeanService instanceA = dInjector.instance(BeanService.class);
        BeanService instanceB = dInjector.instance(BeanService.class);

        // then
        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testInit() throws JAXBException, IOException, SAXException,
            ConfigNotFoundException {
        String beanFile = "x.xml";
        String schemaFile = "x.xsd";

        List<Bean> files = getTestBeanFiles();
        given(beanFiles.getBeanFiles()).willReturn(files);

        beanService.init(beanFile, schemaFile);

        InOrder inOrder = inOrder(beanFiles);

        inOrder.verify(beanFiles).setFiles(beanFile, schemaFile);
        inOrder.verify(beanFiles).validateBeanFile();
        inOrder.verify(beanFiles).getBeanFiles();
        inOrder.verify(beanFiles).validateBeanFiles(files);

        inOrder.verify(beanFiles).unmarshalBeanFile("a.xml", Locator.class);
        inOrder.verify(beanFiles).unmarshalBeanFile("b.xml", DataDef.class);
    }

    @Test
    public void testInitThrowsCriticalException() throws JAXBException,
            IOException, SAXException, ConfigNotFoundException {
        String beanFile = "x.xml";
        String schemaFile = "x.xsd";

        given(beanFiles.validateBeanFile()).willThrow(JAXBException.class);

        expected.expect(CriticalException.class);
        beanService.init(beanFile, schemaFile);
    }

    @Test
    public void testGetBeans() throws JAXBException, IOException, SAXException {
        List<Locator> locators = getTestLocators();
        List<DataDef> dataDefs = getTestDataDefs();
        List<Bean> files = getTestBeanFiles();

        given(beanFiles.getBeanFiles()).willReturn(files);
        given(beanFiles.unmarshalBeanFile("a.xml", Locator.class))
                .willReturn(locators);
        given(beanFiles.unmarshalBeanFile("b.xml", DataDef.class))
                .willReturn(dataDefs);

        beanService.init("x", "y");

        List<Locator> actualLocators = beanService.getBeans(Locator.class);
        List<DataDef> actualDataDefs = beanService.getBeans(DataDef.class);

        assertThat(actualLocators).isEqualTo(locators);
        assertThat(actualDataDefs).isEqualTo(dataDefs);
    }

    @Test
    public void testGetBeansDeepClone()
            throws JAXBException, IOException, SAXException {
        List<Locator> locators = getTestLocators();
        List<DataDef> dataDefs = getTestDataDefs();
        List<Bean> files = getTestBeanFiles();

        given(beanFiles.getBeanFiles()).willReturn(files);
        given(beanFiles.unmarshalBeanFile("a.xml", Locator.class))
                .willReturn(locators);
        given(beanFiles.unmarshalBeanFile("b.xml", DataDef.class))
                .willReturn(dataDefs);

        beanService.init("x", "y");

        List<Locator> actualLocators = beanService.getBeans(Locator.class);
        List<DataDef> actualDataDefs = beanService.getBeans(DataDef.class);

        Locator expectedLocator = locators.get(0);
        Locator actualLocator = actualLocators.get(0);

        assertThat(actualLocator).isEqualTo(expectedLocator);
        assertThat(actualLocator).isNotSameAs(expectedLocator);

        expectedLocator = locators.get(1);
        actualLocator = actualLocators.get(1);

        assertThat(actualLocator).isEqualTo(expectedLocator);
        assertThat(actualLocator).isNotSameAs(expectedLocator);

        DataDef expectedDataDef = dataDefs.get(0);
        DataDef actualDataDef = actualDataDefs.get(0);

        assertThat(actualDataDef).isEqualTo(expectedDataDef);
        assertThat(actualDataDef).isNotSameAs(expectedDataDef);
    }

    @Test
    public void testGetCount() throws JAXBException, IOException, SAXException {
        List<Locator> locators = getTestLocators();
        List<DataDef> dataDefs = getTestDataDefs();
        List<Bean> files = getTestBeanFiles();

        given(beanFiles.getBeanFiles()).willReturn(files);
        given(beanFiles.unmarshalBeanFile("a.xml", Locator.class))
                .willReturn(locators);
        given(beanFiles.unmarshalBeanFile("b.xml", DataDef.class))
                .willReturn(dataDefs);

        beanService.init("x", "y");

        long locatorCount = beanService.getCount(Locator.class);
        long dataDefCount = beanService.getCount(DataDef.class);

        assertThat(locatorCount).isEqualTo(2);
        assertThat(dataDefCount).isEqualTo(1);
    }

    private List<Bean> getTestBeanFiles() {
        List<Bean> beans;
        beans = new ArrayList<>();
        Bean bean = new Bean();
        bean.setName("a");
        bean.setClassName("org.codetab.gotz.model.Locator");
        bean.setXmlFile("a.xml");
        beans.add(bean);

        bean = new Bean();
        bean.setName("b");
        bean.setClassName("org.codetab.gotz.model.DataDef");
        bean.setXmlFile("b.xml");
        bean.setSchemaFile("b.xsd");
        beans.add(bean);
        return beans;
    }

    private List<Locator> getTestLocators() {
        List<Locator> list = new ArrayList<>();
        Locator locator = new Locator();
        locator.setName("la");
        list.add(locator);

        locator = new Locator();
        locator.setName("ln");
        list.add(locator);
        return list;
    }

    private List<DataDef> getTestDataDefs() {
        List<DataDef> list = new ArrayList<>();
        DataDef dataDef = new DataDef();
        dataDef.setName("d1");
        list.add(dataDef);
        return list;
    }
}
