package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

public class FieldsBaseTest {

    // enhanced class to test excludes in hashcode and equals
    class Enhanced extends FieldsBase {
        private static final long serialVersionUID = 1L;
        private String name, value;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Iterator<FieldsBase> iterator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String indentToString(final String indent) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected = ToStringBuilder.reflectionToString(t1,
                ToStringStyle.MULTI_LINE_STYLE);
        assertThat(t1.toString()).isEqualTo(expected);
    }

    private List<Enhanced> createTestObjects() {
        Enhanced t1 = new Enhanced();

        Enhanced t2 = new Enhanced();
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
