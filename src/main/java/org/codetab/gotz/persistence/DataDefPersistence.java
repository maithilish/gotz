package org.codetab.gotz.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DataDef persistence and related helper methods.
 * @author Maithilish
 *
 */
public class DataDefPersistence {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataDefPersistence.class);

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
     * Loads all active DataDef as on run datetime from store.
     * @return list of active datadef
     * @throws CriticalException
     *             if any persistence error
     */
    public List<DataDef> loadDataDefs() {

        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return new ArrayList<>();
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            List<DataDef> dataDefs =
                    dao.getDataDefs(configService.getRunDateTime());
            LOGGER.debug(Messages.getString("DataDefPersistence.1"), //$NON-NLS-1$
                    dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            throw new CriticalException(
                    Messages.getString("DataDefPersistence.2"), e); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * Store DataDef.
     * @param dataDef
     *            not null
     * @throws CriticalException
     *             if any persistence error
     */
    public void storeDataDef(final DataDef dataDef) {
        Validate.notNull(dataDef, Messages.getString("DataDefPersistence.3")); //$NON-NLS-1$

        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return;
        }

        boolean persist = configService.isPersist("gotz.persist.dataDef"); //$NON-NLS-1$
        try {
            // xpath - not abs path
            persist = fieldsHelper.isTrue("//xf:persist", dataDef.getFields()); //$NON-NLS-1$
        } catch (FieldsNotFoundException e) {
        }

        if (!persist) {
            return;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            String name = dataDef.getName();
            LOGGER.debug(Messages.getString("DataDefPersistence.7")); //$NON-NLS-1$
            dao.storeDataDef(dataDef);
            if (dataDef.getId() != null) {
                LOGGER.debug(Messages.getString("DataDefPersistence.8"), //$NON-NLS-1$
                        dataDef.getId(), name);
            }
        } catch (RuntimeException e) {
            throw new CriticalException(
                    Messages.getString("DataDefPersistence.9"), e); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * Compares active and new list of datadef and if any active datadef has a
     * new datadef version, then the active datadef highDate is reset to
     * runDateTime and new datadef is added to active list. For any new datadef,
     * there is no active datadef, then the new datadef is added active list.
     * Later updated active list is persisted to store.
     * @param dataDefs
     *            active list - existing active datadefs, not null
     * @param newDataDefs
     *            list of new datadefs, not null
     * @return true if active list is modified
     */
    public boolean markForUpdation(final List<DataDef> dataDefs,
            final List<DataDef> newDataDefs) {
        Validate.notNull(dataDefs, Messages.getString("DataDefPersistence.10")); //$NON-NLS-1$
        Validate.notNull(newDataDefs,
                Messages.getString("DataDefPersistence.11")); //$NON-NLS-1$

        boolean updates = false;
        for (DataDef newDataDef : newDataDefs) {
            String name = newDataDef.getName();
            String message = null;
            try {
                DataDef dataDef = getDataDef(dataDefs, name);
                if (dataDef.equals(newDataDef)) {
                    message = Messages.getString("DataDefPersistence.12"); //$NON-NLS-1$
                } else {
                    message = Messages.getString("DataDefPersistence.13"); //$NON-NLS-1$
                    updates = true;
                    resetHighDate(dataDef);
                    dataDefs.add(newDataDef);
                }
            } catch (NoSuchElementException e) {
                message = Messages.getString("DataDefPersistence.14"); //$NON-NLS-1$
                updates = true;
                dataDefs.add(newDataDef);
            }
            LOGGER.info(Messages.getString("DataDefPersistence.15"), name, //$NON-NLS-1$
                    message);
        }
        return updates;
    }

    /**
     * <p>
     * Reset datadef highDate to runDateTime.
     * @param dataDef
     *            datadef to modify
     */
    private void resetHighDate(final DataDef dataDef) {
        dataDef.setToDate(configService.getRunDateTime());
    }

    /**
     * <p>
     * Filter list of datadef by name.
     * @param dataDefs
     *            list datadef
     * @param name
     *            datadef name
     * @return matching datadef
     */
    private DataDef getDataDef(final List<DataDef> dataDefs,
            final String name) {
        return dataDefs.stream().filter(e -> e.getName().equals(name))
                .findFirst().get();
    }
}
