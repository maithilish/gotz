package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ConfigService.ConfigIndex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigServiceTest {

    private static final int DEFAULT_CONFIGS_COUNT = 18;

    private static final int USER_PROVIDED_CONFIGS_COUNT = 20;

    @Mock
    private CompositeConfiguration configs;

    @InjectMocks
    private ConfigService configService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConfigIndex() {
        // then
        // for test coverage of enum, we need to run both values and valueOf
        assertThat(ConfigIndex.SYSTEM).isEqualTo(ConfigIndex.values()[0]);
        assertThat(ConfigIndex.PROVIDED).isEqualTo(ConfigIndex.values()[1]);
        assertThat(ConfigIndex.DEFAULTS).isEqualTo(ConfigIndex.values()[2]);
        assertThat(ConfigIndex.SYSTEM).isEqualTo(ConfigIndex.valueOf("SYSTEM"));
        assertThat(ConfigIndex.PROVIDED).isEqualTo(ConfigIndex.valueOf("PROVIDED"));
        assertThat(ConfigIndex.DEFAULTS).isEqualTo(ConfigIndex.valueOf("DEFAULTS"));
    }

    @Test
    public void testSingleton() {
        // given
        DInjector dInjector = new DInjector().instance(DInjector.class);

        // when
        ConfigService instanceA = dInjector.instance(ConfigService.class);
        ConfigService instanceB = dInjector.instance(ConfigService.class);

        // then
        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testGetConfig() throws ConfigNotFoundException {
        // given
        given(configs.getString("xyz")).willReturn("xxx");

        // when
        configService.getConfig("xyz");

        // then
        verify(configs).getString("xyz");
    }

    @Test
    public void testGetConfigNull() throws ConfigNotFoundException {
        // given
        given(configs.getString("xyz")).willReturn(null);

        // then
        exception.expect(ConfigNotFoundException.class);

        // when
        configService.getConfig("xyz"); // sut
    }

    @Test
    public void testGetConfigArray() throws ConfigNotFoundException {
        // given
        String[] array = {"x", "y"};
        given(configs.getStringArray("xyz")).willReturn(array);

        // when
        configService.getConfigArray("xyz");

        // then
        verify(configs).getStringArray("xyz");
    }

    @Test
    public void testGetConfigArrayNull() throws ConfigNotFoundException {
        // given
        String[] array = {}; // zero length
        given(configs.getStringArray("xyz")).willReturn(array);

        // then
        exception.expect(ConfigNotFoundException.class);

        // when
        configService.getConfigArray("xyz"); // sut
    }

    @Test
    public void testDefaultConfigs() throws Exception {
        // given
        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        // when
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.DEFAULTS);

        // then
        int expectedSize = DEFAULT_CONFIGS_COUNT;
        assertThat(expectedSize).isEqualTo(configuration.size());

        assertThat("gotz/properties/property")
        .isEqualTo(configuration.getString("gotz.propertyPattern"));

        assertThat("/bean.xml").isEqualTo(configuration.getString("gotz.beanFile"));
        assertThat("/schema/gotz.xsd")
        .isEqualTo(configuration.getString("gotz.schemaFile"));
        assertThat("org.codetab.gotz.ext.LocatorSeeder")
        .isEqualTo(configuration.getString("gotz.seederClass"));

        assertThat("datastore").isEqualTo(configuration.getString("gotz.datastore.name"));
        assertThat("jdo").isEqualTo(configuration.getString("gotz.datastore.orm"));
        assertThat("/jdoconfig.properties")
        .isEqualTo(configuration.getString("gotz.datastore.configFile"));

        assertThat("6").isEqualTo(configuration.getString("gotz.poolsize.seeder"));
        assertThat("4").isEqualTo(configuration.getString("gotz.poolsize.loader"));
        assertThat("4").isEqualTo(configuration.getString("gotz.poolsize.parser"));
        assertThat("4").isEqualTo(configuration.getString("gotz.poolsize.filter"));
        assertThat("4").isEqualTo(configuration.getString("gotz.poolsize.transformer"));
        assertThat("2").isEqualTo(configuration.getString("gotz.poolsize.appender"));

        assertThat("120000").isEqualTo(configuration.getString("gotz.webClient.timeout"));
        assertThat("Mozilla/5.0")
        .isEqualTo(configuration.getString("gotz.webClient.userAgent"));

        assertThat("31-12-2099 23:59:59.999")
        .isEqualTo(configuration.getString("gotz.highDate"));
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
        // then
        exception.expect(CriticalException.class);

        // when
        configService.init("xyz", "xyz");
    }

    @Test
    public void testConfigsInvalidUserProvidedFile() {
        // given
        configService.init("xyz", "gotz-default.xml");
        // when
        Configuration defaults = configService.getConfiguration(ConfigIndex.DEFAULTS);
        // then
        assertThat(defaults.size()).isEqualTo(DEFAULT_CONFIGS_COUNT);

        // when
        Configuration userProvided = configService.getConfiguration(ConfigIndex.PROVIDED);
        // then
        assertThat(userProvided.size()).isEqualTo(0);
    }

    /*
     * user provided configs are tested by loading gotz-test.properties
     */
    @Test
    public void testUserProvidedConfigs() {
        // given
        configService.init("gotz-provided-test.properties", "gotz-default.xml");

        // when
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.PROVIDED);

        // then
        assertThat(configuration.size()).isEqualTo(USER_PROVIDED_CONFIGS_COUNT);

        assertThat("user-gotz/properties/property")
        .isEqualTo(configuration.getString("gotz.propertyPattern"));

        assertThat("user-/bean.xml").isEqualTo(configuration.getString("gotz.beanFile"));
        assertThat("user-/schema/gotz.xsd")
        .isEqualTo(configuration.getString("gotz.schemaFile"));
        assertThat("user-org.codetab.gotz.ext.LocatorSeeder")
        .isEqualTo(configuration.getString("gotz.seederClass"));

        assertThat("user-datastore")
        .isEqualTo(configuration.getString("gotz.datastore.name"));
        assertThat("user-jdo").isEqualTo(configuration.getString("gotz.datastore.orm"));
        assertThat("user-jdoconfig.properties")
        .isEqualTo(configuration.getString("gotz.datastore.configFile"));

        assertThat("user-6").isEqualTo(configuration.getString("gotz.poolsize.seeder"));
        assertThat("user-4").isEqualTo(configuration.getString("gotz.poolsize.loader"));
        assertThat("user-4").isEqualTo(configuration.getString("gotz.poolsize.parser"));
        assertThat("user-4").isEqualTo(configuration.getString("gotz.poolsize.filter"));
        assertThat("user-4")
        .isEqualTo(configuration.getString("gotz.poolsize.transformer"));
        assertThat("user-2").isEqualTo(configuration.getString("gotz.poolsize.appender"));

        assertThat("user-120000")
        .isEqualTo(configuration.getString("gotz.webClient.timeout"));
        assertThat("user-Mozilla/5.0")
        .isEqualTo(configuration.getString("gotz.webClient.userAgent"));

        assertThat("12-31-2060 23:59:59.999")
        .isEqualTo(configuration.getString("gotz.highDate"));
        String[] dateTimePatterns = {"MM-dd-yyyy HH:mm:ss.SSS",
        "MM/dd/yyyy HH:mm:ss.SSS"};
        assertArrayEquals(dateTimePatterns,
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        String[] datePatterns = {"MM-dd-yyyy", "MM/dd/yyyy"};
        assertArrayEquals(datePatterns,
                configuration.getStringArray("gotz.dateParsePattern"));

        assertThat("03-30-2017").isEqualTo(configuration.getString("gotz.runDate"));
        assertThat("03-31-2017 01:02:03.004")
        .isEqualTo(configuration.getString("gotz.runDateTime"));
    }

    /*
     * user may give dates in different format and we test that here by loading
     * gotz-test.properties
     */
    @Test
    public void testUserProvidedDates() throws ParseException {
        // given
        configService.init("gotz-provided-test.properties", "gotz-default.xml");

        // when
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.PROVIDED);

        Date userHighDate = configService.getHighDate();
        Date expectedDate = DateUtils.parseDate(configuration.getString("gotz.highDate"),
                configuration.getStringArray("gotz.dateTimeParsePattern"));

        // then
        assertThat(expectedDate).isEqualTo(userHighDate);

        Date userRunDate = configService.getRunDate();
        expectedDate = DateUtils.parseDate(configuration.getString("gotz.runDate"),
                configuration.getStringArray("gotz.dateParsePattern"));
        assertThat(expectedDate).isEqualTo(userRunDate);

        Date userRunDateTime = configService.getRunDateTime();
        expectedDate = DateUtils.parseDate(configuration.getString("gotz.runDateTime"),
                configuration.getStringArray("gotz.dateTimeParsePattern"));
        assertThat(expectedDate).isEqualTo(userRunDateTime);
    }

    @Test
    public void testGetRunDate() throws ConfigNotFoundException, ParseException {
        // given
        configService.init("xyz", "gotz-default.xml");
        String runDate = configService.getConfig("gotz.runDate");
        String[] patterns = configService.getConfigArray("gotz.dateParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        // when
        Date actual = configService.getRunDate();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateInvalidPattern() throws Exception {
        // given
        configService.init("xyz", "gotz-default.xml");
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.DEFAULTS);
        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateParsePattern", invalidPattern);

        // then
        exception.expect(CriticalException.class);

        // when
        configService.getRunDate();
    }

    @Test
    public void testGetRunDateTime() throws Exception {
        // given
        configService.init("xyz", "gotz-default.xml");
        String runDate = configService.getConfigs().getString("gotz.runDateTime");
        String[] patterns = configService.getConfigs()
                .getStringArray("gotz.dateTimeParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        // when
        Date actual = configService.getRunDateTime();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateTimeInvalidPattern() throws Exception {
        // given
        configService.init("xyz", "gotz-default.xml");
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.DEFAULTS);

        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateTimeParsePattern", invalidPattern);

        // then
        exception.expect(CriticalException.class);

        // when
        configService.getRunDateTime();
    }

    @Test
    public void testHighDate() throws Exception {
        // given
        configService.init("xyz", "gotz-default.xml");
        String runDate = configService.getConfigs().getString("gotz.highDate");
        String[] patterns = configService.getConfigs()
                .getStringArray("gotz.dateTimeParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        // then
        Date actual = configService.getHighDate();

        // when
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetOrmType() throws Exception {
        // given
        given(configs.getString("gotz.datastore.orm")).willReturn("jdo").willReturn("jDo")
        .willReturn("jpa").willReturn("jPa")
        .willThrow(ConfigNotFoundException.class);

        // when then
        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
    }

    @Test
    public void testGetHighDateInvalidPattern() throws Exception {
        // given
        configService.init("xyz", "gotz-default.xml");
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.DEFAULTS);
        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("gotz.dateTimeParsePattern", invalidPattern);

        // then
        exception.expect(CriticalException.class);

        // when
        configService.getHighDate();
    }

    @Test
    public void testIsTestMode() {
        assertThat(configService.isTestMode()).isTrue();
    }

    @Test
    public void testIsDevMode() {
        // given
        configService.init("xyz", "gotz-default.xml");
        Configuration configuration = configService
                .getConfiguration(ConfigIndex.DEFAULTS);
        assertThat(configService.isDevMode()).isFalse();
        configuration.setProperty("gotz.mode", "dev");

        // when
        boolean devMode = configService.isDevMode();

        // then
        assertThat(devMode).isTrue();
    }
}
