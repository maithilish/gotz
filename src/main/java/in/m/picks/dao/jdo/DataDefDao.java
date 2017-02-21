package in.m.picks.dao.jdo;

import java.util.Date;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.IDataDefDao;
import in.m.picks.model.DataDef;

public class DataDefDao implements IDataDefDao {

	final static Logger logger = LoggerFactory.getLogger(DataDefDao.class);

	private PersistenceManagerFactory pmf;

	public DataDefDao(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
		if (pmf == null)
			logger.error(
					"loading JDO Dao failed as PersistenceManagerFactory is null");
	}

	@Override
	public void storeDataDef(DataDef dataDef) {
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
			@SuppressWarnings("unchecked")
			List<DataDef> dataDefs = (List<DataDef>) query
					.execute(dataDef.getName());
			if (dataDefs.size() > 0) {
				DataDef lastDataDef = dataDefs.get(dataDefs.size() - 1);
				if (!dataDef.equals(lastDataDef)) {
					Date toDate = DateUtils.addMilliseconds(dataDef.getFromDate(),
							-1);
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
	public List<DataDef> getDataDefs(Date date) {
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

	private PersistenceManager getPM() {
		PersistenceManager pm = pmf.getPersistenceManager();
		logger.trace("returning PM : {}", pm);
		return pm;
	}
}
