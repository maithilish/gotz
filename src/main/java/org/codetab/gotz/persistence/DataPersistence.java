package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPersistence {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataPersistence.class);

    @Inject
    private ConfigService configService;
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    public Data loadData(final Long dataDefId, final Long documentId) {
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
            throw new StepRunException(message, e);
        }
    }

    public Data loadData(final Long id) {
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
            throw new StepRunException(message, e);
        }
    }

    public void storeData(final Data data) {
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
