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

/**
 * <p>
 * JDO LocatorDao.
 * <p>
 * Locator can hold list of Document. The {@see Document} contains metadata
 * fields like fromDate,toDate and also a field named documentObject to hold
 * actual contents. To conserve memory and for performance, LocatorDao fetches
 * only metadata and it will not fetch contents. As such, documentObject will be
 * null.
 * <p>
 * To fetch document with contents, use {@see DocumentDao}.
 * @author Maithilish
 *
 */
public final class LocatorDao implements ILocatorDao {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorDao.class);

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
    public LocatorDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Get locator (detached copy) for a name and group. It fetches list of
     * document of the locator, but without documentObject (i.e. actual
     * contents).
     * <p>
     * The {@see Document} has metadata fields like fromDate,toDate and also a
     * field documentObject which holds actual contents. As locator may have
     * multiple documents,for performance, LocatorDao fetches list of documents
     * with only metadata. To fetch document with contents, use DocumentDao.
     * @param id
     * @return document
     */
    @Override
    public Locator getLocator(final String name, final String group) {
        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

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
                        "found multiple locators for [name][group] [" + name
                                + "][" + group + "]");
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Store locator and its documents.
     * @param locator
     */
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

    /**
     * <p>
     * Get detached copy of locator by id. It fetches list of document of the
     * locator, but without documentObject (i.e. actual contents).
     * @param id
     *            locator id
     * @return locator if no matching locator then return null
     */
    @Override
    public Locator getLocator(final long id) {
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

    /**
     * <p>
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        return pm;
    }

}
