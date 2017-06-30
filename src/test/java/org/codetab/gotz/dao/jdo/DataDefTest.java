package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataDefTest {

    private static IDaoUtil daoUtil;
    private static PersistenceManagerFactory pmf;
    private static HashSet<String> schemaClasses;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        daoUtil = DaoUtilFactory.getDaoFactory(ORM.JDO).getUtilDao();
        pmf = daoUtil.getPersistenceManagerFactory();

        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.DataDef");
        daoUtil.deleteSchemaForClasses(schemaClasses);
    }

    @Before
    public void setUp() {
        // create schema
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @After
    public void tearDown() {
        // drop schema
        daoUtil.deleteSchemaForClasses(schemaClasses);
        // clear cache
        daoUtil.clearCache();
    }

    @Test
    public void testDao() {
        exception.expect(NullPointerException.class);
        new DataDefDao(null);
    }

    @Test
    public void testStoreDataDef() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dao.storeDataDef(dataDef);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef.getName());

        assertEquals(1, dataDefs.size());

        DataDef actualDef = dataDefs.get(0);

        assertEquals(dataDef, actualDef);
        assertEquals(dataDef.getId(), actualDef.getId());
        assertEquals(dataDef.getFromDate(), actualDef.getFromDate());
        assertEquals(dataDef.getToDate(), actualDef.getToDate());
        assertEquals(createAxis(), actualDef.getAxis());
        assertEquals(createFields(), actualDef.getFields());
    }

    @Test
    public void testStoreDataDefWithoutChangeTwice() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        DataDef dataDef2 = createDataDef();

        dao.storeDataDef(dataDef);
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef.getName());

        assertEquals(1, dataDefs.size());

        DataDef actualDef = dataDefs.get(0);

        assertEquals(dataDef, actualDef);
        assertEquals(dataDef.getId(), actualDef.getId());
        assertEquals(dataDef.getFromDate(), actualDef.getFromDate());
        assertEquals(dataDef.getToDate(), actualDef.getToDate());
        assertEquals(createAxis(), actualDef.getAxis());
        assertEquals(createFields(), actualDef.getFields());
    }

    @Test
    public void testStoreDataDefWithChangeTwice() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dao.storeDataDef(dataDef);

        DataDef dataDef2 = createDataDefWithChanges();

        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef.getName());

        assertEquals(2, dataDefs.size());

        dataDefs = dao.getDataDefs(new Date());

        assertEquals(1, dataDefs.size());
        DataDef actualDef = dataDefs.get(0);

        assertEquals(dataDef2, actualDef);
        assertEquals(dataDef2.getId(), actualDef.getId());
        assertEquals(dataDef2.getFromDate(), actualDef.getFromDate());
        assertEquals(dataDef2.getToDate(), actualDef.getToDate());
        assertEquals(createAxis(), actualDef.getAxis());
        assertEquals(dataDef2.getFields(), actualDef.getFields());
    }

    @Test
    public void testGetDataDef() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dao.storeDataDef(dataDef);

        List<DataDef> dataDefs = dao.getDataDefs(new Date());

        assertEquals(1, dataDefs.size());

        DataDef actualDef = dataDefs.get(0);

        assertEquals(dataDef, actualDef);
        assertEquals(dataDef.getId(), actualDef.getId());
        assertEquals(dataDef.getFromDate(), actualDef.getFromDate());
        assertEquals(dataDef.getToDate(), actualDef.getToDate());
        assertEquals(createAxis(), actualDef.getAxis());
        assertEquals(createFields(), actualDef.getFields());
    }

    @Test
    public void testGetDataDefWithChange() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dao.storeDataDef(dataDef);

        DataDef dataDef2 = createDataDefWithChanges();
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(new Date());

        assertEquals(1, dataDefs.size());
        DataDef actualDef = dataDefs.get(0);

        assertEquals(dataDef2, actualDef);
        assertEquals(dataDef2.getId(), actualDef.getId());
        assertEquals(dataDef2.getFromDate(), actualDef.getFromDate());
        assertEquals(dataDef2.getToDate(), actualDef.getToDate());
        assertEquals(createAxis(), actualDef.getAxis());
        assertEquals(dataDef2.getFields(), actualDef.getFields());
    }

    @Test
    public void testGetDataDefsFromMulti() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dataDef.setName("datadef1");
        dao.storeDataDef(dataDef);

        DataDef dataDef2 = createDataDef();
        dataDef2.setName("datadef2");
        dao.storeDataDef(dataDef2);

        // change
        dataDef2 = createDataDefWithChanges();
        dataDef2.setName("datadef2");
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(new Date());

        assertEquals(2, dataDefs.size());
        DataDef actualDef1, actualDef2;
        if (dataDefs.get(0).getName().equals("datadef1")) {
            actualDef1 = dataDefs.get(0);
            actualDef2 = dataDefs.get(1);
        } else {
            actualDef1 = dataDefs.get(1);
            actualDef2 = dataDefs.get(0);
        }

        assertEquals(dataDef, actualDef1);
        assertEquals(dataDef.getId(), actualDef1.getId());
        assertEquals(dataDef.getFromDate(), actualDef1.getFromDate());
        assertEquals(dataDef.getToDate(), actualDef1.getToDate());
        assertEquals(createAxis(), actualDef1.getAxis());
        assertEquals(dataDef.getFields(), actualDef1.getFields());

        assertEquals(dataDef2, actualDef2);
        assertEquals(dataDef2.getId(), actualDef2.getId());
        assertEquals(dataDef2.getFromDate(), actualDef2.getFromDate());
        assertEquals(dataDef2.getToDate(), actualDef2.getToDate());
        assertEquals(createAxis(), actualDef2.getAxis());
        assertEquals(dataDef2.getFields(), actualDef2.getFields());
    }

    @Test
    public void testGetDataDefsFromMultiWithChange() {
        DataDefDao dao = new DataDefDao(pmf);

        DataDef dataDef = createDataDef();
        dataDef.setName("datadef1");
        dao.storeDataDef(dataDef);

        DataDef dataDef2 = createDataDef();
        dataDef2.setName("datadef2");
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(new Date());

        assertEquals(2, dataDefs.size());
        DataDef actualDef1, actualDef2;
        if (dataDefs.get(0).getName().equals("datadef1")) {
            actualDef1 = dataDefs.get(0);
            actualDef2 = dataDefs.get(1);
        } else {
            actualDef1 = dataDefs.get(1);
            actualDef2 = dataDefs.get(0);
        }

        assertEquals(dataDef, actualDef1);
        assertEquals(dataDef.getId(), actualDef1.getId());
        assertEquals(dataDef.getFromDate(), actualDef1.getFromDate());
        assertEquals(dataDef.getToDate(), actualDef1.getToDate());
        assertEquals(createAxis(), actualDef1.getAxis());
        assertEquals(dataDef.getFields(), actualDef1.getFields());

        assertEquals(dataDef2, actualDef2);
        assertEquals(dataDef2.getId(), actualDef2.getId());
        assertEquals(dataDef2.getFromDate(), actualDef2.getFromDate());
        assertEquals(dataDef2.getToDate(), actualDef2.getToDate());
        assertEquals(createAxis(), actualDef2.getAxis());
        assertEquals(dataDef2.getFields(), actualDef2.getFields());
    }

    private DataDef createDataDef() {
        int days = 10;
        Date fromDate = DateUtils.addDays(new Date(), -1);
        Date toDate = DateUtils.addDays(fromDate, days);

        DataDef dataDef = new DataDef();
        dataDef.setName("testdatadef");
        dataDef.setFromDate(fromDate);
        dataDef.setToDate(toDate);

        dataDef.getAxis().addAll(createAxis());
        dataDef.getFields().addAll(createFields());

        return dataDef;
    }

    private DataDef createDataDefWithChanges() {
        DataDef dataDef2 = createDataDef();
        Field field = new Field();
        field.setName("newfield");
        field.setName("newvalue");
        dataDef2.getFields().add(field);
        return dataDef2;
    }

    private List<DAxis> createAxis() {
        List<DAxis> list = new ArrayList<>();
        DAxis col = new DAxis();
        col.setName("col");
        DAxis row = new DAxis();
        row.setName("row");

        list.add(col);
        list.add(row);

        return list;
    }

    private List<FieldsBase> createFields() {
        List<FieldsBase> list = new ArrayList<>();

        Fields fields = new Fields();
        fields.setName("testfields");
        fields.setValue("testfieldsvalue");

        Field field = new Field();
        field.setName("nestedfield");
        field.setValue("nestedvalue");
        fields.getFields().add(field);

        field = new Field();
        field.setName("testfield");
        field.setValue("testfieldvalue");

        list.add(fields);
        list.add(field);

        return list;
    }

}
