package org.codetab.gotz.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DaoFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoFactory.class);

    public enum ORM {
        JDO
    }

    private static DaoFactory instance;

    public abstract ILocatorDao getLocatorDao();

    public abstract IDocumentDao getDocumentDao();

    public abstract IDataDefDao getDataDefDao();

    public abstract IDataDao getDataDao();

    public static DaoFactory getDaoFactory(final ORM orm) {
        if (instance == null) {
            switch (orm) {
            case JDO:
                instance = new org.codetab.gotz.dao.jdo.DaoFactory();
                break;
            default:
                instance = new org.codetab.gotz.dao.jdo.DaoFactory();
                break;
            }
        }
        return instance;
    }

    public static ORM getOrmType(final String ormName) {
        ORM orm = null;
        if (ormName == null) {
            orm = ORM.JDO;
        }
        if (ormName.toUpperCase().equals("JDO")) {
            orm = ORM.JDO;
        }
        return orm;
    }

}
