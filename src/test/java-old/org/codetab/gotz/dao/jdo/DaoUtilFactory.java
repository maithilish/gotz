package org.codetab.gotz.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.IDaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DaoUtilFactory extends org.codetab.gotz.dao.DaoUtilFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DaoUtilFactory.class);

    private PersistenceManagerFactory pmf;

    public DaoUtilFactory() {
        pmf = PMF.INSTANCE.getFactory();
    }

    public PersistenceManagerFactory getFactory() {
        return pmf;
    }

    @Override
    public IDaoUtil getUtilDao() {
        return new DaoUtil(pmf);
    }

}
