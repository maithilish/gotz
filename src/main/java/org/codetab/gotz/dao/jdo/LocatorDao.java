package org.codetab.gotz.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.model.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocatorDao implements ILocatorDao {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorDao.class);

    private PersistenceManagerFactory pmf;

    public LocatorDao(final PersistenceManagerFactory pmf) {
        this.pmf = pmf;
        if (pmf == null) {
            LOGGER.error("loading JDO Dao failed as PersistenceManagerFactory is null");
        }
    }

    @Override
    public Locator getLocator(final String name, final String group) {
        PersistenceManager pm = getPM();
        try {
            String filter = "name == pname && group == pgroup";
            String paramDecalre = "String pname, String pgroup";
            Extent<Locator> extent = pm.getExtent(Locator.class);
            Query<Locator> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecalre);
            @SuppressWarnings("unchecked")
            List<Locator> locators = (List<Locator>) query.execute(name, group);
            for (Locator locator : locators) {
                if (locator.getName().equals(name) && locator.getGroup().equals(group)) {
                    // document without documentObject !!!
                    // to fetch documentObject use DocumentDao
                    pm.getFetchPlan().addGroup("detachDocuments");
                    return pm.detachCopy(locator);
                }
            }
        } finally {
            pm.close();
        }
        return null;
    }

    @Override
    public void storeLocator(final Locator locator) {
        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(locator);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Override
    public Locator getLocator(final Long id) {
        PersistenceManager pm = getPM();
        try {
            Locator locator = pm.getObjectById(Locator.class, id);
            // document without documentObject !!!
            // to fetch documentObject use DocumentDao
            pm.getFetchPlan().addGroup("detachDocuments");
            return pm.detachCopy(locator);
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
