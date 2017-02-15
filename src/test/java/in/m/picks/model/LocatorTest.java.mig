package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LocatorTest {

	private Locator locator;

	@Before
	public void setUp() throws Exception {
		locator = new Locator();
	}

	@Test
	public void testAddResource() {
		Document resource = new Document();
		resource.setId((long) 0);
		locator.addDocument(resource);
		assertEquals(1, locator.getDocuments().size());
		assertTrue(locator.getDocuments().contains(resource));
	}

}
