package in.m.picks.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.ILocatorDao;
import in.m.picks.model.Locator;

public class LocatorDao implements ILocatorDao {

	final static Logger logger = LoggerFactory.getLogger(LocatorDao.class);

	private PersistenceManagerFactory pmf;

	public LocatorDao(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
		if (pmf == null)
			logger.error(
					"loading JDO Dao failed as PersistenceManagerFactory is null");
	}

	@Override
	public Locator getLocator(String name, String group) {
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
				if (locator.getName().equals(name)
						&& locator.getGroup().equals(group)) {
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
	public void storeLocator(Locator locator) {
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
	public Locator getLocator(Long id) {
		PersistenceManager pm = getPM();
		try {
			Locator locator = (Locator) pm.getObjectById(Locator.class, id);
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
		logger.trace("returning PM : {}", pm);
		return pm;
	}

}
