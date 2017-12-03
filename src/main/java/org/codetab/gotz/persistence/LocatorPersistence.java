package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Locator Persistence methods.
 * @author Maithilish
 *
 */
public class LocatorPersistence {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorPersistence.class);

    /**
     * Config service.
     */
    @Inject
    private ConfigService configService;
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
        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            Locator existingLocator = dao.getLocator(name, group);
            return existingLocator;
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message =
                    Util.join("unable to load [", name, ":", group, "]");
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
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            return dao.getLocator(id);
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message = Util.join("unable to load Locator[id=",
                    String.valueOf(id), "]");
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
    public void storeLocator(final Locator locator) {
        Validate.notNull(locator, "locator must not be null");

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            dao.storeLocator(locator);
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message = Util.join("unable to store [",
                    locator.getName(), ":", locator.getGroup(), "]");
            throw new StepPersistenceException(message, e);
        }
    }

}
