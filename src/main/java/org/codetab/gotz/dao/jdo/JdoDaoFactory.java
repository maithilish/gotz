package org.codetab.gotz.dao.jdo;

import javax.inject.Inject;

import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdoDaoFactory extends org.codetab.gotz.dao.DaoFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(JdoDaoFactory.class);

    private PMF pmf;

    @Inject
    public JdoDaoFactory() {
    }

    @Inject
    public void setPmf(final PMF pmf) {
        if (pmf.getFactory() == null) {
            pmf.init();
        }
        this.pmf = pmf;
    }

    @Override
    public IDocumentDao getDocumentDao() {
        return new DocumentDao(pmf.getFactory());
    }

    @Override
    public IDataDefDao getDataDefDao() {
        return new DataDefDao(pmf.getFactory());
    }

    @Override
    public IDataDao getDataDao() {
        return new DataDao(pmf.getFactory());
    }

    @Override
    public ILocatorDao getLocatorDao() {
        return new LocatorDao(pmf.getFactory());
    }

}
