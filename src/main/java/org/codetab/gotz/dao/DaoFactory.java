package org.codetab.gotz.dao;

import javax.inject.Inject;

import org.codetab.gotz.di.DInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoFactory.class);

    public enum ORM {
        JDO
    }

    private DaoFactory instance;
    private DInjector dInjector;

    public ILocatorDao getLocatorDao() {
        throw new UnsupportedOperationException("subclass should override this method");
    }

    public IDocumentDao getDocumentDao() {
        throw new UnsupportedOperationException("subclass should override this method");
    }

    public IDataDefDao getDataDefDao() {
        throw new UnsupportedOperationException("subclass should override this method");
    }

    public IDataDao getDataDao() {
        throw new UnsupportedOperationException("subclass should override this method");
    }

    @Inject
    public DaoFactory() {
    }

    @Inject
    public void setdInjector(DInjector dInjector) {
        this.dInjector = dInjector;
    }

    public DaoFactory getDaoFactory(final ORM orm) {
        if (instance == null) {
            switch (orm) {
            case JDO:
                instance = dInjector.instance(org.codetab.gotz.dao.jdo.DaoFactory.class);
                break;
            default:
                instance = dInjector.instance(org.codetab.gotz.dao.jdo.DaoFactory.class);
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
