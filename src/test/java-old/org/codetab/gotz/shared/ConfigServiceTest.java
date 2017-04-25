package org.codetab.gotz.shared;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.shared.ConfigService.ConfigIndex;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigServiceTest {

    private static final int DEFAULT_CONFIGS_COUNT = 18;

    private static final int USER_PROVIDED_CONFIGS_COUNT = 20;

    private static CompositeConfiguration orgConfig;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /*
     * clear user provided configs as majority of tests are about default configs and save
     * it as orgConfig. In some tests, we load test configs as user provided configs.
     * Before start of each test we revert back to orgConfig
     */
    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        orgConfig = ConfigService.INSTANCE.getConfigs();
        orgConfig.getConfiguration(ConfigIndex.PROVIDED.ordinal()).clear();
    }

    /*
     * in the end switch to gotz-test.properties as user provided properties
     *
     */
    @AfterClass
    public static void tearDownAfterClass() throws IOException {
        ConfigService.INSTANCE.init("gotz-test.properties", "gotz-default.xml");
    }

    @Before
    public void setUp() {
        ConfigService.INSTANCE.setConfigs(orgConfig);
    }

    @Test
    public void testConfigIndex() {
        // for test coverage of enum, we need to run both values and valueOf
        assertEquals(ConfigIndex.SYSTEM, ConfigIndex.values()[0]);
        assertEquals(ConfigIndex.PROVIDED, ConfigIndex.values()[1]);
        assertEquals(ConfigIndex.DEFAULTS, ConfigIndex.values()[2]);
        assertEquals(ConfigIndex.SYSTEM, ConfigIndex.valueOf("SYSTEM"));
        assertEquals(ConfigIndex.PROVIDED, ConfigIndex.valueOf("PROVIDED"));
        assertEquals(ConfigIndex.DEFAULTS, ConfigIndex.valueOf("DEFAULTS"));
    }

    @Test
    public void testSingleton() {
        ConfigService i1 = ConfigService.INSTANCE;
        ConfigService i2 = ConfigService.INSTANCE;
        assertNotNull(i1);
        assertSame(i1, i2);
    }

    @Test
    public void testGetConfig() {
        String expected = "gotz/properties/property";
        String actual = ConfigService.INSTANCE.getConfig("gotz.propertyPattern");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetConfigNull() {
        MonitorService.instance().start();
        exception.expect(IllegalStateException.class);
        ConfigService.INSTANCE.getConfig("undefined");
    }

    @Test
    public void testGetConfigArray() {
        String[] expected = {"dd-MM-yyyy", "dd/MM/yyyy"};
        String[] actual = ConfigService.INSTANCE.getConfigArray("gotz.dateParsePattern");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetConfigArrayNull() {
        MonitorService.instance().start();
        exception.expect(IllegalStateException.class);
        ConfigService.INSTANCE.getConfigArray("undefined");
    }

    @Test
    public void testDefaultConfigs() throws Exception {
        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.DEFAULTS);

        int expectedSize = DEFAULT_CONFIGS_COUNT;
        assertEquals(expectedSize, configuration.size());

        assertEquals("gotz/properties/property",
                configuration.getString("gotz.propertyPattern"));

        assertEquals("/bean.xml", configuration.getString("gotz.beanFile"));
        assertEquals("/schema/gotz.xsd", configuration.getString("gotz.schemaFile"));
        assertEquals("org.codetab.gotz.ext.LocatorSeeder",
                configuration.getString("gotz.seederClass"));

        assertEquals("datastore", configuration.getString("gotz.datastore.name"));
        assertEquals("jdo", configuration.getString("gotz.datastore.orm"));
        assertEquals("jdoconfig.properties",
                configuration.getString("gotz.datastore.configFile"));

        assertEquals("6", configuration.getString("gotz.poolsize.seeder"));
        assertEquals("4", configuration.getString("gotz.poolsize.loader"));
        assertEquals("4", configuration.getString("gotz.poolsize.parser"));
        assertEquals("4", configuration.getString("gotz.poolsize.filter"));
        assertEquals("4", configuration.getString("gotz.poolsize.transformer"));
        assertEquals("2", configuration.getString("gotz.poolsize.appender"));

        assertEquals("120000", configuration.getString("gotz.webClient.timeout"));
        assertEquals("Mozilla/5.0", configuration.getString("gotz.webClient.userAgent"));

        assertEquals("31-12-2099 23:59:59.999", configuration.getString("gotz.highDate"));
        String[] dateTimePatterns = {"dd-MM-yyyy HH:mm:ss.SSS",
        "dd/MM/yyyy HH:mm:ss.SSS"};
        assertArrayEquals(dateTimePatterns,
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        String[] datePatterns = {"dd-MM-yyyy", "dd/MM/yyyy"};
        assertArrayEquals(datePatterns,
                configuration.getStringArray("gotz.dateParsePattern"));
    }

    @Test
    public void testConfigsInvalidFiles() {
        MonitorService.instance().start();
        exception.expect(IllegalStateException.class);
        ConfigService.INSTANCE.init("xyz", "xyz");
    }

    @Test
    public void testConfigsInvalidUserProvidedFile() {
        ConfigService.INSTANCE.init("xyz", "gotz-default.xml");
        Configuration defaults = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.DEFAULTS);
        assertEquals(DEFAULT_CONFIGS_COUNT, defaults.size());

        Configuration userProvided = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.PROVIDED);
        assertEquals(0, userProvided.size());
    }

    /*
     * user provided configs are tested by loading gotz-test.properties
     */
    @Test
    public void testUserProvidedConfigs() {

        ConfigService.INSTANCE.init("gotz-provided-test.properties", "gotz-default.xml");

        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.PROVIDED);

        assertEquals(USER_PROVIDED_CONFIGS_COUNT, configuration.size());

        assertEquals("user-gotz/properties/property",
                configuration.getString("gotz.propertyPattern"));

        assertEquals("user-/bean.xml", configuration.getString("gotz.beanFile"));
        assertEquals("user-/schema/gotz.xsd", configuration.getString("gotz.schemaFile"));
        assertEquals("user-org.codetab.gotz.ext.LocatorSeeder",
                configuration.getString("gotz.seederClass"));

        assertEquals("user-datastore", configuration.getString("gotz.datastore.name"));
        assertEquals("user-jdo", configuration.getString("gotz.datastore.orm"));
        assertEquals("user-jdoconfig.properties",
                configuration.getString("gotz.datastore.configFile"));

        assertEquals("user-6", configuration.getString("gotz.poolsize.seeder"));
        assertEquals("user-4", configuration.getString("gotz.poolsize.loader"));
        assertEquals("user-4", configuration.getString("gotz.poolsize.parser"));
        assertEquals("user-4", configuration.getString("gotz.poolsize.filter"));
        assertEquals("user-4", configuration.getString("gotz.poolsize.transformer"));
        assertEquals("user-2", configuration.getString("gotz.poolsize.appender"));

        assertEquals("user-120000", configuration.getString("gotz.webClient.timeout"));
        assertEquals("user-Mozilla/5.0",
                configuration.getString("gotz.webClient.userAgent"));

        assertEquals("12-31-2060 23:59:59.999", configuration.getString("gotz.highDate"));
        String[] dateTimePatterns = {"MM-dd-yyyy HH:mm:ss.SSS",
        "MM/dd/yyyy HH:mm:ss.SSS"};
        assertArrayEquals(dateTimePatterns,
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        String[] datePatterns = {"MM-dd-yyyy", "MM/dd/yyyy"};
        assertArrayEquals(datePatterns,
                configuration.getStringArray("gotz.dateParsePattern"));

        assertEquals("03-30-2017", configuration.getString("gotz.runDate"));
        assertEquals("03-31-2017 01:02:03.004",
                configuration.getString("gotz.runDateTime"));
    }

    /*
     * user may give dates in different format and we test that here by loading
     * gotz-test.properties
     */
    @Test
    public void testUserProvidedDates() throws ParseException {
        ConfigService.INSTANCE.init("gotz-provided-test.properties", "gotz-default.xml");
        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.PROVIDED);

        Date userHighDate = ConfigService.INSTANCE.getHighDate();
        Date expectedDate = DateUtils.parseDate(configuration.getString("gotz.highDate"),
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        assertEquals(expectedDate, userHighDate);

        Date userRunDate = ConfigService.INSTANCE.getRunDate();
        expectedDate = DateUtils.parseDate(configuration.getString("gotz.runDate"),
                configuration.getStringArray("gotz.dateParsePattern"));
        assertEquals(expectedDate, userRunDate);

        Date userRunDateTime = ConfigService.INSTANCE.getRunDateTime();
        expectedDate = DateUtils.parseDate(configuration.getString("gotz.runDateTime"),
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        assertEquals(expectedDate, userRunDateTime);
    }

    @Test
    public void testGetRunDate() throws Exception {
        String runDate = ConfigService.INSTANCE.getConfig("gotz.runDate");
        String[] patterns = ConfigService.INSTANCE
                .getConfigArray("gotz.dateParsePattern");
        Date expectedDate = DateUtils.parseDate(runDate, patterns);

        assertEquals(expectedDate, ConfigService.INSTANCE.getRunDate());
    }

    @Test
    public void testGetRunDateInvalidPattern() throws Exception {
        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.DEFAULTS);
        String[] dateParsePattern = configuration.getStringArray("gotz.dateParsePattern");

        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateParsePattern", invalidPattern);

        exception.expect(IllegalStateException.class);
        // we need to restore dateParsePattern hence try block
        try {
            ConfigService.INSTANCE.getRunDate();
        } catch (IllegalStateException e) {
            configuration.setProperty("gotz.dateParsePattern", dateParsePattern);
            throw e;
        }
    }

    @Test
    public void testGetRunDateTime() throws Exception {
        String runDate = ConfigService.INSTANCE.getConfigs()
                .getString("gotz.runDateTime");
        String[] patterns = ConfigService.INSTANCE.getConfigs()
                .getStringArray("gotz.dateTimeParsePattern");
        Date expectedDate = DateUtils.parseDate(runDate, patterns);

        assertEquals(expectedDate, ConfigService.INSTANCE.getRunDateTime());
    }

    @Test
    public void testGetRunDateTimeInvalidPattern() throws Exception {
        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.DEFAULTS);

        String[] dateTimeParsePattern = configuration
                .getStringArray("gotz.dateTimeParsePattern");

        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateTimeParsePattern", invalidPattern);

        exception.expect(IllegalStateException.class);
        // try block to restore dateParsePattern
        try {
            ConfigService.INSTANCE.getRunDateTime();
        } catch (IllegalStateException e) {
            configuration.setProperty("gotz.dateTimeParsePattern", dateTimeParsePattern);
            throw e;
        }
    }

    @Test
    public void testHighDate() throws Exception {
        String runDate = ConfigService.INSTANCE.getConfigs().getString("gotz.highDate");
        String[] patterns = ConfigService.INSTANCE.getConfigs()
                .getStringArray("gotz.dateTimeParsePattern");
        Date expectedDate = DateUtils.parseDate(runDate, patterns);

        assertEquals(expectedDate, ConfigService.INSTANCE.getHighDate());
    }

    @Test
    public void testGetHighDateInvalidPattern() throws Exception {
        Configuration configuration = ConfigService.INSTANCE
                .getConfiguration(ConfigIndex.DEFAULTS);
        String[] dateTimeParsePattern = configuration
                .getStringArray("gotz.dateTimeParsePattern");

        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateTimeParsePattern", invalidPattern);

        exception.expect(IllegalStateException.class);
        // try block to restore dateParsePattern
        try {
            ConfigService.INSTANCE.getHighDate();
        } catch (IllegalStateException e) {
            configuration.setProperty("gotz.dateTimeParsePattern", dateTimeParsePattern);
            throw e;
        }
    }

    @Test
    public void testIsTestMode() {
        assertTrue(ConfigService.INSTANCE.isTestMode());
    }

    @Test
    public void testIsDevMode() {
        assertFalse(ConfigService.INSTANCE.isDevMode());
        System.setProperty("gotz.mode", "dev");
        assertTrue(ConfigService.INSTANCE.isDevMode());
        System.setProperty("gotz.mode", "");
    }
}
