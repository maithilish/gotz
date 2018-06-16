package org.codetab.gotz.itest.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.dao.jdo.PMF;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.itest.DaoUtil;
import org.codetab.gotz.itest.DaoUtilFactory;
import org.codetab.gotz.itest.IDaoUtil;
import org.codetab.gotz.shared.ConfigService;

/**
 * <p>
 * JDO DaoUtilFactory for tests.
 * @author Maithilish
 *
 */
public final class JdoDaoUtilFactory extends DaoUtilFactory {

    /**
     * pmf.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     *
     */
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

    /**
     * <p>
     * Get PMF.
     * @return PMF
     */
    public PersistenceManagerFactory getFactory() {
        return pmf;
    }

    @Override
    public IDaoUtil getUtilDao() {
        return new DaoUtil(pmf);
    }

}
