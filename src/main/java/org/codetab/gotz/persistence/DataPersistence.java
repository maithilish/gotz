package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;

/**
 * <p>
 * Data Persistence methods.
 * @author Maithilish
 *
 */
public class DataPersistence {

    /**
     * ConfigService.
     */
    @Inject
    private ConfigService configService;

    @Inject
    private FieldsHelper fieldsHelper;
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

        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            String message = Util.join(Messages.getString("DataPersistence.1"), //$NON-NLS-1$
                    "dataDefId=", //$NON-NLS-1$
                    String.valueOf(dataDefId), ",documentId=", //$NON-NLS-1$
                    String.valueOf(documentId));
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

        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(id);
            return data;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("DataPersistence.5"), "id=", //$NON-NLS-1$ //$NON-NLS-2$
                            String.valueOf(id));
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Store data.
     * @param data
     *            data to store, not null
     * @param fields
     * @throws StepPersistenceException
     *             on persistence error
     */
    public boolean storeData(final Data data, final Fields fields) {
        Validate.notNull(data, Messages.getString("DataPersistence.7")); //$NON-NLS-1$
        Validate.notNull(fields, Messages.getString("DataPersistence.8")); //$NON-NLS-1$

        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return false;
        }
        boolean persist = configService.isPersist("gotz.persist.data"); //$NON-NLS-1$
        try {
            persist = fieldsHelper
                    .isTrue("/xf:fields/xf:task/xf:persist/xf:data", fields); //$NON-NLS-1$
        } catch (FieldsNotFoundException e) {
        }

        if (!persist) {
            return false;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            dao.storeData(data);
            return true;
        } catch (RuntimeException e) {
            String message = Util.join(Messages.getString("DataPersistence.12"), //$NON-NLS-1$
                    data.getName(), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }
}
