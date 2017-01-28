package in.m.picks.shared;

import static org.junit.Assert.assertEquals;
import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Afield;
import in.m.picks.model.Locator;
import in.m.picks.util.AccessUtil;

import org.junit.Before;
import org.junit.Test;

public class AccessUtilTest {

	private Locator locator;
	private Afield afield;

	@Before
	public void setup() {
		locator = new Locator();
		afield = new Afield();
		afield.setName("testkey");
	}

	@Test
	public void testGetStringValue() throws AfieldNotFoundException {
		afield.setValue("testvalue");
		locator.addAfield(afield);
		String value = AccessUtil.getStringValue(locator, "testkey");
		assertEquals("testvalue", value);
	}

	@Test(expected = AfieldNotFoundException.class)
	public void testGetStringValueNotFoundException() throws AfieldNotFoundException {
		AccessUtil.getStringValue(locator, "testkey");
	}

	@Test
	public void testGetIntValue() throws AfieldNotFoundException {
		afield.setValue("1");
		locator.addAfield(afield);
		int value = AccessUtil.getIntValue(locator, "testkey");
		assertEquals(1, value);
	}

	@Test(expected = NumberFormatException.class)
	public void testGetIntValueFormatException() throws AfieldNotFoundException {
		afield.setValue("one");
		locator.addAfield(afield);
		AccessUtil.getIntValue(locator, "testkey");
	}

	@Test(expected = AfieldNotFoundException.class)
	public void testGetIntValueNotFoundException() throws AfieldNotFoundException {
		AccessUtil.getIntValue(locator, "testkey");
	}

	@Test
	public void testUpdateField() throws AfieldNotFoundException {
		afield.setValue("before value");
		locator.addAfield(afield);

		String value = AccessUtil.getStringValue(locator, "testkey");
		assertEquals("before value", value);

		AccessUtil.updateAfield(locator, "testkey", "after value");
		value = AccessUtil.getStringValue(locator, "testkey");
		assertEquals("after value", value);
	}

	@Test(expected = AfieldNotFoundException.class)
	public void testUpdateFieldNotFoundException() throws AfieldNotFoundException {
		AccessUtil.updateAfield(locator, "testkey", "after value");
	}

}
