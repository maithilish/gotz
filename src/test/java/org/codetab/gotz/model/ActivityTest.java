package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.codetab.gotz.model.Activity.Type;
import org.junit.Test;

public class ActivityTest {

    @Test
    public void testActivityTypeString() {
        Activity act = new Activity(Type.FATAL, "test");
        assertEquals(Type.FATAL, act.getType());
        assertEquals("test", act.getMessage());
        assertNull(act.getThrowable());
    }

    @Test
    public void testActivityTypeStringThrowable() {
        Throwable t = new Throwable("exception");
        Activity act = new Activity(Type.FATAL, "test", t);
        assertEquals(Type.FATAL, act.getType());
        assertEquals("test", act.getMessage());
        assertSame(t, act.getThrowable());
    }

    @Test
    public void testType() {
        // for test coverage of enum, we need to run both values and valueOf
        assertEquals(Type.GIVENUP, Type.values()[0]);
        assertEquals(Type.CONFIG, Type.values()[1]);
        assertEquals(Type.SUMMARY, Type.values()[2]);
        assertEquals(Type.FATAL, Type.values()[3]);
        assertEquals(Type.GIVENUP, Type.valueOf("GIVENUP"));
        assertEquals(Type.CONFIG, Type.valueOf("CONFIG"));
        assertEquals(Type.SUMMARY, Type.valueOf("SUMMARY"));
        assertEquals(Type.FATAL, Type.valueOf("FATAL"));
    }

    @Test
    public void testActivityToString() {
        Activity act = new Activity(Type.FATAL, "test");
        String expected = getExprectedString(Type.FATAL, "test", null);
        assertEquals(expected, act.toString());
    }

    @Test
    public void testActivityToStringWithThrowable() {
        Throwable t = new Throwable("exception");
        Activity act = new Activity(Type.FATAL, "test", t);
        String expected = getExprectedString(Type.FATAL, "test", t);
        assertEquals(expected, act.toString());
    }

    private String getExprectedString(Type type, String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Activity [type=");
        sb.append(type);
        sb.append("\n          message=");
        sb.append(message);
        if (throwable != null) {
            sb.append("\n          throwable=");
            sb.append(throwable);
        }
        sb.append("]");
        return sb.toString();
    }

}
