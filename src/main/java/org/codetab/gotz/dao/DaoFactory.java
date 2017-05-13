package org.codetab.gotz.dao;

import javax.inject.Inject;

import org.codetab.gotz.di.DInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoFactory.class);

    private DaoFactory instance;

    @Inject
    private DInjector dInjector;

    @Inject
    public DaoFactory() {
    }

    public DaoFactory getDaoFactory(final ORM orm) {
        if (instance == null) {
            switch (orm) {
            case JDO:
                instance = dInjector.instance(org.codetab.gotz.dao.jdo.JdoDaoFactory.class);
                break;
            default:
                instance = dInjector.instance(org.codetab.gotz.dao.jdo.JdoDaoFactory.class);
                break;
            }
        }
        return instance;
    }

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
}
