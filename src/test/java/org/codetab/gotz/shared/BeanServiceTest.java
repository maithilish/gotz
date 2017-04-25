package org.codetab.gotz.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.codetab.gotz.model.DataDefs;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.ConfigService.ConfigIndex;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BeanServiceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        ConfigService.INSTANCE.init("testdefs/beanservice/gotz-test.properties",
                "gotz-default.xml");
    }

    @Test
    public final void testBeanService() {
        assertNotNull(BeanService.instance());
        assertSame(BeanService.instance(), BeanService.instance());
    }

    @Test
    public final void testInit() {
        String orgBeanFile = ConfigService.INSTANCE.getConfig("gotz.beanFile");
        Configuration provided = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.PROVIDED);
        provided.setProperty("gotz.beanFile", "xyz");

        MonitorService.instance().start();

        exception.expect(IllegalStateException.class);
        try {
            BeanService.instance().init();
        } catch (IllegalStateException e) {
            provided.setProperty("gotz.beanFile", orgBeanFile);
            throw e;
        }
    }

    @Test
    public final void testGetBeans() {
        List<Locators> locatorsList = BeanService.instance().getBeans(Locators.class);
        assertEquals(1, locatorsList.size());
        List<Locator> locators = locatorsList.get(0).getLocator();
        assertEquals(3, locators.size());

        List<FieldsBase> fieldsList = BeanService.instance().getBeans(FieldsBase.class);
        assertEquals(2, fieldsList.size());
    }

    @Test
    public final void testGetBeansNonExist() {
        List<DataDefs> dataDefs = BeanService.instance().getBeans(DataDefs.class);
        assertEquals(0, dataDefs.size());
    }

    @Test
    public final void testGetCount() {
        assertEquals(1, BeanService.instance().getCount(Locators.class));
        assertEquals(2, BeanService.instance().getCount(FieldsBase.class));
    }

}
