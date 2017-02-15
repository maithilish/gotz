package in.m.picks.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import in.m.picks.util.Util;

public class UtilTest {

	@Test
	public void testHasNulls() {
		String x = "x";
		String y = "y";
		String z = null;
		assertFalse(Util.hasNulls(x));
		assertFalse(Util.hasNulls(x, y));
		assertTrue(Util.hasNulls(x, z));
		assertTrue(Util.hasNulls(z));
	}

	@Test
	public void testCartesianProduct() {
		Set<String> x = new HashSet<String>();
		x.add("x1");
		x.add("x2");
		Set<String> y = new HashSet<String>();
		y.add("y1");
		y.add("y2");
		Set<String> z = new HashSet<String>();
		z.add("z1");

		Set<Set<Object>> xy = Util.cartesianProduct(x, y);
		assertEquals(4, xy.size());
		assertTrue(xy.contains(getSet("y1", "x1")));
		assertTrue(xy.contains(getSet("y1", "x2")));
		assertTrue(xy.contains(getSet("y2", "x1")));
		assertTrue(xy.contains(getSet("y2", "x2")));

		Set<Set<Object>> xyz = Util.cartesianProduct(x, y, z);
		assertEquals(4, xyz.size());
		assertTrue(xyz.contains(getSet("y1", "x1", "z1")));
		assertTrue(xyz.contains(getSet("y1", "x2", "z1")));
		assertTrue(xyz.contains(getSet("y2", "x1", "z1")));
		assertTrue(xyz.contains(getSet("y2", "x2", "z1")));

		Set<Set<Object>> xz = Util.cartesianProduct(x, z);
		assertEquals(2, xz.size());
		assertTrue(xz.contains(getSet("x1", "z1")));
		assertTrue(xz.contains(getSet("x2", "z1")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCartesianProductEmptySet() {
		Set<String> x = new HashSet<String>();
		x.add("x1");
		x.add("x2");
		Set<String> emptySet = new HashSet<String>();
		Util.cartesianProduct(x, emptySet);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCartesianProductException() {
		Set<String> x = new HashSet<String>();
		x.add("x1");
		x.add("x2");

		Util.cartesianProduct(x);
		Util.cartesianProduct();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCartesianProductNoArg() {
		Util.cartesianProduct();
	}

	private Set<String> getSet(String... strs) {
		Set<String> set = new HashSet<String>();
		for (String str : strs) {
			set.add(str);
		}
		return set;
	}

	@Test
	public void testStripe() {
		String str = Util.stripe("test", 2, null, null);
		assertTrue(StringUtils.startsWith(str, "test"));
		assertTrue(StringUtils.endsWith(str, "test"));
		assertEquals(0, StringUtils.countMatches(str, "\n"));

		str = Util.stripe("test", 2, "prefix", "suffix");
		assertTrue(str.equals("prefixtestsuffix"));
		assertEquals(0, StringUtils.countMatches(str, "\n"));

		str = Util.stripe("line1\nline2\nline3\nline4", 1, null, null);
		assertEquals(2, StringUtils.countMatches(str, "\n")); // head 1 dots 1

		str = Util.stripe("line1\nline2\nline3\nline4\nline5\nline6", 2, null, null);
		assertEquals(4, StringUtils.countMatches(str, "\n")); // head 2 dots 1
																// tail 1
	}

	@Test
	public void testHead() {
		String str = Util.head("line1\nline2\nline3\nline4\nline5\nline6", 3);
		assertTrue(str.equals("line1\nline2\nline3"));

		str = Util.head("line1\nline2", 1);
		assertTrue(str.equals("line1"));

		str = Util.head("line1\nline2", 0);
		assertTrue(str.equals("line1"));
	}

	@Test
	public void testTail() {
		String str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6", 3);
		assertTrue(str.equals("line4\nline5\nline6"));

		str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6\n", 3);
		assertTrue(str.equals("line4\nline5\nline6\n"));

		str = Util.tail("line1\nline2", 1);
		assertTrue(str.equals("line2"));

		str = Util.tail("line1\nline2", 0);
		System.out.println(str);
		assertTrue(str.equals("line2"));
	}

}
