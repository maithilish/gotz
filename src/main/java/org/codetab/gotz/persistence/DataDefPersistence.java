package org.codetab.gotz.persistence;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.codetab.gotz.dao.DaoFactory;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataDefPersistence {

    static private final Logger LOGGER =
            LoggerFactory.getLogger(DataDefPersistence.class);

    @Inject
    private ConfigService configService;
    @Inject
    private DaoFactory daoFactory;

    public List<DataDef> loadDataDefs() {
        try {
            ORM orm = configService.getOrmType();
            IDataDefDao dao = daoFactory.getDaoFactory(orm).getDataDefDao();
            List<DataDef> dataDefs =
                    dao.getDataDefs(configService.getRunDateTime());
            LOGGER.debug("DataDef loaded : [{}]", dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new CriticalException("datadef creation error", e);
        }
    }

    public void storeDataDef(final DataDef dataDef) {
        try {
            ORM orm = configService.getOrmType();
            IDataDefDao dao = daoFactory.getDaoFactory(orm).getDataDefDao();
            String name = dataDef.getName();
            LOGGER.debug("store DataDef");
            dao.storeDataDef(dataDef);
            if (dataDef.getId() != null) {
                LOGGER.debug("stored DataDef [{}] [{}]", dataDef.getId(), name);
            }
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            throw new CriticalException("datadef creation error", e);
        }
    }

    public boolean markForUpdation(List<DataDef> dataDefs,
            List<DataDef> newDataDefs) {
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

    private void resetHighDate(final DataDef dataDef) {
        dataDef.setToDate(configService.getRunDateTime());
    }

    private DataDef getDataDef(List<DataDef> dataDefs, String name) {
        return dataDefs.stream().filter(e -> e.getName().equals(name))
                .findFirst().get();
    }
}
