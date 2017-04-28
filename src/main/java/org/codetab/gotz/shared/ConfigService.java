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
import org.codetab.gotz.exception.FatalException;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfigService {

    enum ConfigIndex {
        SYSTEM, PROVIDED, DEFAULTS
    }

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private CompositeConfiguration configs;

    @Inject
    private ConfigService() {
    }

    public void init(String userProvidedFile, String defaultsFile) throws FatalException {
        logger.info("Initializing Configs");

        configs = new CompositeConfiguration();

        SystemConfiguration systemConfigs = new SystemConfiguration();
        configs.addConfiguration(systemConfigs);

        try {
            Configuration userProvided = getPropertiesConfigs(userProvidedFile);
            configs.addConfiguration(userProvided);
        } catch (ConfigurationException e) {
            configs.addConfiguration(new PropertiesConfiguration());
            logger.info(e.getLocalizedMessage() + ". " + "Using default properties");
        }

        try {
            Configuration defaults = getXMLConfigs(defaultsFile);
            configs.addConfiguration(defaults);
        } catch (ConfigurationException e) {
            logger.error("{}. Exit", e);
            throw new FatalException("Configure error");
        }

        addRunDate();
        addRunDateTime();

        logger.trace("{}", configsAsString(ConfigIndex.SYSTEM));
        logger.debug("{}", configsAsString(ConfigIndex.PROVIDED));
        logger.debug("{}", configsAsString(ConfigIndex.DEFAULTS));
        logger.debug("Initialized Configs");

        logger.info("Config precedence - SYSTEM, PROVIDED, DEFAULTS");
        logger.info("Use gotz.properties or system property to override defaults");
    }

    public String getConfig(final String key) throws FatalException {
        String value = configs.getString(key);
        if (value == null) {
            logger.error("{}", "Config [{}] not found. Check prefix and key.", key);
            throw new FatalException("Configure error");
        }
        return value;
    }

    public String[] getConfigArray(final String key) throws FatalException {
        String[] values = configs.getStringArray(key);
        if (values.length == 0) {
            logger.error("{}", "Config [{}] not found. Check prefix and key.", key);
            throw new FatalException("config failure");
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

    public Date getRunDate() throws FatalException {
        Date runDate = null;
        String dateStr = getConfig("gotz.runDate"); //$NON-NLS-1$
        String patterns = getConfig("gotz.dateParsePattern"); //$NON-NLS-1$
        try {
            runDate = DateUtils.parseDate(dateStr, new String[] {patterns});
        } catch (ParseException e) {
            logger.error("RunDate error. {}", e); //$NON-NLS-1$
            throw new FatalException("config failure");
        }
        return runDate;
    }

    public Date getRunDateTime() throws FatalException {
        Date runDateTime = null;
        String dateTimeStr = getConfig("gotz.runDateTime"); //$NON-NLS-1$
        String patterns = getConfig("gotz.dateTimeParsePattern"); //$NON-NLS-1$

        try {
            runDateTime = DateUtils.parseDate(dateTimeStr, new String[] {patterns});
        } catch (ParseException e) {
            logger.error("Run Date error. {}", e); //$NON-NLS-1$
            throw new FatalException("config failure");
        }
        return runDateTime;
    }

    public Date getHighDate() throws FatalException {
        Date highDate = null;
        String dateStr = getConfig("gotz.highDate"); //$NON-NLS-1$
        String[] patterns = getConfigArray("gotz.dateTimeParsePattern"); //$NON-NLS-1$
        try {
            highDate = DateUtils.parseDate(dateStr, patterns);
        } catch (ParseException e) {
            logger.error("{}", e); //$NON-NLS-1$
            throw new FatalException("config failure");
        }
        return highDate;
    }

    // public boolean isTestMode() {
    // boolean testMode = false;
    // String command = System.getProperty("sun.java.command");
    // String surefirePath = System.getProperty("surefire.test.class.path");
    // if (command.contains("surefire")) {
    // testMode = true;
    // }
    // if (StringUtils.isNotBlank(surefirePath)) {
    // testMode = true;
    // }
    // if (command.contains("junit.runner.RemoteTestRunner")) {
    // testMode = true;
    // }
    // return testMode;
    // }

    public boolean isTestMode() {
        StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackElement = stackElements[stackElements.length - 1];
        String mainClass = stackElement.getClassName();
        String eclipseTestRunner = "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner";
        String mavenTestRunner = "org.apache.maven.surefire.booter.ForkedBooter";
        if (mainClass.equals(mavenTestRunner)) {
            return true;
        }
        if (mainClass.equals(eclipseTestRunner)) {
            return true;
        }
        return false;
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(configs.getString("gotz.mode"), "dev");
    }

    // private methods

    private Configuration getPropertiesConfigs(String fileName)
            throws ConfigurationException {

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                PropertiesConfiguration.class)
                .configure(new Parameters().properties().setFileName(fileName)
                        .setThrowExceptionOnMissing(true).setListDelimiterHandler(
                                new DefaultListDelimiterHandler(';')));

        Configuration configs = builder.getConfiguration();
        return configs;
    }

    private Configuration getXMLConfigs(String fileName) throws ConfigurationException {

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class)
                .configure(new Parameters().properties().setFileName(fileName)
                        .setThrowExceptionOnMissing(true).setListDelimiterHandler(
                                new DefaultListDelimiterHandler(';')));

        Configuration configs = builder.getConfiguration();
        return configs;
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
            String dateTimeFormat = configs.getString("gotz.dateTimeParsePattern");
            runDateTimeStr = DateFormatUtils.format(runDateTime, dateTimeFormat);
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
