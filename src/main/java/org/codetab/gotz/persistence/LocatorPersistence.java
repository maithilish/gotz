package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;

/**
 * <p>
 * Locator Persistence methods.
 * @author Maithilish
 *
 */
public class LocatorPersistence {

    /**
     * Config service.
     */
    @Inject
    private ConfigService configService;

    @Inject
    private FieldsHelper fieldsHelper;

    /**
     * DaoFactory provider.
     */
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    /**
     * <p>
     * Loads locator by name and group.
     * @param name
     *            locator name, not null
     * @param group
     *            locator group, not null
     * @return locator
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Locator loadLocator(final String name, final String group) {
        Validate.notNull(name, Messages.getString("LocatorPersistence.0")); //$NON-NLS-1$
        Validate.notNull(group, Messages.getString("LocatorPersistence.1")); //$NON-NLS-1$

        if (!configService.isPersist("gotz.useDataStore")) { //$NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            Locator existingLocator = dao.getLocator(name, group);
            return existingLocator;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.3"), name, //$NON-NLS-1$
                            ":", group, "]"); //$NON-NLS-1$ //$NON-NLS-2$
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Loads locator by id.
     * @param id
     *            id of locator to load, not null
     * @return locator
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Locator loadLocator(final long id) {

        if (!configService.isPersist("gotz.useDataStore")) { //$NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            return dao.getLocator(id);
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.7"), //$NON-NLS-1$
                            String.valueOf(id), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Store locator.
     * @param locator
     *            locator to store, not null
     * @throws StepPersistenceException
     *             if persistence error
     */
    public boolean storeLocator(final Locator locator) {
        Validate.notNull(locator, Messages.getString("LocatorPersistence.9")); //$NON-NLS-1$

        if (!configService.isPersist("gotz.useDataStore")) { //$NON-NLS-1$
            return false;
        }

        boolean persist = configService.isPersist("gotz.persist.locator"); //$NON-NLS-1$
        try {
            persist = fieldsHelper.isTrue(
                    "/xf:fields/xf:tasks/xf:persist/xf:locator", //$NON-NLS-1$
                    locator.getFields());
        } catch (FieldsNotFoundException e) {
        }

        if (!persist) {
            return false;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            dao.storeLocator(locator);
            return true;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.13"), //$NON-NLS-1$
                            locator.getName(), ":", locator.getGroup(), "]"); //$NON-NLS-2$
            throw new StepPersistenceException(message, e);
        }
    }

}
