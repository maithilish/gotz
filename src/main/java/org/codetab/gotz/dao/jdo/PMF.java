package org.codetab.gotz.dao.jdo;

import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.ResourceStream;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
@Singleton
public final class PMF {

    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    private PersistenceManagerFactory factory;

    @Inject
    private ConfigService configService;
    @Inject
    private ResourceStream resourceStream;
    @Inject
    private Properties jdoProperties;

    @Inject
    private PMF() {
    }

    @GuardedBy("this")
    public void init() {
        if (factory == null) {
            synchronized (this) {
                logger.info("initialize JDO PMF");
                String configFile;
                try {
                    configFile = configService
                            .getConfig("gotz.datastore.configFile");
                } catch (ConfigNotFoundException e) {
                    throw new CriticalException(
                            "JDO Persistence Manager setup error", e);
                }
                try (InputStream propStream =
                        resourceStream.getInputStream(configFile)) {
                    jdoProperties.load(propStream);
                    factory = JDOHelper
                            .getPersistenceManagerFactory(jdoProperties);

                    logger.info("JDO PersistenceManagerFactory created");
                    logger.debug("PMF Properties {}",
                            pmfPropertiesString(jdoProperties));
                    logger.debug("initialized JDO PMF");
                } catch (Exception e) {
                    logger.error("{}", e.getMessage());
                    logger.trace("{}", e);
                    throw new CriticalException(
                            "JDO Persistence Manager setup error", e);
                }
            }
        }
    }

    private String pmfPropertiesString(final Properties properties) {
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        for (Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(Util.logIndent());
            sb.append(entry);
            sb.append(line);
        }
        return sb.toString();
    }

    public PersistenceManagerFactory getFactory() {
        return factory;
    }

}
