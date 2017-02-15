package in.m.picks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataTest {

	@Test
	public void testData() {
		Data data = new Data();

		assertNotNull(data.getMembers());
		assertEquals(0, data.getMembers().size());
	}

	@Test
	public void testAddMember() {
		Data data = new Data();
		Member m1 = new Member();
		data.addMember(m1);

		assertEquals(1, data.getMembers().size());
		assertTrue(data.getMembers().contains(m1));
	}

}
