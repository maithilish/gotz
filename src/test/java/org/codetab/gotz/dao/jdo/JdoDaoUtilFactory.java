package org.codetab.gotz.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdoDaoUtilFactory extends DaoUtilFactory {

    static final Logger LOGGER =
            LoggerFactory.getLogger(JdoDaoUtilFactory.class);

    private PersistenceManagerFactory pmf;

    public JdoDaoUtilFactory() {
        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        DInjector dInjector = new DInjector();
        ConfigService configService = dInjector.instance(ConfigService.class);
        configService.init(userProvidedFile, defaultsFile);
        PMF jdoPMF = dInjector.instance(PMF.class);
        jdoPMF.init();
        pmf = jdoPMF.getFactory();
    }

    public PersistenceManagerFactory getFactory() {
        return pmf;
    }

    @Override
    public IDaoUtil getUtilDao() {
        return new DaoUtil(pmf);
    }

}
