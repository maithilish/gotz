package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.codetab.gotz.util.FieldsUtil;
import org.junit.Test;

public class AxisTest {
    @Test
    public void testAxis() {
        Axis axis = createAxis();

        assertEquals(AxisName.ROW, axis.getName());
        assertEquals(Integer.valueOf(1), axis.getIndex());
        assertEquals("tmatch", axis.getMatch());
        assertEquals(Integer.valueOf(2), axis.getOrder());
        assertEquals("tvalue", axis.getValue());
    }

    @Test
    public void testGetFields() {
        Axis axis = new Axis();
        assertEquals(0, axis.getFields().size());

        Field field = new Field();
        field.setName("tname");
        field.setValue("tvalue");
        axis.getFields().add(field);

        assertEquals(1, axis.getFields().size());
        assertSame(field, axis.getFields().get(0));
    }

    @Test
    public void testCompareTo() {
        Axis a1 = createAxisWithField();
        Axis a2 = createAxisWithField();
        Axis a3 = createAxisWithField();

        a3.setName(AxisName.COL);

        assertEquals(0, a1.compareTo(a2));
        assertEquals(1, a1.compareTo(a3));
    }

    @Test
    public void testEqualsSymetry() {
        Axis a1 = createAxisWithField();
        Axis a2 = createAxisWithField();
        Axis a3 = createAxisWithField();
        a3.setName(AxisName.COL);

        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertEquals(a1.hashCode(),a2.hashCode());
    }

    @Test
    public void testToString() {
        Axis axis = createAxisWithField();
        assertEquals(expectedToString(axis),axis.toString());
    }

    private Axis createAxis() {
        Axis axis = new Axis();
        axis.setName(AxisName.ROW);
        axis.setIndex(1);
        axis.setMatch("tmatch");
        axis.setOrder(2);
        axis.setValue("tvalue");
        return axis;
    }

    private Axis createAxisWithField() {
        Axis axis = createAxis();
        Field field = new Field();
        field.setName("tname");
        field.setValue("tvalue");
        axis.getFields().add(field);
        return axis;
    }

    private String expectedToString(Axis axis) {
        StringBuilder builder = new StringBuilder();
        builder.append("Axis [name=");
        builder.append(axis.getName());
        builder.append(", value=");
        builder.append(axis.getValue());
        builder.append(", match=");
        builder.append(axis.getMatch());
        builder.append(", index=");
        builder.append(axis.getIndex());
        builder.append(", order=");
        builder.append(axis.getOrder());
        builder.append(",");
        builder.append(FieldsUtil.getFormattedFields(axis.getFields()));
        builder.append("]");
        return builder.toString();
    }
}
