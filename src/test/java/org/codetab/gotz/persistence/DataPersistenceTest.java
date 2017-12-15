package org.codetab.gotz.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.dao.jdo.JdoDaoFactory;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DataPersistence tests.
 * @author Maithilish
 *
 */
public class DataPersistenceTest {

    @Mock
    private ConfigService configService;

    @Mock
    private DaoFactoryProvider daoFactoryProvider;

    @Mock
    private JdoDaoFactory jdoDao;

    @Mock
    private IDataDao dataDao;

    @InjectMocks
    private DataPersistence dataPersistence;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoadDataByDataDefAndDocumentId() {
        Data data = new Data();
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(1L, 1L)).willReturn(data);

        Data actual = dataPersistence.loadData(1L, 1L);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, dataDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDao();
        inOrder.verify(dataDao).getData(1L, 1L);
        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testLoadDataByDataDefAndDocumentIdShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        dataPersistence.loadData(1L, 1L);
    }

    @Test
    public void testLoadDataById() {
        Data data = new Data();
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(1L)).willReturn(data);

        Data actual = dataPersistence.loadData(1L);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, dataDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDao();
        inOrder.verify(dataDao).getData(1L);
        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testLoadDataByIdShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        dataPersistence.loadData(1L);
    }

    @Test
    public void testStoreData() {
        Data data = new Data();

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDao()).willReturn(dataDao);

        Fields fields = new Fields();
        dataPersistence.storeData(data, fields);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, dataDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDao();
        inOrder.verify(dataDao).storeData(data);
    }

    @Test
    public void testStoreDataShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        Data data = new Data();
        Fields fields = new Fields();

        testRule.expect(StepPersistenceException.class);
        dataPersistence.storeData(data, fields);
    }

    @Test
    public void testStoreNullParams() {
        try {
            dataPersistence.storeData(null, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

}
