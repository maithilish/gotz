package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AxisTest {

	@Test
	public void testHashCode() {
		Axis axis = getAxis();
		assertEquals(1239074157, axis.hashCode());
	}

	@Test
	public void testEqualsObject() {
		Axis a1 = getAxis();
		Axis a2 = getAxis();

		assertTrue(a1 != a2);
		assertTrue(a1.equals(a2) && a2.equals(a1));
		assertTrue(a1.hashCode() == a2.hashCode());
	}

	@Test
	public void testCompareTo() {
		// axis comparison uses AxisNames enum values
		Axis a1 = getAxis();
		Axis a2 = getAxis();
		assertTrue(a1.compareTo(a2) == 0);
		assertTrue(a2.compareTo(a1) == 0);
		a2.setName("row");
		assertTrue(a1.compareTo(a2) < 0);
		assertTrue(a2.compareTo(a1) > 0);
	}

	private Axis getAxis() {
		Axis axis = new Axis();
		axis.setName("col");
		axis.setIndex(5);
		axis.setOrder(4);
		axis.setMatch("tmatch");
		axis.setValue("tvalue");
		return axis;
	}
}
