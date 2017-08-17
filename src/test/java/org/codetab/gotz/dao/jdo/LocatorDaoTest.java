package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Locator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * LocatorDao tests.
 * @author Maithilish
 *
 */
public class LocatorDaoTest {

    private static IDaoUtil daoUtil;
    private static PersistenceManagerFactory pmf;
    private static HashSet<String> schemaClasses;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        daoUtil = DaoUtilFactory.getDaoFactory(ORM.JDO).getUtilDao();
        pmf = daoUtil.getPersistenceManagerFactory();

        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.Locator");
        schemaClasses.add("org.codetab.gotz.model.Document");

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
        exceptionRule.expect(NullPointerException.class);
        new LocatorDao(null);
    }

    @Test
    public void testGetLocatorFromEmptyStore() {
        String name = "tname";
        String group = "tgroup";

        LocatorDao locatorDao = new LocatorDao(pmf);
        Locator actualLocator = locatorDao.getLocator(name, group);
        assertNull(actualLocator);

        actualLocator = locatorDao.getLocator("n", "g");
        assertNull(actualLocator);
    }

    @Test
    public void testGetLocatorById() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        Locator actualLocator = locatorDao.getLocator(locator.getId());

        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());
        assertEquals(0, actualLocator.getDocuments().size());
    }

    @Test
    public void testGetLocatorByIdNonExistent() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        exceptionRule.expect(JDOObjectNotFoundException.class);
        locatorDao.getLocator(10L);
    }

    @Test
    public void testGetLocatorByIdFromTwo() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        Locator locator2 = new Locator();
        locator2.setName("tname2");
        locator2.setGroup("tgroup2");
        locator2.setUrl("turl2");
        locatorDao.storeLocator(locator2);

        Locator actualLocator = locatorDao.getLocator(locator.getId());
        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());
        assertEquals(0, actualLocator.getDocuments().size());
    }

    @Test
    public void testGetLocatorByWithDocument() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Date fromDate = new Date();
        Date toDate = new Date();
        String url = "turl";
        String docObject = "testResourceObject";

        Document document = new Document();
        document.setFromDate(fromDate);
        document.setToDate(toDate);
        document.setUrl(url);
        document.setDocumentObject(docObject);

        Locator locator = createLocator();
        locator.getDocuments().add(document);

        locatorDao.storeLocator(locator);

        Locator actualLocator = locatorDao.getLocator(locator.getId());
        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());

        assertEquals(1, actualLocator.getDocuments().size());
        // document (without documentObject)
        Document actualDocument = actualLocator.getDocuments().get(0);

        assertNotNull(actualDocument.getId());
        assertEquals(fromDate, actualDocument.getFromDate());
        assertEquals(toDate, actualDocument.getToDate());
        assertEquals(url, actualDocument.getUrl());

        exceptionRule.expect(JDODetachedFieldAccessException.class);
        actualDocument.getDocumentObject();
    }

    @Test
    public void testStoreLocatorNull() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        exceptionRule.expect(NullPointerException.class);
        locatorDao.storeLocator(null);
    }

    @Test
    public void testStoreLocatorsWithSameNameGroup() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        Locator locator2 = createLocator();

        locatorDao.storeLocator(locator);

        exceptionRule.expect(JDODataStoreException.class);
        locatorDao.storeLocator(locator2);
    }

    @Test
    public void testGetLocator() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        Locator actualLocator =
                locatorDao.getLocator(locator.getName(), locator.getGroup());
        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());
        assertEquals(0, actualLocator.getDocuments().size());
    }

    @Test
    public void testGetLocatorNotUnique() throws SQLException {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        daoUtil.dropConstraint(pmf, "locator", "unique_namegroup");

        Locator locator2 = createLocator();
        locatorDao.storeLocator(locator2);

        exceptionRule.expect(IllegalStateException.class);
        locatorDao.getLocator(locator.getName(), locator.getGroup());
    }

    @Test
    public void testGetLocatorNonExistent() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();
        locatorDao.storeLocator(locator);

        Locator actualLocator = locatorDao.getLocator(locator.getName(), "x");
        assertNull(actualLocator);

        actualLocator = locatorDao.getLocator("x", locator.getGroup());
        assertNull(actualLocator);
    }

    @Test
    public void testGetLocatorFromTwo() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Locator locator = createLocator();

        Locator locator2 = new Locator();
        locator2.setName("tname2");
        locator2.setGroup("tgroup2");
        locator2.setUrl("turl2");

        locatorDao.storeLocator(locator);
        locatorDao.storeLocator(locator2);

        Locator actualLocator =
                locatorDao.getLocator(locator.getName(), locator.getGroup());
        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());
        assertEquals(0, actualLocator.getDocuments().size());
    }

    @Test
    public void testGetLocatorWithDocument() {
        LocatorDao locatorDao = new LocatorDao(pmf);

        Date fromDate = new Date();
        Date toDate = new Date();
        String url = "turl";
        String docObject = "testResourceObject";

        Document document = new Document();
        document.setFromDate(fromDate);
        document.setToDate(toDate);
        document.setUrl(url);
        document.setDocumentObject(docObject);

        Locator locator = createLocator();
        locator.getDocuments().add(document);

        locatorDao.storeLocator(locator);

        Locator actualLocator =
                locatorDao.getLocator(locator.getName(), locator.getGroup());
        assertEquals(locator.getId(), actualLocator.getId());
        assertEquals(locator.getName(), actualLocator.getName());
        assertEquals(locator.getGroup(), actualLocator.getGroup());
        assertNull(actualLocator.getUrl());
        assertEquals(0, actualLocator.getFields().size());

        assertEquals(1, actualLocator.getDocuments().size());
        // document (without documentObject)
        Document actualDocument = actualLocator.getDocuments().get(0);

        assertNotNull(actualDocument.getId());
        assertEquals(fromDate, actualDocument.getFromDate());
        assertEquals(toDate, actualDocument.getToDate());
        assertEquals(url, actualDocument.getUrl());

        exceptionRule.expect(JDODetachedFieldAccessException.class);
        actualDocument.getDocumentObject();
    }

    private Locator createLocator() {
        String name = "tname";
        String group = "tgroup";
        String url = "turl";

        Locator locator = new Locator();
        locator.setName(name);
        locator.setGroup(group);
        locator.setUrl(url);

        return locator;
    }
}
