package org.codetab.gotz.shared;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

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
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ConfigService {

    INSTANCE;

    enum ConfigIndex {
        SYSTEM, PROPERTIES, DEFAULTS
    }

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private CompositeConfiguration configs;

    ConfigService() {

        logger.info("Initializing Configs");

        configs = loadConfigs();
        addRunDate();
        addRunDateTime();

        logger.trace("{}", configsAsString(ConfigIndex.SYSTEM));
        logger.debug("{}", configsAsString(ConfigIndex.PROPERTIES));
        logger.debug("{}", configsAsString(ConfigIndex.DEFAULTS));
        logger.debug("Initialized Configs");

        logger.info("Config precedence - SYSTEM, PROPERTIES, DEFAULTS");
        logger.info("Use gotz.properties or system property to override defaults");
    }

    private String configsAsString(final ConfigIndex index) {
        Configuration config = getConfig(index);
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

    public String getConfig(final String key) {
        String value = configs.getString(key);
        if (value == null) {
            logger.error("{}", "Config [{}] not found. Check prefix and key.", key);
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return value;
    }

    public String[] getConfigArray(final String key) {
        String[] values = configs.getStringArray(key);
        if (values == null) {
            logger.error("{}", "Config [{}] not found. Check prefix and key.", key);
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return values;
    }

    public final CompositeConfiguration getConfigs() {
        return configs;
    }

    public final Configuration getConfig(final ConfigIndex index) {
        Configuration config;
        switch (index) {
        case SYSTEM:
            config = configs.getConfiguration(0);
            break;
        case PROPERTIES:
            config = configs.getConfiguration(1);
            break;
        case DEFAULTS:
            config = configs.getConfiguration(2);
            break;
        default:
            config = configs.getConfiguration(2);
            break;
        }
        return config;
    }

    public Date getRunDate() {
        Date runDate = null;
        String dateStr = getConfig("gotz.runDate"); //$NON-NLS-1$
        String patterns = getConfig("gotz.dateParsePattern"); //$NON-NLS-1$

        try {
            runDate = DateUtils.parseDate(dateStr, new String[] {patterns});
        } catch (ParseException e) {
            logger.error("Run Date error. {}", e); //$NON-NLS-1$
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return runDate;
    }

    public Date getRunDateTime() {
        Date runDateTime = null;
        String dateTimeStr = getConfig("gotz.runDateTime"); //$NON-NLS-1$
        String patterns = getConfig("gotz.dateTimeParsePattern"); //$NON-NLS-1$

        try {
            runDateTime = DateUtils.parseDate(dateTimeStr, new String[] {patterns});
        } catch (ParseException e) {
            logger.error("Run Date error. {}", e); //$NON-NLS-1$
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return runDateTime;
    }

    public Date getHighDate() {
        Date highDate = null;
        String dateStr = getConfig("gotz.highDate"); //$NON-NLS-1$
        String[] patterns = getConfigArray("gotz.dateTimeParsePattern"); //$NON-NLS-1$
        try {
            highDate = DateUtils.parseDate(dateStr, patterns);
        } catch (ParseException e) {
            logger.error("{}", e); //$NON-NLS-1$
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return highDate;
    }

    private CompositeConfiguration loadConfigs() {

        CompositeConfiguration configs = new CompositeConfiguration();

        configs.addConfiguration(new SystemConfiguration());

        try {
            configs.addConfiguration(userProvidedConfigs());
        } catch (ConfigurationException e) {
            logger.info(e.getLocalizedMessage() + ". " + "Using default properties");
        }

        try {
            configs.addConfiguration(defaultConfigs());
        } catch (ConfigurationException e) {
            logger.error("{}. Exit", e);
            MonitorService.INSTANCE.triggerFatal("Config failure");
        }
        return configs;
    }

    private Configuration userProvidedConfigs() throws ConfigurationException {

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder;
        builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                PropertiesConfiguration.class).configure(
                        new Parameters().properties().setFileName("gotz.properties")
                                .setThrowExceptionOnMissing(true).setListDelimiterHandler(
                                        new DefaultListDelimiterHandler(';')));

        Configuration userProvided = builder.getConfiguration();
        return userProvided;
    }

    private Configuration defaultConfigs() throws ConfigurationException {

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class).configure(
                        new Parameters().properties().setFileName("gotz-default.xml")
                                .setThrowExceptionOnMissing(true).setListDelimiterHandler(
                                        new DefaultListDelimiterHandler(';')));

        Configuration defaultConfigs = builder.getConfiguration();
        return defaultConfigs;
    }

    private void addRunDate() {
        Date runDate = new Date();
        String runDateStr = configs.getString("gotz.runDate"); //$NON-NLS-1$
        if (runDateStr == null) {
            String dateFormat = configs.getString("gotz.dateParsePattern"); //$NON-NLS-1$
            runDateStr = DateFormatUtils.format(runDate, dateFormat);
            configs.addProperty("gotz.runDate", runDateStr); //$NON-NLS-1$
        }
    }

    private void addRunDateTime() {
        Date runDateTime = new Date();
        String runDateTimeStr = configs.getString("gotz.runDateTime"); //$NON-NLS-1$
        if (runDateTimeStr == null) {
            String dateTimeFormat = configs.getString("gotz.dateTimeParsePattern");
            runDateTimeStr = DateFormatUtils.format(runDateTime, dateTimeFormat);
            configs.addProperty("gotz.runDateTime", runDateTimeStr); //$NON-NLS-1$
        }
    }

    public boolean isTestMode() {
        return StringUtils.isNotBlank(System.getProperty("surefire.test.class.path"));
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(configs.getString("gotz.mode"), "dev");
    }
}
