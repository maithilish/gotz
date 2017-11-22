package org.codetab.gotz.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.model.Bean;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * JAXBXoc tests.
 * @author Maithilish
 *
 */
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
    public void testUnmarshallFromStream()
            throws FileNotFoundException, JAXBException {
        // given
        IOHelper rs = new IOHelper();
        InputStream xmlStream =
                rs.getInputStream("/testdefs/beanservice/bean.xml");

        // when
        List<Bean> acutalBeans = jaxbXoc.unmarshall(xmlStream, Bean.class);

        // then
        assertThat(acutalBeans.size()).isEqualTo(2);
        assertThat(acutalBeans).contains(bean1, bean2);
    }

    // @Test
    // public void testMarshall() throws JAXBException, IOException {
    // // given
    // IOHelper rs = new IOHelper();
    // InputStream xmlStream =
    // rs.getInputStream("/testdefs/beanservice/bean.xml");
    // String expected = IOUtils.toString(xmlStream, "UTF-8");
    // expected = StringUtils.deleteWhitespace(expected);
    //
    // ObjectFactory of = new ObjectFactory();
    // Wrapper w = of.createWrapper();
    // w.getAny().addAll(beansList);
    // JAXBElement<Wrapper> we = of.createWrapper(w);
    // String actual = jaxbXoc.marshall(we, w).toString();
    // actual = StringUtils.deleteWhitespace(actual);
    // // then
    // assertThat(expected).contains(actual);
    // }
}
