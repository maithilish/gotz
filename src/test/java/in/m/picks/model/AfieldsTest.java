package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AfieldsTest {

	@Test
	public void testAfields() {
		Afield afield = new Afield("tname", "tvalue");

		Afields afields = new Afields();
		afields.addAfield(afield);

		Afields afieldsCopy = new Afields(afields);

		assertNotNull(afieldsCopy.getAfields());
		assertEquals(1, afieldsCopy.getAfields().size());

		assertNotSame(afields, afieldsCopy);
		assertTrue(afields.equals(afieldsCopy) && afieldsCopy.equals(afields));

		Afield afieldCopy = afieldsCopy.getAfield("tname");
		assertNotSame(afield, afieldCopy);
		assertTrue(afield.equals(afieldCopy) && afieldCopy.equals(afield));
	}

	@Test
	public void testFields() {
		Afields afields = new Afields();
		assertNotNull(afields.getAfields());
		assertEquals(0, afields.getAfields().size());
	}

	@Test
	public void testGetField() {
		Afield af = new Afield("tname", "tvalue");
		Afields afields = new Afields();
		afields.addAfield(af);

		Afield result = afields.getAfield("tname");
		assertSame(af, result);
		assertEquals(af, result);
	}

	@Test
	public void testGetAfieldsByGroup() {
		Afield query1 = new Afield("tname1", "tvalue1");
		query1.setGroup("query");
		Afield query2 = new Afield("tname2", "tvalue2");
		query2.setGroup("query");
		Afield script1 = new Afield("tname3", "tvalue3");
		script1.setGroup("script");

		Afields afields = new Afields();
		afields.addAfield(query1);
		afields.addAfield(query2);
		afields.addAfield(script1);

		Afields queries = afields.getAfieldsByGroup("query");
		assertEquals(2, queries.size());
		assertTrue(queries.getAfields().contains(query1));
		assertTrue(queries.getAfields().contains(query2));

		Afields scripts = afields.getAfieldsByGroup("script");
		assertEquals(1, scripts.size());
		assertTrue(scripts.getAfields().contains(script1));

		Afields xyz = afields.getAfieldsByGroup("xyz");
		assertEquals(0, xyz.size());
	}

	@Test
	public void testGetAfieldsByGroupNull() {
		Afield other1 = new Afield("tname4", "tvalue4"); // group null

		Afields afields = new Afields();
		afields.addAfield(other1);

		Afields others = afields.getAfieldsByGroup(null);
		assertEquals(others.size(), 1);
		assertTrue(others.getAfields().contains(other1));
	}

	@Test
	public void testGetAfieldsByName() {
		Afield query1 = new Afield("tname1", "tvalue1");
		query1.setGroup("query");
		Afield query2 = new Afield("tname2", "tvalue2");
		query2.setGroup("query");
		Afield script1 = new Afield("tname3", "tvalue3");
		script1.setGroup("script");

		Afields afields = new Afields();
		afields.addAfield(query1);
		afields.addAfield(query2);
		afields.addAfield(script1);

		Afields queries = afields.getAfieldsByName("tname2");
		assertEquals(1, queries.size());
		assertTrue(queries.getAfields().contains(query2));

		Afields scripts = afields.getAfieldsByName("tname3");
		assertEquals(1, scripts.size());
		assertTrue(scripts.getAfields().contains(script1));

		Afields xyz = afields.getAfieldsByGroup("xyz");
		assertEquals(0, xyz.size());

	}

	@Test
	public void testGetFieldNull() {
		Afields afields = new Afields();
		assertNull(afields.getAfield("xyz"));
	}

	@Test
	public void testAddField() {
		Afields afields = new Afields();
		assertEquals(0, afields.getAfields().size());

		Afield af = new Afield("tname", "tvalue");
		afields.addAfield(af);
		assertEquals(1, afields.getAfields().size());
		assertTrue(afields.getAfields().contains(af));

		Afield afWithNewValue = new Afield("tname", "new value");
		afields.addAfield(afWithNewValue);
		assertFalse(afields.getAfields().contains(af));
		assertTrue(afields.getAfields().contains(afWithNewValue));
	}

	@Test
	public void testEqualsObject() {
		Afields afs = new Afields();
		Afield af = new Afield("tname", "tvalue");
		afs.addAfield(af);

		Afields afsCopy = new Afields();
		Afield afCopy = new Afield("tname", "tvalue");
		afsCopy.addAfield(afCopy);

		assertTrue(afs != afsCopy);
		assertTrue(afs.equals(afsCopy) && afsCopy.equals(afs));
		assertTrue(afs.hashCode() == afsCopy.hashCode());
	}

	@Test
	public void testHashCode() {
		Afields afields = new Afields();
		assertEquals(32, afields.hashCode());

		Afield af = new Afield("tname", "tvalue");
		afields.addAfield(af);
		assertEquals(-1693638872, afields.hashCode());
	}

}
