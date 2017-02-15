package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ResourceTest {

	private Document r1, r2, r3;

	@Before
	public void setup() {
		r1 = new Document();
		r2 = new Document();
		r3 = new Document();

		r1.setId((long) 0);
		r2.setId((long) 0);
		r3.setId((long) 1);
	}

	@Test
	public void testHashCode() {
		assertEquals(r1.hashCode(), r2.hashCode());
		assertNotEquals(r1.hashCode(), r3.hashCode());
		assertEquals(32, r3.hashCode());
	}

	@Test
	public void testEqualsObject() {

		assertNotSame(r1, r2);
		assertTrue(r1.equals(r2) && r2.equals(r1));
		assertTrue(r1.hashCode() == r2.hashCode());
		assertFalse(r1.equals(r3));
	}

}
