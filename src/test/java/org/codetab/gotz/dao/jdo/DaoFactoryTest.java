package org.codetab.gotz.dao.jdo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.ConfigService;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <p>
 * DaoFactory tests.
 * @author Maithilish
 *
 */
public class DaoFactoryTest {

    private static JdoDaoFactory daoFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        DInjector dInjector = new DInjector();
        ConfigService configService = dInjector.instance(ConfigService.class);

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        daoFactory = dInjector.instance(JdoDaoFactory.class);
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
}
