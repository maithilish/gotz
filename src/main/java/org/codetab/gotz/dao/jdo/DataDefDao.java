package org.codetab.gotz.dao.jdo;

import java.util.Date;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.model.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataDefDao implements IDataDefDao {

    static final Logger LOGGER = LoggerFactory.getLogger(DataDefDao.class);

    private PersistenceManagerFactory pmf;

    public DataDefDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    @Override
    public void storeDataDef(final DataDef dataDef) {
        Validate.notNull(dataDef, "dataDef must not be null");
        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            String filter = "name == pname";
            String paramDecla = "String pname";
            String ordering = "id ascending";
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);
            query.setOrdering(ordering);

            pm.getFetchPlan().addGroup("detachFields");
            pm.getFetchPlan().addGroup("detachAxis");
            pm.getFetchPlan().addGroup("detachMembers");
            pm.getFetchPlan().addGroup("detachFilters");

            @SuppressWarnings("unchecked")
            List<DataDef> dataDefs = (List<DataDef>) query.execute(dataDef.getName());
            if (dataDefs.size() > 0) {
                DataDef lastDataDef = dataDefs.get(dataDefs.size() - 1);
                if (!dataDef.equals(lastDataDef)) {
                    Date toDate = DateUtils.addMilliseconds(dataDef.getFromDate(), -1);
                    lastDataDef.setToDate(toDate);
                    pm.makePersistent(dataDef);
                }
            } else {
                pm.makePersistent(dataDef);
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Override
    public List<DataDef> getDataDefs(final Date date) {
        PersistenceManager pm = getPM();
        try {
            String filter = "fromDate <= pdate && toDate >= pdate";
            String paramDecla = "java.util.Date pdate";
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> dataDefs = (List<DataDef>) query.execute(date);
            pm.getFetchPlan().addGroup("detachFields");
            pm.getFetchPlan().addGroup("detachAxis");
            pm.getFetchPlan().addGroup("detachMembers");
            pm.getFetchPlan().addGroup("detachFilters");
            return (List<DataDef>) pm.detachCopyAll(dataDefs);
        } finally {
            pm.close();
        }
    }

    @Override
    public List<DataDef> getDataDefs(final String name) {
        PersistenceManager pm = getPM();
        try {
            String filter = "name == pname";
            String paramDecla = "String pname";
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> dataDefs = (List<DataDef>) query.execute(name);
            pm.getFetchPlan().addGroup("detachFields");
            pm.getFetchPlan().addGroup("detachAxis");
            pm.getFetchPlan().addGroup("detachMembers");
            pm.getFetchPlan().addGroup("detachFilters");
            return (List<DataDef>) pm.detachCopyAll(dataDefs);
        } finally {
            pm.close();
        }
    }

    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        LOGGER.trace("returning PM : {}", pm);
        return pm;
    }
}
