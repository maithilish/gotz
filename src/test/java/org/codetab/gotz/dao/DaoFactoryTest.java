package org.codetab.gotz.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.dao.jdo.JdoDaoFactory;
import org.codetab.gotz.di.DInjector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DaoFactory tests.
 * @author Maithilish
 *
 */
public class DaoFactoryTest {

    @Mock
    private DInjector dInjector;

    @InjectMocks
    private DaoFactory daoFactory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDaoFactoryJDO() {
        daoFactory.getDaoFactory(ORM.JDO);
        verify(dInjector).instance(JdoDaoFactory.class);
    }

    @Test
    public void testGetDaoFactoryDefault() {
        daoFactory.getDaoFactory(ORM.DEFUALT);
        verify(dInjector).instance(JdoDaoFactory.class);
    }

    @Test
    public void testGetDaoFactoryJPAShouldThrowException() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactory.getDaoFactory(ORM.JPA);
    }

    @Test
    public void testGetDaoFactoryInstance() {
        DaoFactory df1 = daoFactory.getDaoFactory(ORM.JDO);
        DaoFactory df2 = daoFactory.getDaoFactory(ORM.JDO);

        assertThat(df1).isSameAs(df2);
    }

    @Test
    public void testGetLocatorDao() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactory.getLocatorDao();
    }

    @Test
    public void testGetDocumentDao() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactory.getDocumentDao();
    }

    @Test
    public void testGetDataDefDao() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactory.getDataDefDao();
    }

    @Test
    public void testGetDataDao() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactory.getDataDao();
    }

}
