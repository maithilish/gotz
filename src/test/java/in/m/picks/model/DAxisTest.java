package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class DAxisTest {

	@Test
	public void testDAxis() {
		DAxis axis = new DAxis();
		assertNotNull(axis.getMembers());
		assertEquals(0, axis.getMembers().size());
	}

	@Test
	public void testSetDefaults() {
		// test default fact member
		DAxis axis = new DAxis();
		axis.setName("fact");
		axis.setDefaults();
		Set<DMember> members = axis.getMembers();
		assertEquals(1, members.size());

		DMember fact = members.iterator().next();
		assertEquals("fact", fact.getName());
		assertEquals(0, (int) fact.getOrder());
	}

	@Test
	public void testSetFilter() {
		DFilter f1 = new DFilter();

		DAxis axis = new DAxis();
		axis.setName("col");
		axis.setFilter(f1);

		assertNotNull(axis.getFilter());
		assertSame(f1, axis.getFilter());
	}

	@Test
	public void testHashCode() {
		DAxis axis = getDAxis();

		assertEquals(1834134596, axis.hashCode());
	}

	@Test
	public void testEqualsObject() {
		DAxis axis1 = getDAxis();
		DAxis axis2 = getDAxis();

		assertNotSame(axis1, axis2);
		assertTrue(axis1.equals(axis2) && axis2.equals(axis1));
		assertTrue(axis1.hashCode() == axis2.hashCode());
	}

	private DAxis getDAxis() {
		DAxis axis = new DAxis();
		axis.setName("col");

		DMember m1 = new DMember();
		m1.setIndex(5);
		m1.setOrder(4);
		m1.setGroup("tgroup");
		m1.setName("tname");
		m1.setMatch("tmatch");
		m1.setAxis("col");
		axis.getMembers().add(m1);

		DFilter f1 = new DFilter();
		axis.setFilter(f1);

		return axis;
	}

}
