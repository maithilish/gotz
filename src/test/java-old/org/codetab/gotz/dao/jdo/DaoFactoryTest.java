package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.junit.Before;
import org.junit.Test;

public class DaoFactoryTest {

    private DaoFactory daoFactory;

    @Before
    public void setUp() {
        daoFactory = new DaoFactory();
    }

    @Test
    public void testGetLocatorDao() {
        ILocatorDao dao = daoFactory.getLocatorDao();

        assertNotNull(dao);
        assertEquals(LocatorDao.class, dao.getClass());
    }

    @Test
    public void testGetDocumentDao() {
        IDocumentDao dao = daoFactory.getDocumentDao();

        assertNotNull(dao);
        assertEquals(DocumentDao.class, dao.getClass());
    }

    @Test
    public void testGetDataDefDao() {
        IDataDefDao dao = daoFactory.getDataDefDao();

        assertNotNull(dao);
        assertEquals(DataDefDao.class, dao.getClass());
    }

    @Test
    public void testGetDataDao() {
        IDataDao dao = daoFactory.getDataDao();

        assertNotNull(dao);
        assertEquals(DataDao.class, dao.getClass());
    }

    @Test
    public void testGetFactory() {
        PersistenceManagerFactory pmf = daoFactory.getFactory();
        assertNotNull(pmf);
    }

}
