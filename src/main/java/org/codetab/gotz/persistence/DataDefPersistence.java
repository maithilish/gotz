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

        if (!configService.isPersist("gotz.useDataStore")) {
            return new ArrayList<>();
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            List<DataDef> dataDefs =
                    dao.getDataDefs(configService.getRunDateTime());
            LOGGER.debug("DataDef loaded : [{}]", dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            throw new CriticalException("datadef creation error", e);
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
        Validate.notNull(dataDef, "dataDef must not be null");

        if (!configService.isPersist("gotz.useDataStore")) {
            return;
        }

        boolean persist = configService.isPersist("gotz.persist.dataDef");
        try {
            // xpath - not abs path
            persist = fieldsHelper.isTrue("//xf:persist", dataDef.getFields());
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
            LOGGER.debug("store DataDef");
            dao.storeDataDef(dataDef);
            if (dataDef.getId() != null) {
                LOGGER.debug("stored DataDef [{}] [{}]", dataDef.getId(), name);
            }
        } catch (RuntimeException e) {
            throw new CriticalException("datadef creation error", e);
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
        Validate.notNull(dataDefs, "dataDefs must not be null");
        Validate.notNull(newDataDefs, "newDataDefs must not be null");

        boolean updates = false;
        for (DataDef newDataDef : newDataDefs) {
            String name = newDataDef.getName();
            String message = null;
            try {
                DataDef dataDef = getDataDef(dataDefs, name);
                if (dataDef.equals(newDataDef)) {
                    message = "no changes";
                } else {
                    message = "changed, insert new version";
                    updates = true;
                    resetHighDate(dataDef);
                    dataDefs.add(newDataDef);
                }
            } catch (NoSuchElementException e) {
                message = "not in store, insert";
                updates = true;
                dataDefs.add(newDataDef);
            }
            LOGGER.info("DataDef [{}] {}", name, message);
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
