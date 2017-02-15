package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ColComparatorTest {

	@Test
	public void testCompareNullCol() {
		Member m1 = new Member();
		Member m2 = new Member();

		ColComparator cc = new ColComparator();
		assertEquals(0, cc.compare(m1, m2));
	}

	@Test
	public void testCompare() {
		Member m1 = new Member();
		Axis col1 = new Axis();
		col1.setName("col");
		m1.addAxis(col1);

		Member m2 = new Member();
		Axis col2 = new Axis();
		col2.setName("col");
		m2.addAxis(col2);

		ColComparator cc = new ColComparator();

		col1.setOrder(1);
		col2.setOrder(1);
		assertTrue(cc.compare(m1, m2) == 0);

		col1.setOrder(2);
		col2.setOrder(1);
		assertTrue(cc.compare(m1, m2) > 0);

		col1.setOrder(1);
		col2.setOrder(2);
		assertTrue(cc.compare(m1, m2) < 0);
	}

}
