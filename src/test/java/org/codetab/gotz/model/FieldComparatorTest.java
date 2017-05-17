package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FieldComparatorTest {

    @Test
    public void testCompare() {
        Field f1 = new Field();
        Field f2 = new Field();

        FieldComparator fc = new FieldComparator();

        f1.setValue("1");
        f2.setValue("1");
        assertThat(fc.compare(f1, f2)).isEqualTo(0);

        f1.setValue("2");
        f2.setValue("1");
        assertThat(fc.compare(f1, f2)).isEqualTo(1);

        f1.setValue("1");
        f2.setValue("2");
        assertThat(fc.compare(f1, f2)).isEqualTo(-1);
    }

}
