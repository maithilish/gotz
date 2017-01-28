package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class AfieldTest {

	private Afield af;

	@Before
	public void setUp() throws Exception {
		af = new Afield();
		af.setName("testName");
		af.setValue("testValue");
		af.setGroup("testGroup");
		af.setCascade(true);
	}

	@Test
	public void testHashCode() {
		assertEquals(1848016769, af.hashCode());
	}

	@Test
	public void testAfieldAfield() {
		Afield afCopy = new Afield(af);
		assertEquals(af, afCopy);
		assertEquals(af.hashCode(), afCopy.hashCode());
		assertNotSame(af, afCopy);
	}

	@Test
	public void testAfieldStringString() {
		Afield af1 = new Afield("testname", "testvalue");
		assertEquals(af1.getName(), "testname");
		assertEquals(af1.getValue(), "testvalue");
		assertNull(af1.getGroup());
		assertFalse(af1.isCascade());
	}

	@Test
	public void testEqualsObject() {
		Afield afCopy = new Afield();
		afCopy.setName("testName");
		afCopy.setValue("testValue");
		afCopy.setGroup("testGroup");
		afCopy.setCascade(true);

		assertTrue(af != afCopy); // objects are different
		assertTrue(af.equals(afCopy) && afCopy.equals(af)); // test they are
															// equal
		assertTrue(af.hashCode() == afCopy.hashCode());
	}

}
