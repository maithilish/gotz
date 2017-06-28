package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.codetab.gotz.dao.DaoFactory;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorPersistence {

    static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorPersistence.class);

    @Inject
    private ConfigService configService;
    @Inject
    private DaoFactory daoFactory;

    public Locator loadLocator(final String name, final String group) {
        try {
            ORM orm = configService.getOrmType();
            ILocatorDao dao = daoFactory.getDaoFactory(orm).getLocatorDao();
            Locator existingLocator = dao.getLocator(name, group);
            return existingLocator;
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message =
                    Util.buildString("unable to load [", name, ":", group, "]");
            throw new StepPersistenceException(message, e);
        }
    }

    public Locator loadLocator(final Long id) {
        try {
            ORM orm = configService.getOrmType();
            ILocatorDao dao = daoFactory.getDaoFactory(orm).getLocatorDao();
            return dao.getLocator(id);
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message = Util.buildString("unable to load Locator[id=",
                    String.valueOf(id), "]");
            throw new StepPersistenceException(message, e);
        }
    }

    public void storeLocator(Locator locator) {
        try {
            ORM orm = configService.getOrmType();
            ILocatorDao dao = daoFactory.getDaoFactory(orm).getLocatorDao();
            dao.storeLocator(locator);
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message = Util.buildString("unable to store [",
                    locator.getName(), ":", locator.getGroup(), "]");
            throw new StepPersistenceException(message, e);
        }
    }

}
