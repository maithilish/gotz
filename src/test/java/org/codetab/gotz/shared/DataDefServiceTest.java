package org.codetab.gotz.shared;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.jdo.DaoUtilFactory;
import org.codetab.gotz.model.DataDef;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataDefServiceTest {

    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        DaoUtilFactory daoUtilFactory = new DaoUtilFactory();
        daoUtil = daoUtilFactory.getUtilDao();

        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.DataDef");
        daoUtil.deleteSchemaForClasses(schemaClasses);
    }

    @Before
    public void setUp() {
        // create schema
        MonitorService.instance().start();
        daoUtil.createSchemaForClasses(schemaClasses);
        DataDefService.instance().getCount(); // to initialize enum
    }

    @After
    public void tearDown() {
        // drop schema
        daoUtil.deleteSchemaForClasses(schemaClasses);
        // clear cache
        daoUtil.clearCache();
    }

    @Test
    public void testInitInvalid() {
        ConfigService.INSTANCE.init(
                "testdefs/datadefservice/invalid/gotz-test.properties",
                "gotz-default.xml");

        MonitorService.instance().start();

        BeanService.instance().init();

        exception.expect(IllegalStateException.class);
        DataDefService.instance().init();
    }

    @Test
    public void testInitValidInsert() {
        MonitorService.instance().start();
        ConfigService.INSTANCE.init(
                "testdefs/datadefservice/valid-v1/gotz-test.properties",
                "gotz-default.xml");
        BeanService.instance().init();
        DataDefService.instance().init();

        List<String> detachGroups = new ArrayList<>();
        List<DataDef> dataDefs = daoUtil.getObjects(DataDef.class, detachGroups);

        assertEquals(2, dataDefs.size());

        long result = dataDefs.stream().filter(d -> d.getName().equals("bs")).count();
        assertEquals(1, result);

        result = dataDefs.stream().filter(d -> d.getName().equals("pl")).count();
        assertEquals(1, result);
    }

    @Test
    public void testInitValidNoChange() {
        MonitorService.instance().start();
        ConfigService.INSTANCE.init(
                "testdefs/datadefservice/valid-v1/gotz-test.properties",
                "gotz-default.xml");
        BeanService.instance().init();
        DataDefService.instance().init();

        List<String> detachGroups = new ArrayList<>();
        List<DataDef> dataDefs = daoUtil.getObjects(DataDef.class, detachGroups);

        assertEquals(2, dataDefs.size());

        long result = dataDefs.stream().filter(d -> d.getName().equals("bs")).count();
        assertEquals(1, result);

        DataDefService.instance().init();

        assertEquals(2, dataDefs.size());

        result = dataDefs.stream().filter(d -> d.getName().equals("bs")).count();
        assertEquals(1, result);

    }

    // @Test
    // public void testGetDataDef() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetDataTemplateString() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetDataTemplateDataDef() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetFilterMap() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetCount() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testTraceDataStructure() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testTraceDataDefs() {
    // fail("Not yet implemented");
    // }

}
