package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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

public class DocumentDaoTest {

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
        exception.expect(NullPointerException.class);
        new DocumentDao(null);
    }

    @Test
    public void testGetDocument() {
        String name = "test";
        String url = "testurl";
        String docObj = "testdocumentobject";

        LocatorDao locDao = new LocatorDao(pmf);

        Date fromDate = new Date();
        Date toDate = new Date();

        Document doc = new Document();
        doc.setName(name);
        doc.setUrl(url);
        doc.setFromDate(fromDate);
        doc.setToDate(toDate);
        doc.setDocumentObject(docObj);

        Locator loc = new Locator();
        loc.getDocuments().add(doc);
        locDao.storeLocator(loc);

        loc = locDao.getLocator(loc.getId());
        List<Document> docs = loc.getDocuments();
        assertEquals(1, docs.size());
        Document actualDoc = docs.get(0);
        Long id = actualDoc.getId();

        DocumentDao docDao = new DocumentDao(pmf);
        actualDoc = docDao.getDocument(id);

        assertEquals(name, actualDoc.getName());
        assertEquals(url, actualDoc.getUrl());
        assertEquals(fromDate, actualDoc.getFromDate());
        assertEquals(toDate, actualDoc.getToDate());
        assertEquals(docObj, actualDoc.getDocumentObject());

    }

    @Test
    public void testGetDocumentNonExistent() {
        String name = "test";
        String url = "testurl";
        String docObj = "testdocumentobject";

        LocatorDao locDao = new LocatorDao(pmf);

        Date fromDate = new Date();
        Date toDate = new Date();

        Document doc = new Document();
        doc.setName(name);
        doc.setUrl(url);
        doc.setFromDate(fromDate);
        doc.setToDate(toDate);
        doc.setDocumentObject(docObj);

        Locator loc = new Locator();
        loc.getDocuments().add(doc);
        locDao.storeLocator(loc);

        DocumentDao docDao = new DocumentDao(pmf);

        exception.expect(JDOObjectNotFoundException.class);
        docDao.getDocument(100L);
    }

}
