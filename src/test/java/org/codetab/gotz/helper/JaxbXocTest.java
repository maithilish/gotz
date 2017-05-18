package org.codetab.gotz.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.Beans;
import org.codetab.gotz.model.ObjectFactory;
import org.codetab.gotz.util.ResourceStream;
import org.junit.Before;
import org.junit.Test;

public class JaxbXocTest {

    private JaxbXoc jaxbXoc;

    private Bean bean1;
    private Bean bean2;
    private List<Bean> beansList;

    @Before
    public void setUp() throws Exception {
        DInjector di = new DInjector().instance(DInjector.class);
        jaxbXoc = di.instance(JaxbXoc.class);

        bean1 = new Bean();
        bean1.setName("locator");
        bean1.setClassName("org.codetab.gotz.model.Locator");
        bean1.setXmlFile("locator.xml");

        bean2 = new Bean();
        bean2.setName("fields");
        bean2.setClassName("org.codetab.gotz.model.Fields");
        bean2.setXmlFile("fields.xml");
        bean2.setSchemaFile("/schema/gotz.xsd");

        beansList = new ArrayList<>();
        beansList.add(bean1);
        beansList.add(bean2);
    }

    @Test
    public void testUnmarshallFromFile() throws JAXBException, IOException {
        // given
        String xmlFile = "/testdefs/beanservice/bean.xml";

        // when
        List<Bean> acutalBeans = jaxbXoc.unmarshall(xmlFile, Bean.class);

        // then
        assertThat(acutalBeans.size()).isEqualTo(2);
        assertThat(acutalBeans).contains(bean1, bean2);
    }

    @Test
    public void testUnmarshallFromStream() throws FileNotFoundException, JAXBException {
        // given
        ResourceStream rs = new ResourceStream();
        InputStream xmlStream = rs.getInputStream("/testdefs/beanservice/bean.xml");

        // when
        List<Bean> acutalBeans = jaxbXoc.unmarshall(xmlStream, Bean.class);

        // then
        assertThat(acutalBeans.size()).isEqualTo(2);
        assertThat(acutalBeans).contains(bean1, bean2);
    }

    @Test
    public void testMarshall() throws JAXBException, IOException {
        // given
        ResourceStream rs = new ResourceStream();
        InputStream xmlStream = rs.getInputStream("/testdefs/beanservice/bean.xml");
        String expected = IOUtils.toString(xmlStream, "UTF-8");
        expected = StringUtils.deleteWhitespace(expected);

        Beans beans = new Beans();
        beans.getBean().addAll(beansList);
        ObjectFactory of = new ObjectFactory();
        JAXBElement<Beans> we = of.createBeans(beans);

        // when
        String actual = jaxbXoc.marshall(we, beans).toString();
        actual = StringUtils.deleteWhitespace(actual);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
