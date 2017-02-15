package in.m.picks.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

public class ConfigsTest {

	@Test
	public void testGetConfig() {
		String expected = "picks/properties/property";
		String actual = ConfigService.INSTANCE.getConfig("picks.propertyPattern");
		assertEquals(expected, actual);
	}

	@Test
	public void testSingleton() {
		ConfigService i1 = ConfigService.INSTANCE;
		ConfigService i2 = ConfigService.INSTANCE;
		assertSame(i1, i2);
	}

	@Test
	public void testDefaultAppConfigs() throws Exception {
		CompositeConfiguration appConfigs = getConfig();
		// config order : system (0), user provided (1) and default (2)
		Configuration appDefaults = appConfigs.getConfiguration(2);
		int expectedConfigs = 11;
		int actualConfigs = 0;
		Iterator<String> i = appDefaults.getKeys();
		while (i.hasNext()) {
			i.next();
			actualConfigs++;
		}
		assertEquals(expectedConfigs, actualConfigs);

		assertEquals("picks/properties/property",
				appDefaults.getString("picks.propertyPattern"));
		// assertEquals("locator.xml",
		// appDefaults.getString("picks.locatorFileName"));
		// assertEquals("afields.xml",
		// appDefaults.getString("picks.afieldsFileName"));
		// assertEquals("datadef.xml",
		// appDefaults.getString("picks.dataDefFileName"));
		assertEquals("2", appDefaults.getString("picks.loaderThreads"));
		assertEquals("4", appDefaults.getString("picks.parserThreads"));
		assertEquals("4", appDefaults.getString("picks.processThreads"));
		assertEquals("120000", appDefaults.getString("picks.webClientTimeout"));
		assertEquals("31-12-2099 23:59:59.999", appDefaults.getString("picks.highDate"));
		assertEquals("dd-MM-yyyy HH:mm:ss.SSS",
				appDefaults.getString("picks.dateTimeParsePattern"));
		assertEquals("dd-MM-yyyy",
				appDefaults.getString("picks.dateParsePattern"));

		assertEquals("jdo", appDefaults.getString("picks.orm"));
		assertEquals("datastore", appDefaults.getString("picks.datastore"));
	}

	/*
	 * @Test public void testDefaultTestConfigs() throws Exception {
	 * CompositeConfiguration testConfigs = getTestConfigs(); // config order :
	 * system (0) and default (1) Configuration testDefaults =
	 * testConfigs.getConfiguration(1); int expectedConfigs = 13; int
	 * actualConfigs = 0; Iterator<String> i = testDefaults.getKeys(); while
	 * (i.hasNext()) { i.next(); actualConfigs++; }
	 * assertEquals(expectedConfigs, actualConfigs);
	 * 
	 * assertEquals("picks/properties/property",
	 * testDefaults.getString("picks.propertyPattern"));
	 * assertEquals("digester.xml",
	 * testDefaults.getString("picks.digesterFileName"));
	 * assertEquals("locator-test.xml",
	 * testDefaults.getString("picks.locatorFileName"));
	 * assertEquals("afields-test.xml",
	 * testDefaults.getString("picks.afieldsFileName"));
	 * assertEquals("datadef-test.xml",
	 * testDefaults.getString("picks.dataDefFileName")); assertEquals("2",
	 * testDefaults.getString("picks.loaderThreads")); assertEquals("4",
	 * testDefaults.getString("picks.parserThreads")); assertEquals("4",
	 * testDefaults.getString("picks.processThreads")); assertEquals("120000",
	 * testDefaults.getString("picks.webClientTimeout"));
	 * assertEquals("31-12-2099 23:59:59.999",
	 * testDefaults.getString("picks.highDate"));
	 * assertEquals("dd-MM-yyyy HH:mm:ss.SSS",
	 * testDefaults.getString("picks.dateParsePattern")); assertEquals("jdo",
	 * testDefaults.getString("picks.orm")); assertEquals("testdatastore",
	 * testDefaults.getString("picks.datastore")); }
	 */

	@Test
	public void testSetRunDate() throws ParseException {
		String runDate = ConfigService.INSTANCE.getConfigs().getString("picks.runDate");
		String pattern = ConfigService.INSTANCE.getConfigs()
				.getString("picks.dateParsePattern");
		DateUtils.parseDate(runDate, new String[] { pattern });
	}

	@Test
	public void testGetRunDate() throws Exception {
		// app configs
		CompositeConfiguration configs = getConfig();
		String runDate = configs.getString("picks.runDate");
		String pattern = configs.getString("picks.dateParsePattern");
		Date expectedDate = DateUtils.parseDate(runDate, new String[] { pattern });

		assertEquals(expectedDate, ConfigService.INSTANCE.getRunDate());

		/*
		 * // test configs configs = getTestConfigs(); runDate =
		 * configs.getString("picks.runDate"); pattern =
		 * configs.getString("picks.dateParsePattern"); expectedDate =
		 * DateUtils.parseDate(runDate, new String[] { pattern });
		 * 
		 * assertEquals(expectedDate, Configs.INSTANCE.getRunDate());
		 */
	}

	@Test
	public void testHighDate() throws Exception {
		// app configs
		CompositeConfiguration configs = getConfig();
		String highDate = configs.getString("picks.highDate");
		String pattern = configs.getString("picks.dateTimeParsePattern");
		Date expectedDate = DateUtils.parseDate(highDate, new String[] { pattern });

		assertEquals(expectedDate, ConfigService.INSTANCE.getHighDate());

		/*
		 * // test configs configs = getTestConfigs(); highDate =
		 * configs.getString("picks.highDate"); pattern =
		 * configs.getString("picks.dateParsePattern"); expectedDate =
		 * DateUtils.parseDate(highDate, new String[] { pattern });
		 * 
		 * assertEquals(expectedDate, Configs.INSTANCE.getHighDate());
		 */
	}

	// returns configs from the array
	private CompositeConfiguration getConfig() throws Exception {
		return ConfigService.INSTANCE.getConfigs();
	}

	/*
	 * private CompositeConfiguration getTestConfigs() throws Exception { Field
	 * field = Configs.class.getDeclaredField("configsArray");
	 * field.setAccessible(true); CompositeConfiguration[] configsArray =
	 * (CompositeConfiguration[]) field .get(Configs.INSTANCE); return
	 * configsArray[Configs.appConfigIndex]; }
	 */
}
