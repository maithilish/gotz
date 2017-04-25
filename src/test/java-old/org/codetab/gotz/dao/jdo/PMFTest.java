package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.shared.ConfigService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PMFTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPMF() {
        PMF pmf1 = PMF.INSTANCE;
        PMF pmf2 = PMF.INSTANCE;
        assertSame(pmf1, pmf2);
    }

    @Test
    public void testGetConfigProperties()
            throws IOException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Method method = PMF.class.getDeclaredMethod("getConfigProperties", String.class);
        method.setAccessible(true);

        String configFile = ConfigService.INSTANCE.getConfig("gotz.datastore.configFile");
        Properties properties = (Properties) method.invoke(PMF.INSTANCE, configFile);
        assertNotNull(properties);

        configFile = "x.properties";
        exception.expect(InvocationTargetException.class);
        method.invoke(PMF.INSTANCE, configFile);
    }

    @Test
    public void testGetFactory() {
        PersistenceManagerFactory pmf = PMF.INSTANCE.getFactory();
        assertNotNull(pmf);
    }
}
