package org.codetab.gotz.itest.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.dao.jdo.DocumentDao;
import org.codetab.gotz.dao.jdo.LocatorDao;
import org.codetab.gotz.itest.DaoUtilFactory;
import org.codetab.gotz.itest.IDaoUtil;
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
 * DocumentDao tests.
 * @author Maithilish
 *
 */
public class DocumentDaoTestIT {

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
        try {
            new DocumentDao(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("pmf must not be null");
        }
    }

    @Test
    public void testGetDocument() {
        String name = "x";
        String url = "xu";
        String docObj = "xdoc";

        Date now = new Date();
        Date fromDate = DateUtils.addDays(now, -1);
        Date toDate = now;

        Document doc = new Document();
        doc.setName(name);
        doc.setUrl(url);
        doc.setFromDate(fromDate);
        doc.setToDate(toDate);
        doc.setDocumentObject(docObj);

        Locator locator = new Locator();
        locator.getDocuments().add(doc);

        LocatorDao locatorDao = new LocatorDao(pmf);
        locatorDao.storeLocator(locator);

        DocumentDao documentDao = new DocumentDao(pmf);

        // when
        Document actual = documentDao.getDocument(doc.getId());

        assertThat(actual.getName()).isEqualTo(name);
        assertThat(actual.getUrl()).isEqualTo(url);
        assertThat(actual.getFromDate()).isEqualTo(fromDate);
        assertThat(actual.getToDate()).isEqualTo(toDate);
        assertThat(actual.getDocumentObject()).isEqualTo(docObj);
    }

    @Test
    public void testGetDocumentFromMultiple() {
        String docObj1 = "doc1";
        String docObj2 = "doc2";

        Date now = new Date();
        Date fromDate = DateUtils.addDays(now, -1);
        Date toDate = now;

        Document doc1 = new Document();
        doc1.setName("d1");
        doc1.setUrl("u1");
        doc1.setFromDate(fromDate);
        doc1.setToDate(toDate);
        doc1.setDocumentObject(docObj1);

        Document doc2 = new Document();
        doc2.setName("d2");
        doc2.setUrl("u2");
        doc2.setFromDate(fromDate);
        doc2.setToDate(toDate);
        doc2.setDocumentObject(docObj2);

        Locator locator = new Locator();
        locator.getDocuments().add(doc1);
        locator.getDocuments().add(doc2);

        LocatorDao locatorDao = new LocatorDao(pmf);
        locatorDao.storeLocator(locator);

        DocumentDao documentDao = new DocumentDao(pmf);

        // when
        Document actual1 = documentDao.getDocument(doc1.getId());

        assertThat(actual1.getName()).isEqualTo(doc1.getName());
        assertThat(actual1.getUrl()).isEqualTo(doc1.getUrl());
        assertThat(actual1.getFromDate()).isEqualTo(fromDate);
        assertThat(actual1.getToDate()).isEqualTo(toDate);
        assertThat(actual1.getDocumentObject()).isEqualTo(docObj1);

        Document actual2 = documentDao.getDocument(doc2.getId());

        assertThat(actual2.getName()).isEqualTo(doc2.getName());
        assertThat(actual2.getUrl()).isEqualTo(doc2.getUrl());
        assertThat(actual2.getFromDate()).isEqualTo(fromDate);
        assertThat(actual2.getToDate()).isEqualTo(toDate);
        assertThat(actual2.getDocumentObject()).isEqualTo(docObj2);
    }

    @Test
    public void testGetDocumentNonExistent() {
        String name = "x";
        String url = "xu";
        String docObj = "xdoc";

        Date now = new Date();
        Date fromDate = DateUtils.addDays(now, -1);
        Date toDate = now;

        Document doc = new Document();
        doc.setName(name);
        doc.setUrl(url);
        doc.setFromDate(fromDate);
        doc.setToDate(toDate);
        doc.setDocumentObject(docObj);

        Locator locator = new Locator();
        locator.getDocuments().add(doc);

        LocatorDao locatorDao = new LocatorDao(pmf);
        locatorDao.storeLocator(locator);

        DocumentDao docDao = new DocumentDao(pmf);

        testRule.expect(JDOObjectNotFoundException.class);
        docDao.getDocument(100L);
    }

}
