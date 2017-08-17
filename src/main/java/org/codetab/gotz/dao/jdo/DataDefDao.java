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

/**
 * <p>
 * JDO DataDef DAO implementation.
 * @author Maithilish
 *
 */
public final class DataDefDao implements IDataDefDao {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataDefDao.class);

    /**
     * JDO PMF.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     * @param pmf
     *            JDO PMF
     */
    public DataDefDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Stores new version datadef. It also updates the toDate field of previous
     * version to runDateTime minus 1 second. It will not check whether same
     * version exists in store as an equal but inactive version can exists.
     * @param datadef
     */
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

            @SuppressWarnings("unchecked")
            List<DataDef> dataDefs =
                    (List<DataDef>) query.execute(dataDef.getName());

            /*
             * don't check whether similar item exists in database as some
             * previous version can be same as present one but it is no longer
             * active
             */

            // if previous version exists, update its toDate
            if (dataDefs.size() > 0) {
                DataDef lastDataDef = dataDefs.get(dataDefs.size() - 1);
                Date toDate = DateUtils.addSeconds(dataDef.getFromDate(), -1);
                if (toDate.before(lastDataDef.getFromDate())) {
                    toDate = new Date(lastDataDef.getFromDate().getTime());
                }
                lastDataDef.setToDate(toDate);
            }

            // insert new version
            pm.makePersistent(dataDef);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    /**
     * <p>
     * Get list of active datadef (detached copy) for a date.
     * @param date
     * @return list of datadef
     */
    @Override
    public List<DataDef> getDataDefs(final Date date) {
        Validate.notNull(date, "date must not be null");

        List<DataDef> dataDefs = null;
        PersistenceManager pm = getPM();
        try {
            String filter = "fromDate <= pdate && toDate >= pdate";
            String paramDecla = "java.util.Date pdate";
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> result = (List<DataDef>) query.execute(date);
            pm.getFetchPlan().addGroup("detachFields");
            pm.getFetchPlan().addGroup("detachAxis");

            dataDefs = (List<DataDef>) pm.detachCopyAll(result);
        } finally {
            pm.close();
        }
        return dataDefs;
    }

    /**
     * <p>
     * Get list of active datadef (detached copy) of a name.
     * @param name
     *            datadef name
     * @return list of datadef
     */
    @Override
    public List<DataDef> getDataDefs(final String name) {
        Validate.notNull(name, "name must not be null");

        List<DataDef> dataDefs = null;
        PersistenceManager pm = getPM();
        try {
            String filter = "name == pname";
            String paramDecla = "String pname";
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> result = (List<DataDef>) query.execute(name);
            pm.getFetchPlan().addGroup("detachFields");
            pm.getFetchPlan().addGroup("detachAxis");

            dataDefs = (List<DataDef>) pm.detachCopyAll(result);
        } finally {
            pm.close();
        }
        return dataDefs;
    }

    /**
     * <p>
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        LOGGER.trace("returning PM : {}", pm);
        return pm;
    }
}
