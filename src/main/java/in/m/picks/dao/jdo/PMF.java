package in.m.picks.dao.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.shared.MonitorService;
import in.m.picks.util.Util;

public enum PMF {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    private PersistenceManagerFactory factory;

    PMF() {
        logger.info("initialize JDO PMF");
        createFactory();
        logger.debug("initialized JDO PMF");
    }

    private void createFactory() {

        try {
            Properties jdoProperties = getJdoConfig();
            factory = JDOHelper.getPersistenceManagerFactory(jdoProperties);
            logger.info("JDO PersistenceManagerFactory created");
            debugPMFProperties(factory.getProperties());
        } catch (JDOFatalUserException e) {
            logger.error("{} Exit", e.getMessage());
            logger.trace("{}", e);
            MonitorService.INSTANCE.triggerFatal("Database failure");
        } catch (JDOFatalDataStoreException e) {
            logger.error("{} Exit", e.getMessage());
            logger.trace("{}", e);
            MonitorService.INSTANCE.triggerFatal("Database failure");
        } catch (Exception e) {
            logger.error("{} Exit", e.getMessage());
            logger.trace("{}", e);
            MonitorService.INSTANCE.triggerFatal("Database failure");
        }
    }

    private Properties getJdoConfig() throws Exception {

        Properties jdoProperties = new Properties();
        InputStream inputStream = null;
        String propFileName = "jdoconfig.properties";

        inputStream = PMF.class.getClassLoader()
                .getResourceAsStream(propFileName);
        if (inputStream != null) {
            try {
                jdoProperties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                throw e;
            }
        } else {
            throw new Exception("JDO Config file {}" + propFileName
                    + "not found in classpath. Exit");
        }
        return jdoProperties;
    }

    private void debugPMFProperties(final Properties properties) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        for (Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(Util.logIndent());
            sb.append(entry);
            sb.append(line);
        }
        logger.debug("PMF Properties {}", sb);
    }

    public PersistenceManagerFactory getFactory() {
        return factory;
    }

    public final void setFactory(final PersistenceManagerFactory pmf) {
        this.factory = pmf;
    }

}
