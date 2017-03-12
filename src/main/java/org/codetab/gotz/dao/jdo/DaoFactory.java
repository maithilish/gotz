package org.codetab.gotz.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DaoFactory extends org.codetab.gotz.dao.DaoFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoFactory.class);

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
