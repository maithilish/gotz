package org.codetab.gotz.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * Util tests.
 * @author Maithilish
 *
 */
public class UtilTest {

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Test
    public void testDeepClone() throws ClassNotFoundException, IOException {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");

        List<List<String>> obj = new ArrayList<>();
        obj.add(list);

        List<?> actual = Util.deepClone(List.class, obj);

        int objHash = System.identityHashCode(obj);
        int objListHash = System.identityHashCode(obj.get(0));

        int actualHash = System.identityHashCode(actual);
        int actualListHash = System.identityHashCode(actual.get(0));

        assertThat(obj).isEqualTo(actual);
        assertThat(objHash).isNotEqualTo(actualHash);
        assertThat(objListHash).isNotEqualTo(actualListHash);
    }

    @Test
    public void testDeepCloneNullParams()
            throws ClassNotFoundException, IOException {
        try {
            Util.deepClone(null, "obj");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("ofClass must not be null");
        }

        try {
            Util.deepClone(String.class, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("object must not be null");
        }
    }

    @Test
    public void testHasNulls() {
        String x = "x";
        String y = "y";
        String z = null;
        assertThat(Util.hasNulls(x)).isFalse();
        assertThat(Util.hasNulls(x, y)).isFalse();
        assertThat(Util.hasNulls(x, z)).isTrue();
        assertThat(Util.hasNulls(z)).isTrue();
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
        assertThat(4).isEqualTo(xy.size());
        assertThat(xy.contains(getSet("y1", "x1"))).isTrue();
        assertThat(xy.contains(getSet("y1", "x2"))).isTrue();
        assertThat(xy.contains(getSet("y2", "x1"))).isTrue();
        assertThat(xy.contains(getSet("y2", "x2"))).isTrue();

        Set<Set<Object>> xyz = Util.cartesianProduct(x, y, z);
        assertThat(4).isEqualTo(xyz.size());
        assertThat(xyz.contains(getSet("y1", "x1", "z1"))).isTrue();
        assertThat(xyz.contains(getSet("y1", "x2", "z1"))).isTrue();
        assertThat(xyz.contains(getSet("y2", "x1", "z1"))).isTrue();
        assertThat(xyz.contains(getSet("y2", "x2", "z1"))).isTrue();

        Set<Set<Object>> xz = Util.cartesianProduct(x, z);
        assertThat(2).isEqualTo(xz.size());
        assertThat(xz.contains(getSet("x1", "z1"))).isTrue();
        assertThat(xz.contains(getSet("x2", "z1"))).isTrue();
    }

    @Test
    public void testCartesianProductEmptySet() {
        Set<String> x = new HashSet<String>();
        x.add("x1");
        x.add("x2");
        Set<String> emptySet = new HashSet<String>();

        testRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct(x, emptySet);
    }

    @Test
    public void testCartesianProductException() {
        Set<String> x = new HashSet<String>();
        x.add("x1");
        x.add("x2");

        testRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct(x);
    }

    @Test
    public void testCartesianProductNoArg() {
        testRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct();
    }

    @Test
    public void testCartesianProductNullParams() {
        Set<String> set = null;
        testRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct(set);
    }

    private Set<String> getSet(final String... strs) {
        Set<String> set = new HashSet<String>();
        for (String str : strs) {
            set.add(str);
        }
        return set;
    }

    @Test
    public void testGetgetJson() {
        String expected = "{\"name\":\"x\",\"value\":\"y\"}";
        Field field = TestUtil.createField("x", "y");
        String actual = Util.getJson(field, false);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetgetJsonPrettyPrint() {

        String line = System.lineSeparator();
        String expected = Util.buildString("{", line, "  \"name\": \"x\",",
                line, "  \"value\": \"y\"", line, "}");
        Field field = TestUtil.createField("x", "y");
        String actual = Util.getJson(field, true);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetJsonNullParams() {
        try {
            Util.getJson(null, false);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("object must not be null");
        }
    }

    @Test
    public void testGetgetIndentedJson() {
        String expected = "\t\t\t{\"name\":\"x\",\"value\":\"y\"}";
        Field field = TestUtil.createField("x", "y");
        String actual = Util.getIndentedJson(field, false);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetgetIndentedJsonPrettyPrint() {

        String line = System.lineSeparator();
        String indent = "\t\t\t";
        String expected = Util.buildString(indent, "{", line, indent,
                "  \"name\": \"x\",", line, indent, "  \"value\": \"y\"", line,
                indent, "}");
        Field field = TestUtil.createField("x", "y");
        String actual = Util.getIndentedJson(field, true);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetIndentedJsonNullParams() {
        try {
            Util.getIndentedJson(null, false);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("object must not be null");
        }
    }

    @Test
    public void testBuildString() {
        assertThat("a").isEqualTo(Util.buildString("a"));
        assertThat("ab").isEqualTo(Util.buildString("a", "b"));
    }

    @Test
    public void testBuildStringNullParams() {
        String str = null;

        String actual = Util.buildString(str);

        assertThat(actual).isNull();
    }

    @Test
    public void testPraseTemporalAmount() {
        TemporalAmount ta = Util.parseTemporalAmount("P2M");
        assertThat(ta.get(ChronoUnit.MONTHS)).isEqualTo(2L);

        ta = Util.parseTemporalAmount("PT5S");
        assertThat(ta.get(ChronoUnit.SECONDS)).isEqualTo(5L);

        testRule.expect(DateTimeParseException.class);
        ta = Util.parseTemporalAmount("X2M");
    }

    @Test
    public void testParseTemporalAmountNullParams() {
        try {
            Util.parseTemporalAmount(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("text must not be null");
        }
    }

    @Test
    public void testStripe() {
        String str = Util.stripe("test", 2, null, null);
        assertThat(StringUtils.startsWith(str, "test")).isTrue();
        assertThat(StringUtils.endsWith(str, "test")).isTrue();
        assertThat(0).isEqualTo(StringUtils.countMatches(str, "\n"));

        str = Util.stripe("test", 2, "prefix", "suffix");
        assertThat("prefixtestsuffix").isEqualTo(str);
        assertThat(0).isEqualTo(StringUtils.countMatches(str, "\n"));

        str = Util.stripe("line1\nline2\nline3\nline4", 1, null, null);
        assertThat(2).isEqualTo(StringUtils.countMatches(str, "\n")); // head 1
                                                                      // dots 1

        str = Util.stripe("line1\nline2\nline3\nline4\nline5\nline6", 2, null,
                null);
        assertThat(4).isEqualTo(StringUtils.countMatches(str, "\n")); // head 2
                                                                      // dots 1
        // tail 1
    }

    @Test
    public void testStripeNullParams() {
        try {
            Util.stripe(null, 1, "prefix", "suffix");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("string must not be null");
        }
    }

    @Test
    public void testHead() {
        String str = Util.head("line1\nline2\nline3\nline4\nline5\nline6", 3);
        assertThat(str.equals("line1\nline2\nline3")).isTrue();

        str = Util.head("line1\nline2", 1);
        assertThat("line1").isEqualTo(str);

        str = Util.head("line1\nline2", 0);
        assertThat("line1").isEqualTo(str);
    }

    @Test
    public void testHeadNullParams() {
        try {
            Util.head(null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("string must not be null");
        }
    }

    @Test
    public void testTail() {
        String str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6", 3);
        assertThat(str.equals("line4\nline5\nline6")).isTrue();

        str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6\n", 3);
        assertThat("line4\nline5\nline6\n").isEqualTo(str);

        str = Util.tail("line1\nline2", 1);
        assertThat("line2").isEqualTo(str);

        str = Util.tail("line1\nline2", 0);
        assertThat("line2").isEqualTo(str);
    }

    @Test
    public void testTailNullParams() {
        try {
            Util.tail(null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("string must not be null");
        }
    }

    @Test
    public void testGetMessage() {
        String expected = "FieldNotFoundException: [test]";
        String actual = Util.getMessage(new FieldNotFoundException("test"));
        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetMessageNullParams() {
        try {
            Util.getMessage(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("exception must not be null");
        }
    }

    @Test
    public void testGetPropertiesAsString() {
        Properties props = new Properties();
        props.put("x", "xv");
        props.put("y", "yv");

        String actual = Util.getPropertiesAsString(props);

        String expected = Util.LINE + Util.logIndent() + "x=xv" + Util.LINE
                + Util.logIndent() + "y=yv" + Util.LINE;

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetPropertiesAsNullParams() {
        try {
            Util.getPropertiesAsString(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("properties must not be null");
        }
    }

    @Test
    public void testWellDefinedUtilityClass()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        TestUtil.assertUtilityClassWellDefined(Util.class);
    }
}
