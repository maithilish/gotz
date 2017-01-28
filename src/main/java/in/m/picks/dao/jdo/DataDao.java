package in.m.picks.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.IDataDao;
import in.m.picks.model.Data;

public class DataDao implements IDataDao {

	final static Logger logger = LoggerFactory.getLogger(DataDao.class);

	private PersistenceManagerFactory pmf;

	public DataDao(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
		if (pmf == null)
			logger.error(
					"loading JDO Dao failed as PersistenceManagerFactory is null");
	}

	@Override
	public void storeData(Data data) {
		PersistenceManager pm = getPM();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			pm.makePersistent(data);
			tx.commit();
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}

	@Override
	public Data getData(Long documentId, Long dataDefId) {
		PersistenceManager pm = getPM();
		try {
			String filter = "documentId == rId && dataDefId == dId";
			String paramDecla = "Long rId,Long dId";
			Extent<Data> extent = pm.getExtent(Data.class);
			Query<Data> query = pm.newQuery(extent, filter);
			query.declareParameters(paramDecla);

			@SuppressWarnings("unchecked")
			List<Data> data = (List<Data>) query.execute(documentId, dataDefId);
			pm.getFetchPlan().addGroup("detachMembers");
			data = (List<Data>) pm.detachCopyAll(data);
			switch (data.size()) {
			case 0:
				return null;
			case 1:
				return data.get(0);
			default:
				// TODO log error
				break;
			}
		} finally {
			pm.close();
		}
		return null;
	}

	@Override
	public Data getData(Long id) {
		PersistenceManager pm = getPM();
		try {
			Object result = pm.getObjectById(Data.class, id);
			pm.getFetchPlan().addGroup("detachMembers");
			return (Data) pm.detachCopy(result);
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
