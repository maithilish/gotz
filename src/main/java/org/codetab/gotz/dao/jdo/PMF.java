package org.codetab.gotz.dao.jdo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
@Singleton
public class PMF {

    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    private PersistenceManagerFactory factory;
    private ConfigService configService;

    @Inject
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Inject
    private PMF() {
    }

    @GuardedBy("this")
    public void init() {
        if (factory == null) {
            synchronized (this) {
                logger.info("initialize JDO PMF");
                try {
                    String configFile = configService
                            .getConfig("gotz.datastore.configFile");
                    Properties properties = getConfigProperties(configFile);

                    factory = createFactory(properties);
                    logger.info("JDO PersistenceManagerFactory created");

                    logger.debug("PMF Properties {}", pmfPropertiesString(properties));
                    logger.debug("initialized JDO PMF");
                } catch (Exception e) {
                    logger.error("{} Exit", e.getMessage());
                    logger.trace("{}", e);
                    throw new CriticalException("JDO Persistence Manager setup error",e);
                }
            }
        }
    }

    private PersistenceManagerFactory createFactory(Properties properties) {
        return JDOHelper.getPersistenceManagerFactory(properties);
    }

    private Properties getConfigProperties(String propertyFile) throws IOException {
        Properties jdoProperties = new Properties();
        InputStream inputStream = PMF.class.getClassLoader()
                .getResourceAsStream(propertyFile);
        if (inputStream == null) {
            String message = MessageFormat.format(
                    "JDO Config file {0} not found in classpath. Exit", propertyFile);
            throw new FileNotFoundException(message);
        }
        jdoProperties.load(inputStream);
        inputStream.close();
        return jdoProperties;
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
