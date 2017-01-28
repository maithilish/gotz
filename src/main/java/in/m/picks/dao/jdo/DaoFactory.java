package in.m.picks.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.IDataDao;
import in.m.picks.dao.IDataDefDao;
import in.m.picks.dao.IDocumentDao;
import in.m.picks.dao.ILocatorDao;

public class DaoFactory extends in.m.picks.dao.DaoFactory {

	final static Logger logger = LoggerFactory.getLogger(DaoFactory.class);

	private PersistenceManagerFactory pmf;

	public DaoFactory() {
		pmf = PMF.INSTANCE.getFactory();
	}

	public PersistenceManagerFactory getFactory() {
		return pmf;
	}

	@Override
	public IDocumentDao getDocumentDao() {
		return new DocumentDao(pmf);
	}

	@Override
	public IDataDefDao getDataDefDao() {
		return new DataDefDao(pmf);
	}

	@Override
	public IDataDao getDataDao() {
		return new DataDao(pmf);
	}

	@Override
	public ILocatorDao getLocatorDao() {
		return new LocatorDao(pmf);
	}

}
