package org.codetab.gotz.shared;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfigService {

    enum ConfigIndex {
        SYSTEM, PROVIDED, DEFAULTS
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigService.class);

    private CompositeConfiguration configs;

    @Inject
    private ConfigService() {
    }

    public void init(final String userProvidedFile, final String defaultsFile) {
        LOGGER.info("Initializing Configs");

        configs = new CompositeConfiguration();

        SystemConfiguration systemConfigs = new SystemConfiguration();
        configs.addConfiguration(systemConfigs);

        try {
            Configuration userProvided = getPropertiesConfigs(userProvidedFile);
            configs.addConfiguration(userProvided);
        } catch (ConfigurationException e) {
            configs.addConfiguration(new PropertiesConfiguration());
            LOGGER.info(e.getLocalizedMessage() + ". "
                    + "Using default properties");
        }

        try {
            Configuration defaults = getXMLConfigs(defaultsFile);
            configs.addConfiguration(defaults);
        } catch (ConfigurationException e) {
            LOGGER.error("{}. Exit", e);
            throw new CriticalException("Configure error", e);
        }

        addRunDate();
        addRunDateTime();

        LOGGER.trace("{}", configsAsString(ConfigIndex.SYSTEM));
        LOGGER.debug("{}", configsAsString(ConfigIndex.PROVIDED));
        LOGGER.debug("{}", configsAsString(ConfigIndex.DEFAULTS));
        LOGGER.debug("Initialized Configs");

        LOGGER.info("Config precedence - SYSTEM, PROVIDED, DEFAULTS");
        LOGGER.info(
                "Use gotz.properties or system property to override defaults");
    }

    // when config not found, default value may be used in some cases
    // otherwise usually exceptionRule is translated to higher level
    // CriticalException
    // which is unrecoverable. Hence warn is used in here instead of error
    public String getConfig(final String key) throws ConfigNotFoundException {
        String value = configs.getString(key);
        if (value == null) {
            LOGGER.warn("Config [{}] not found. Check prefix and key.", key);
            throw new ConfigNotFoundException(key);
        }
        return value;
    }

    public String[] getConfigArray(final String key)
            throws ConfigNotFoundException {
        String[] values = configs.getStringArray(key);
        if (values.length == 0) {
            LOGGER.warn("Config [{}] not found. Check prefix and key.", key);
            throw new ConfigNotFoundException(key);
        }
        return values;
    }

    public final CompositeConfiguration getConfigs() {
        return configs;
    }

    protected void setConfigs(final CompositeConfiguration configs) {
        this.configs = configs;
    }

    public final Configuration getConfiguration(final ConfigIndex index) {
        return configs.getConfiguration(index.ordinal());
    }

    public Date getRunDate() {
        try {
            Date runDate = null;
            String dateStr = getConfig("gotz.runDate"); //$NON-NLS-1$
            String patterns = getConfig("gotz.dateParsePattern"); //$NON-NLS-1$
            runDate = DateUtils.parseDate(dateStr, new String[] {patterns});
            return runDate;
        } catch (ParseException | ConfigNotFoundException e) {
            LOGGER.error("RunDate error. {}", e); //$NON-NLS-1$
            throw new CriticalException("config failure", e);
        }
    }

    public Date getRunDateTime() {
        try {
            Date runDateTime = null;
            String dateTimeStr = getConfig("gotz.runDateTime"); //$NON-NLS-1$
            String patterns = getConfig("gotz.dateTimeParsePattern"); //$NON-NLS-1$
            runDateTime =
                    DateUtils.parseDate(dateTimeStr, new String[] {patterns});
            return runDateTime;
        } catch (ParseException | ConfigNotFoundException e) {
            LOGGER.error("Run Date error. {}", e); //$NON-NLS-1$
            throw new CriticalException("config failure", e);
        }
    }

    public Date getHighDate() {
        try {
            Date highDate = null;
            String dateStr = getConfig("gotz.highDate"); //$NON-NLS-1$
            String[] patterns = getConfigArray("gotz.dateTimeParsePattern"); //$NON-NLS-1$
            highDate = DateUtils.parseDate(dateStr, patterns);
            return highDate;
        } catch (ParseException | ConfigNotFoundException e) {
            LOGGER.error("{}", e); //$NON-NLS-1$
            throw new CriticalException("config failure", e);
        }

    }

    public ORM getOrmType() {
        ORM orm = ORM.JDO;
        try {
            String ormName = getConfig("gotz.datastore.orm");
            if (StringUtils.compareIgnoreCase(ormName, "jdo") == 0) {
                orm = ORM.JDO;
            }
            if (StringUtils.compareIgnoreCase(ormName, "jpa") == 0) {
                orm = ORM.JPA;
            }
        } catch (ConfigNotFoundException e) {
            LOGGER.error("{}", e.getLocalizedMessage());
            LOGGER.trace("", e);
        }
        return orm;
    }

    public boolean isTestMode() {
        StackTraceElement[] stackElements =
                Thread.currentThread().getStackTrace();
        StackTraceElement stackElement =
                stackElements[stackElements.length - 1];
        String mainClass = stackElement.getClassName();
        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner";
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter";
        if (mainClass.equals(mavenTestRunner)) {
            return true;
        }
        if (mainClass.equals(eclipseTestRunner)) {
            return true;
        }
        return false;
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(configs.getString("gotz.mode"),
                "dev");
    }

    // private methods

    private Configuration getPropertiesConfigs(final String fileName)
            throws ConfigurationException {

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                                .configure(new Parameters().properties()
                                        .setFileName(fileName)
                                        .setThrowExceptionOnMissing(true)
                                        .setListDelimiterHandler(
                                                new DefaultListDelimiterHandler(
                                                        ';')));
        return builder.getConfiguration();
    }

    private Configuration getXMLConfigs(final String fileName)
            throws ConfigurationException {

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class).configure(
                        new Parameters().properties().setFileName(fileName)
                                .setThrowExceptionOnMissing(true)
                                .setListDelimiterHandler(
                                        new DefaultListDelimiterHandler(';')));

        return builder.getConfiguration();
    }

    private void addRunDate() {
        Date runDate = new Date();
        String runDateStr = configs.getString("gotz.runDate"); //$NON-NLS-1$
        if (runDateStr == null) {
            String dateFormat = configs.getString("gotz.dateParsePattern"); //$NON-NLS-1$
            runDateStr = DateFormatUtils.format(runDate, dateFormat);
        }
        configs.addProperty("gotz.runDate", runDateStr);
    }

    private void addRunDateTime() {
        Date runDateTime = new Date();
        String runDateTimeStr = configs.getString("gotz.runDateTime"); //$NON-NLS-1$
        if (runDateTimeStr == null) {
            String dateTimeFormat =
                    configs.getString("gotz.dateTimeParsePattern");
            runDateTimeStr =
                    DateFormatUtils.format(runDateTime, dateTimeFormat);
        }
        configs.addProperty("gotz.runDateTime", runDateTimeStr); //$NON-NLS-1$
    }

    private String configsAsString(final ConfigIndex index) {
        Configuration config = getConfiguration(index);
        Iterator<String> keys = config.getKeys();

        StringBuilder sb = new StringBuilder();
        sb.append(index);
        sb.append(System.lineSeparator());
        while (keys.hasNext()) {
            String key = keys.next();
            sb.append(Util.logIndent());
            sb.append(key);
            sb.append(" = ");
            sb.append(configs.getProperty(key));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
