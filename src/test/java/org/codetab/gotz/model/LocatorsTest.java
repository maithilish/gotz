package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

public class LocatorsTest {

    // locators is just a holder and not compared with each other.
    // tests for hashCode, equals are for coverage
    @Test
    public void testHashCode() {
        Locators t1 = new Locators();
        Locators t2 = new Locators();

        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        Locators t1 = new Locators();
        Locators t2 = new Locators();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        Locators t1 = new Locators();
        String expected = ToStringBuilder.reflectionToString(t1,
                ToStringStyle.MULTI_LINE_STYLE);

        assertThat(t1.toString()).isEqualTo(expected);
    }


    @Test
    public void testGetLocator() {
        Locators t1 = new Locators();
        List<Locator> list = t1.getLocator();

        assertThat(list).isNotNull();
        // for coverage when not null
        assertThat(t1.getLocator()).isSameAs(list);
    }

    @Test
    public void testGetLocators() {
        Locators t1 = new Locators();
        List<Locators> list = t1.getLocators();

        assertThat(list).isNotNull();
        // for coverage when not null
        assertThat(t1.getLocators()).isSameAs(list);
    }

    @Test
    public void testGetGroup() {
        Locators t1 = new Locators();
        t1.setGroup("x");

        assertThat(t1.getGroup()).isEqualTo("x");
    }
}
