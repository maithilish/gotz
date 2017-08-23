package org.codetab.gotz.dao.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * Singleton which provides JDO PersistenceManagerFactory to DAO layer.
 * Initializes JDO PersistenceManagerFactory from properties file specified by
 * config property gotz.datastore.configFile.
 * @author Maithilish
 *
 */
@ThreadSafe
@Singleton
public class PMF {

    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    /**
     * ConfigService.
     */
    @Inject
    private ConfigService configService;

    /**
     * ResourceStream.
     */
    @Inject
    private IOHelper ioHelper;
    /**
     * JDO properties.
     */
    @Inject
    private Properties jdoProperties;

    /**
     * JDO PersistenceManagerFactory.
     */
    @GuardedBy("this")
    private PersistenceManagerFactory factory;

    /**
     * <p>
     * Private Constructor - Singleton.
     */
    @Inject
    private PMF() {
    }

    /**
     * <p>
     * Get JDO PersistenceManagerFactory.
     * @return persistence manager factory
     */
    public PersistenceManagerFactory getFactory() {
        return factory;
    }

    /**
     * <p>
     * Initializes JDO PersistenceManagerFactory from properties file specified
     * by config property gotz.datastore.configFile.
     * @throws CriticalException
     *             if gotz.datastore.configFile config not found or if unable to
     *             read properties file or unable to create persistence manager
     *             factory
     */
    public synchronized void init() {
        if (factory == null) {
            logger.info("initialize JDO PMF");
            String configFile;
            try {
                configFile =
                        configService.getConfig("gotz.datastore.configFile");
            } catch (ConfigNotFoundException e) {
                throw new CriticalException(
                        "JDO Persistence Manager setup error", e);
            }
            try (InputStream propStream = ioHelper.getInputStream(configFile)) {
                jdoProperties.load(propStream);
                factory = JDOHelper.getPersistenceManagerFactory(jdoProperties);

                logger.info("JDO PersistenceManagerFactory created");
                logger.debug("PMF Properties {}",
                        Util.getPropertiesAsString(jdoProperties));
                logger.debug("initialized JDO PMF");
            } catch (IOException e) {
                logger.error("{}", e.getMessage());
                logger.trace("{}", e);
                throw new CriticalException(
                        "JDO Persistence Manager setup error", e);
            }
        }
    }

}
