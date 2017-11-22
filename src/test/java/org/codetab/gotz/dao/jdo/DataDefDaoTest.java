package org.codetab.gotz.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.FieldsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * DataDefDao tests.
 * @author Maithilish
 *
 */
public class DataDefDaoTest {

    private static IDaoUtil daoUtil;
    private static PersistenceManagerFactory pmf;
    private static HashSet<String> schemaClasses;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

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
        try {
            new DataDefDao(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("pmf must not be null");
        }
    }

    @Test
    public void testStoreDataDefNewItem() {
        DataDefDao dao = new DataDefDao(pmf);
        DataDef dataDef = createDataDef();

        // when
        dao.storeDataDef(dataDef);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef.getName());

        assertThat(dataDefs.size()).isEqualTo(1);
        DataDef actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef);
        assertThat(actual.getId()).isEqualTo(dataDef.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef.getToDate());
    }

    @Test
    public void testStoreDataDefWithoutChangeTwice() {
        DataDefDao dao = new DataDefDao(pmf);

        Date fromDate = new Date();

        DataDef dataDef1 = createDataDef();
        dataDef1.setFromDate(DateUtils.addDays(fromDate, -2));

        DataDef dataDef2 = createDataDef();
        dataDef2.setFromDate(DateUtils.addDays(fromDate, -1));

        dao.storeDataDef(dataDef1);
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef1.getName());

        assertThat(dataDefs.size()).isEqualTo(2);

        DataDef actual1 = dataDefs.get(0);
        DataDef actual2 = dataDefs.get(1);

        assertThat(actual1).isEqualTo(dataDef1);
        assertThat(actual2).isEqualTo(dataDef1);

        assertThat(actual1).isEqualTo(dataDef2);
        assertThat(actual2).isEqualTo(dataDef2);

        assertThat(actual1.getId()).isNotNull();
        assertThat(actual2.getId()).isNotNull();
        assertThat(actual1.getId()).isNotEqualTo(actual2.getId());

        fromDate = DateUtils.truncate(fromDate, Calendar.SECOND);

        assertThat(DateUtils.truncate(actual1.getFromDate(), Calendar.SECOND))
                .isEqualTo(DateUtils.addDays(fromDate, -2));

        assertThat(DateUtils.truncate(actual1.getToDate(), Calendar.SECOND))
                .isEqualTo(DateUtils.truncate(
                        DateUtils.addSeconds(actual2.getFromDate(), -1),
                        Calendar.SECOND));

        assertThat(actual2.getToDate()).isEqualTo(dataDef2.getToDate());
    }

    @Test
    public void testStoreDataDefWithChangeTwice() {
        DataDefDao dao = new DataDefDao(pmf);

        Date fromDate = new Date();

        DataDef dataDef1 = createDataDef();
        dataDef1.setFromDate(DateUtils.addDays(fromDate, -2));
        dao.storeDataDef(dataDef1);

        DataDef dataDef2 = createDataDefWithChanges();
        dataDef2.setFromDate(DateUtils.addDays(fromDate, -1));
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef1.getName());

        assertThat(dataDefs.size()).isEqualTo(2);

        DataDef actual1 = dataDefs.get(0);
        DataDef actual2 = dataDefs.get(1);

        assertThat(actual1).isEqualTo(dataDef1);
        assertThat(actual2).isEqualTo(dataDef2);

        assertThat(actual1.getId()).isNotNull();
        assertThat(actual2.getId()).isNotNull();
        assertThat(actual1.getId()).isNotEqualTo(actual2.getId());

        fromDate = DateUtils.truncate(fromDate, Calendar.SECOND);

        assertThat(DateUtils.truncate(actual1.getFromDate(), Calendar.SECOND))
                .isEqualTo(DateUtils.addDays(fromDate, -2));

        assertThat(DateUtils.truncate(actual1.getToDate(), Calendar.SECOND))
                .isEqualTo(DateUtils.truncate(
                        DateUtils.addSeconds(actual2.getFromDate(), -1),
                        Calendar.SECOND));

        assertThat(actual2.getToDate()).isEqualTo(dataDef2.getToDate());
    }

    @Test
    public void testStoreDataDefWithChangeSameFromDate() {
        DataDefDao dao = new DataDefDao(pmf);

        Date fromDate = new Date();

        DataDef dataDef1 = createDataDef();
        dataDef1.setFromDate(DateUtils.addDays(fromDate, -1));
        dao.storeDataDef(dataDef1);

        DataDef dataDef2 = createDataDefWithChanges();
        dataDef2.setFromDate(DateUtils.addDays(fromDate, -1));
        dao.storeDataDef(dataDef2);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef1.getName());

        assertThat(dataDefs.size()).isEqualTo(2);

        DataDef actual1 = dataDefs.get(0);
        DataDef actual2 = dataDefs.get(1);

        assertThat(actual1).isEqualTo(dataDef1);
        assertThat(actual2).isEqualTo(dataDef2);

        assertThat(actual1.getId()).isNotNull();
        assertThat(actual2.getId()).isNotNull();
        assertThat(actual1.getId()).isNotEqualTo(actual2.getId());

        fromDate = DateUtils.truncate(fromDate, Calendar.SECOND);

        assertThat(DateUtils.truncate(actual1.getFromDate(), Calendar.SECOND))
                .isEqualTo(DateUtils.addDays(fromDate, -1));

        Date expectedToDate = DateUtils.truncate(
                DateUtils.addSeconds(actual2.getFromDate(), -1),
                Calendar.SECOND);
        if (expectedToDate.before(actual1.getFromDate())) {
            expectedToDate =
                    DateUtils.truncate(actual1.getFromDate(), Calendar.SECOND);
        }
        assertThat(DateUtils.truncate(actual1.getToDate(), Calendar.SECOND))
                .isEqualTo(expectedToDate);

        assertThat(actual2.getToDate()).isEqualTo(dataDef2.getToDate());
    }

    @Test
    public void testStoreNullParams() {

        DataDefDao dao = new DataDefDao(pmf);

        try {
            dao.storeDataDef(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testGetDataDefByDate() {
        DataDefDao dao = new DataDefDao(pmf);

        Date now = new Date();
        Date tMinus2 = DateUtils.addDays(now, -2);
        Date tMinus1 = DateUtils.addDays(now, -1);
        Date tPlus1 = DateUtils.addDays(now, 1);

        // T-2 to T-1
        DataDef dataDef1 = createDataDef();
        dataDef1.setFromDate(tMinus2);
        dataDef1.setToDate(DateUtils.addSeconds(tMinus1, -1));
        dao.storeDataDef(dataDef1);

        // T-1 to T-0
        DataDef dataDef2 = createDataDef();
        dataDef2.setFromDate(tMinus1);
        dataDef2.setToDate(DateUtils.addSeconds(now, -1));
        dao.storeDataDef(dataDef2);

        // T-0 to T+0
        DataDef dataDef3 = createDataDef();
        dataDef3.setFromDate(now);
        dataDef3.setToDate(DateUtils.addSeconds(tPlus1, -1));
        dao.storeDataDef(dataDef3);

        // when tMinus2 - at start time
        List<DataDef> dataDefs = dao.getDataDefs(tMinus2);
        assertThat(dataDefs.size()).isEqualTo(1);
        DataDef actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef1);
        assertThat(actual.getId()).isEqualTo(dataDef1.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef1.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef1.getToDate());

        // when tMinus2 - at end time
        dataDefs = dao.getDataDefs(DateUtils.addSeconds(tMinus1, -1));
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef1);
        assertThat(actual.getId()).isEqualTo(dataDef1.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef1.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef1.getToDate());

        // when tMinus1 - at start time
        dataDefs = dao.getDataDefs(tMinus1);
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef2);
        assertThat(actual.getId()).isEqualTo(dataDef2.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef2.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef2.getToDate());

        // when tMinus1 - at end time
        dataDefs = dao.getDataDefs(DateUtils.addSeconds(now, -1));
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef2);
        assertThat(actual.getId()).isEqualTo(dataDef2.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef2.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef2.getToDate());

        // when now tPlus1 - at start time
        dataDefs = dao.getDataDefs(now);
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef3);
        assertThat(actual.getId()).isEqualTo(dataDef3.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef3.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef3.getToDate());

        // when now tPlus1 - at end time
        dataDefs = dao.getDataDefs(DateUtils.addSeconds(tPlus1, -1));
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef3);
        assertThat(actual.getId()).isEqualTo(dataDef3.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef3.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef3.getToDate());

        // when somewhere in between start and end time
        dataDefs = dao.getDataDefs(DateUtils.addHours(now, 1));
        assertThat(dataDefs.size()).isEqualTo(1);
        actual = dataDefs.get(0);

        assertThat(actual).isEqualTo(dataDef3);
        assertThat(actual.getId()).isEqualTo(dataDef3.getId());
        assertThat(actual.getFromDate()).isEqualTo(dataDef3.getFromDate());
        assertThat(actual.getToDate()).isEqualTo(dataDef3.getToDate());

        // out of range - before tMinus2
        dataDefs = dao.getDataDefs(DateUtils.addSeconds(tMinus2, -1));
        assertThat(dataDefs.size()).isEqualTo(0);

        // out of range - after tPlus2
        dataDefs = dao.getDataDefs(tPlus1);
        assertThat(dataDefs.size()).isEqualTo(0);
    }

    @Test
    public void testGetDataDefByDateMultiple() {
        DataDefDao dao = new DataDefDao(pmf);

        Date now = new Date();
        Date tMinus1 = DateUtils.addDays(now, -1);

        // T-1 to now
        DataDef dataDef1 = createDataDef();
        dataDef1.setName("x");
        dataDef1.setFromDate(tMinus1);
        dataDef1.setToDate(DateUtils.addSeconds(now, -1));
        dao.storeDataDef(dataDef1);

        DataDef dataDef2 = createDataDef();
        dataDef2.setName("y");
        dataDef2.setFromDate(tMinus1);
        dataDef2.setToDate(DateUtils.addSeconds(now, -1));
        dao.storeDataDef(dataDef2);

        // when tMinus2 - at start time
        List<DataDef> dataDefs = dao.getDataDefs(tMinus1);
        assertThat(dataDefs.size()).isEqualTo(2);
        DataDef actual1 = dataDefs.get(0);
        DataDef actual2 = dataDefs.get(1);

        assertThat(actual1).isEqualTo(dataDef1);
        assertThat(actual1.getId()).isEqualTo(dataDef1.getId());
        assertThat(actual1.getFromDate()).isEqualTo(dataDef1.getFromDate());
        assertThat(actual1.getToDate()).isEqualTo(dataDef1.getToDate());

        assertThat(actual2).isEqualTo(dataDef2);
        assertThat(actual2.getId()).isEqualTo(dataDef2.getId());
        assertThat(actual2.getFromDate()).isEqualTo(dataDef2.getFromDate());
        assertThat(actual2.getToDate()).isEqualTo(dataDef2.getToDate());
    }

    @Test
    public void testGetDataDefByDateEmptyStore() {
        DataDefDao dao = new DataDefDao(pmf);

        Date now = new Date();

        List<DataDef> dataDefs = dao.getDataDefs(now);
        assertThat(dataDefs.size()).isEqualTo(0);
    }

    @Test
    public void testGetDataDefsByDateNullParams() {

        DataDefDao dao = new DataDefDao(pmf);

        try {
            Date date = null;
            dao.getDataDefs(date);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("date must not be null");
        }
    }

    @Test
    public void testGetDataDefByName() {
        DataDefDao dao = new DataDefDao(pmf);

        Date now = new Date();
        Date tMinus2 = DateUtils.addDays(now, -2);
        Date tMinus1 = DateUtils.addDays(now, -1);

        // T-2 to T-1
        DataDef dataDef1 = createDataDef();
        dataDef1.setFromDate(tMinus2);
        dataDef1.setToDate(DateUtils.addSeconds(tMinus1, -1));
        dao.storeDataDef(dataDef1);

        // T-1 to T-0
        DataDef dataDef2 = createDataDef();
        dataDef2.setFromDate(tMinus1);
        dataDef2.setToDate(DateUtils.addSeconds(now, -1));
        dao.storeDataDef(dataDef2);

        // different name
        DataDef dataDef3 = createDataDef();
        dataDef3.setName("dd2");
        dataDef3.setFromDate(tMinus1);
        dataDef3.setToDate(DateUtils.addSeconds(now, -1));
        dao.storeDataDef(dataDef3);

        List<DataDef> dataDefs = dao.getDataDefs(dataDef1.getName());
        assertThat(dataDefs.size()).isEqualTo(2);
        DataDef actual1 = dataDefs.get(0);
        DataDef actual2 = dataDefs.get(1);

        assertThat(actual1).isEqualTo(dataDef1);
        assertThat(actual1.getId()).isEqualTo(dataDef1.getId());
        assertThat(actual1.getFromDate()).isEqualTo(dataDef1.getFromDate());
        assertThat(actual1.getToDate()).isEqualTo(dataDef1.getToDate());

        assertThat(actual2).isEqualTo(dataDef2);
        assertThat(actual2.getId()).isEqualTo(dataDef2.getId());
        assertThat(actual2.getFromDate()).isEqualTo(dataDef2.getFromDate());
        assertThat(actual2.getToDate()).isEqualTo(dataDef2.getToDate());

        dataDefs = dao.getDataDefs(dataDef3.getName());
        assertThat(dataDefs.size()).isEqualTo(1);
        DataDef actual3 = dataDefs.get(0);

        assertThat(actual3).isEqualTo(dataDef3);
        assertThat(actual3.getId()).isEqualTo(dataDef3.getId());
        assertThat(actual3.getFromDate()).isEqualTo(dataDef3.getFromDate());
        assertThat(actual3.getToDate()).isEqualTo(dataDef3.getToDate());

        // out of range - before tMinus2
        dataDefs = dao.getDataDefs("xyz");
        assertThat(dataDefs.size()).isEqualTo(0);

    }

    @Test
    public void testGetDataDefByNameEmptyStore() {
        DataDefDao dao = new DataDefDao(pmf);

        List<DataDef> dataDefs = dao.getDataDefs("xyz");
        assertThat(dataDefs.size()).isEqualTo(0);
    }

    @Test
    public void testGetDataDefsByNameNullParams() {

        DataDefDao dao = new DataDefDao(pmf);

        try {
            String name = null;
            dao.getDataDefs(name);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }
    }

    private DataDef createDataDef() {
        int days = 10;
        Date fromDate = DateUtils.addDays(new Date(), -1);
        Date toDate = DateUtils.addDays(fromDate, days);

        DataDef dataDef = new DataDef();
        dataDef.setName("dd1");
        dataDef.setFromDate(fromDate);
        dataDef.setToDate(toDate);

        dataDef.getAxis().addAll(createAxis());
        dataDef.setFields(createFields());

        return dataDef;
    }

    private DataDef createDataDefWithChanges() {
        DataDef dataDef2 = createDataDef();
        Date toDate = DateUtils.addDays(dataDef2.getToDate(), 10);
        dataDef2.setToDate(toDate);
        dataDef2.setFields(
                TestUtil.buildFields("<xf:member name='x' />", "xf"));
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

    private Fields createFields() {

        // @formatter:off

        Fields fields = new FieldsBuilder()
                .add("<xf:a>")
                .add("  <xf:b>b</xf:b>")
                .add("  <xf:c>c</xf:c>")
                .add("</xf:a>")
                .build("xf");

        //@formatter:on

        return fields;
    }

}
