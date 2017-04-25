package org.codetab.gotz.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.model.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocatorDao implements ILocatorDao {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorDao.class);

    private PersistenceManagerFactory pmf;

    public LocatorDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
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
            // fetch document without documentObject !!!
            // to fetch documentObject use DocumentDao
            pm.getFetchPlan().addGroup("detachDocuments");
            locators = (List<Locator>) pm.detachCopyAll(locators);
            switch (locators.size()) {
            case 0:
                return null;
            case 1:
                return locators.get(0);
            default:
                throw new IllegalStateException(
                        "found multiple locators for [name][group] [" + name + "]["
                                + group + "]");
            }
        } finally {
            pm.close();
        }
    }

    @Override
    public void storeLocator(final Locator locator) {
        Validate.notNull(locator, "locator must not be null");
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
