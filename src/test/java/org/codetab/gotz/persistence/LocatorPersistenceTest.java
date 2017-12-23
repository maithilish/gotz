package org.codetab.gotz.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.dao.jdo.JdoDaoFactory;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.FieldsHelper;
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
 * LocatorPersistence tests.
 * @author Maithilish
 *
 */
public class LocatorPersistenceTest {

    @Mock
    private ConfigService configService;

    @Mock
    private DaoFactoryProvider daoFactoryProvider;

    @Mock
    private JdoDaoFactory jdoDao;

    @Mock
    private ILocatorDao locatorDao;

    @Mock
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private LocatorPersistence locatorPersistence;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoadLocatorByNameGroup() throws FieldsNotFoundException {
        Locator locator = new Locator();

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator("n", "g")).willReturn(locator);

        given(configService.isPersist("gotz.useDataStore")).willReturn(true);

        Locator actual = locatorPersistence.loadLocator("n", "g");

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator("n", "g");
        assertThat(actual).isSameAs(locator);
    }

    @Test
    public void testLoadLocatorByNameGroupShouldThrowException() {
        given(configService.isPersist("gotz.useDataStore")).willReturn(true);
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.loadLocator("n", "g");
    }

    @Test
    public void testLoadByNameGroupNullParams() {
        try {
            locatorPersistence.loadLocator(null, "g");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            locatorPersistence.loadLocator("n", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testLoadLocatorById() {
        Locator locator = new Locator();

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator(1L)).willReturn(locator);

        given(configService.isPersist("gotz.useDataStore")).willReturn(true);

        Locator actual = locatorPersistence.loadLocator(1L);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator(1L);
        assertThat(actual).isSameAs(locator);
    }

    @Test
    public void testLoadLocatorByIdShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        given(configService.isPersist("gotz.useDataStore")).willReturn(true);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.loadLocator(1L);
    }

    @Test
    public void testStoreLocator() throws FieldsNotFoundException {
        Locator locator = new Locator();

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);

        given(configService.isPersist("gotz.useDataStore")).willReturn(true);
        given(fieldsHelper.isTrue("/xf:fields/xf:tasks/xf:persist/xf:locator", //$NON-NLS-1$
                locator.getFields())).willReturn(true);

        locatorPersistence.storeLocator(locator);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).storeLocator(locator);
    }

    @Test
    public void testStoreLocatorShouldThrowException()
            throws FieldsNotFoundException {
        Locator locator = new Locator();

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        given(configService.isPersist("gotz.useDataStore")).willReturn(true);
        given(fieldsHelper.isTrue("/xf:fields/xf:tasks/xf:persist/xf:locator", //$NON-NLS-1$
                locator.getFields())).willReturn(true);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.storeLocator(locator);
    }

    @Test
    public void testStoreNullParams() {
        try {
            locatorPersistence.storeLocator(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("locator must not be null");
        }
    }

}
