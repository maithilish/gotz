package org.codetab.gotz.dao.jdo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.MonitorService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PMF {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    private PersistenceManagerFactory factory;

    PMF() {
        logger.info("initialize JDO PMF");
        try {
            String configFile = ConfigService.INSTANCE
                    .getConfig("gotz.datastore.configFile");
            Properties properties = getConfigProperties(configFile);

            factory = createFactory(properties);
            logger.info("JDO PersistenceManagerFactory created");

            logger.debug("PMF Properties {}", pmfPropertiesString(properties));
            logger.debug("initialized JDO PMF");
        } catch (Exception e) {
            logger.error("{} Exit", e.getMessage());
            logger.trace("{}", e);
            MonitorService.instance().triggerFatal("Database failure");
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
