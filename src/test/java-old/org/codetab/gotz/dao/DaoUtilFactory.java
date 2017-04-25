package org.codetab.gotz.dao;

import org.codetab.gotz.dao.DaoFactory.ORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DaoUtilFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoUtilFactory.class);

    private static DaoUtilFactory instance;

    public abstract IDaoUtil getUtilDao();

    public static DaoUtilFactory getDaoFactory(final ORM orm) {
        if (instance == null) {
            switch (orm) {
            case JDO:
                instance = new org.codetab.gotz.dao.jdo.DaoUtilFactory();
                break;
            default:
                instance = new org.codetab.gotz.dao.jdo.DaoUtilFactory();
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
