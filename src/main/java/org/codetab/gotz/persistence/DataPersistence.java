package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Data Persistence methods.
 * @author Maithilish
 *
 */
public class DataPersistence {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataPersistence.class);

    /**
     * ConfigService.
     */
    @Inject
    private ConfigService configService;
    /**
     * DaoFactoryProvider.
     */
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    /**
     * <p>
     * Load Data from store by datadef id and document id.
     * @param dataDefId
     *            datadef id
     * @param documentId
     *            document id
     * @return data or null
     * @throws StepPersistenceException
     *             on persistence error
     */
    public Data loadData(final long dataDefId, final long documentId) {
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            String message = Util.buildString("unable to load data,",
                    "dataDefId=", String.valueOf(dataDefId), ",documentId=",
                    String.valueOf(documentId));
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Load Data from store by id.
     * @param id
     *            data id
     * @return data or null
     * @throws StepPersistenceException
     *             on persistence error
     */
    public Data loadData(final long id) {
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(id);
            return data;
        } catch (RuntimeException e) {
            String message = Util.buildString("unable to load data,", "id=",
                    String.valueOf(id));
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Store data.
     * @param data
     *            data to store, not null
     * @throws StepPersistenceException
     *             on persistence error
     */
    public void storeData(final Data data) {
        Validate.notNull(data, "data must not be null");

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            dao.storeData(data);
        } catch (RuntimeException e) {
            String message =
                    Util.buildString("unable to store data", data.getName());
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new StepPersistenceException(message, e);
        }
    }
}
