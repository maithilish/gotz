package in.m.picks.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DaoFactory {

	final static Logger logger = LoggerFactory.getLogger(DaoFactory.class);

	public enum ORM {
		JDO
	}

	private static DaoFactory INSTANCE;

	public abstract ILocatorDao getLocatorDao();

	public abstract IDocumentDao getDocumentDao();

	public abstract IDataDefDao getDataDefDao();

	public abstract IDataDao getDataDao();

	public static DaoFactory getDaoFactory(ORM orm) {
		if (INSTANCE == null) {
			switch (orm) {
			case JDO:
				INSTANCE = new in.m.picks.dao.jdo.DaoFactory();
				break;
			}
		}
		return INSTANCE;
	}

	public static ORM getOrmType(String ormName) {
		if (ormName == null) {
			ormName = "JDO";
		}
		ORM orm = null;
		if (ormName.toUpperCase().equals("JDO")) {
			orm = ORM.JDO;
		}
		return orm;
	}

}
