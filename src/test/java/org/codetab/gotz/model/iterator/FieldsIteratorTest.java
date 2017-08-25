package org.codetab.gotz.model.iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * FieldsIterator tests.
 * @author Maithilish
 *
 */
public class FieldsIteratorTest {

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFieldsIteratorListOfFieldsBase()
            throws IllegalAccessException {
        Field f1 = TestUtil.createField("f1", "f1v");
        Field f2 = TestUtil.createField("f2", "f2v");
        Field f3 = TestUtil.createField("f3", "f3v");

        Fields g1 = TestUtil.createFields("g1", "g1v", f1);

        List<FieldsBase> fields = TestUtil.asList(g1);
        fields.add(f2);
        fields.add(f3);

        FieldsIterator ite = new FieldsIterator(fields);

        @SuppressWarnings("unchecked")
        Stack<FieldsBase> actual = (Stack<FieldsBase>) FieldUtils
                .readDeclaredField(ite, "stack", true);

        // stacked in reverse order
        assertThat(actual).containsExactly(f3, f2, g1);
    }

    @Test
    public void testFieldsIteratorListOfFieldsBaseNullParams() {
        try {
            List<FieldsBase> list = null;
            new FieldsIterator(list);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("fieldsBaseList must not be null");
        }
    }

    @Test
    public void testFieldsIteratorFieldsBase() {
        Field f1 = TestUtil.createField("f1", "f1v");
        FieldsIterator ite = new FieldsIterator(f1);

        FieldsBase actual = ite.next();
        assertThat(actual).isSameAs(f1);
    }

    @Test
    public void testFieldsIteratorFieldsBaseNullParams() {
        try {
            FieldsBase fb = null;
            new FieldsIterator(fb);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fieldsBase must not be null");
        }
    }

    @Test
    public void testHasNext() {

        FieldsIterator ite = new FieldsIterator(new Field());

        assertThat(ite.hasNext()).isTrue();
        ite.next();
        assertThat(ite.hasNext()).isFalse();
    }

    @Test
    public void testNext() {

        Field f1 = TestUtil.createField("f1", "f1v");
        Field f2 = TestUtil.createField("f2", "f2v");
        Fields fs1 = TestUtil.createFields("fs1", "fs1v", f1);
        Fields g1 = TestUtil.createFields("g1", "g1v", fs1);
        g1.getFields().add(f2);
        List<FieldsBase> fields = TestUtil.asList(g1);

        FieldsIterator ite = new FieldsIterator(fields);

        List<FieldsBase> actual = new ArrayList<>();
        while (ite.hasNext()) {
            actual.add(ite.next());
        }

        assertThat(actual).containsExactly(g1, fs1, f1, f2);
    }

    @Test
    public void testNextEmptyStack() {

        FieldsIterator ite = new FieldsIterator(new Field());

        ite.next();

        testRule.expect(NoSuchElementException.class);
        ite.next();
    }

    @Test
    public void testNextNullFields() {

        Field f1 = TestUtil.createField("f1", "f1v");
        Fields fs1 = null;
        Fields g1 = TestUtil.createFields("g1", "g1v", fs1, f1);
        List<FieldsBase> fields = TestUtil.asList(g1);

        FieldsIterator ite = new FieldsIterator(fields.get(0));

        List<FieldsBase> actual = new ArrayList<>();
        while (ite.hasNext()) {
            actual.add(ite.next());
        }

        assertThat(actual).containsExactly(g1, fs1, f1);
    }

}
