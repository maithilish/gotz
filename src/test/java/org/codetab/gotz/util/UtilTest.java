package org.codetab.gotz.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Wrapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UtilTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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

        assertEquals(obj, actual);
        assertNotEquals(objHash, actualHash);
        assertNotEquals(objListHash, actualListHash);
    }

    @Test
    public void testHasNulls() {
        String x = "x";
        String y = "y";
        String z = null;
        assertFalse(Util.hasNulls(x));
        assertFalse(Util.hasNulls(x, y));
        assertTrue(Util.hasNulls(x, z));
        assertTrue(Util.hasNulls(z));
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
        assertEquals(4, xy.size());
        assertTrue(xy.contains(getSet("y1", "x1")));
        assertTrue(xy.contains(getSet("y1", "x2")));
        assertTrue(xy.contains(getSet("y2", "x1")));
        assertTrue(xy.contains(getSet("y2", "x2")));

        Set<Set<Object>> xyz = Util.cartesianProduct(x, y, z);
        assertEquals(4, xyz.size());
        assertTrue(xyz.contains(getSet("y1", "x1", "z1")));
        assertTrue(xyz.contains(getSet("y1", "x2", "z1")));
        assertTrue(xyz.contains(getSet("y2", "x1", "z1")));
        assertTrue(xyz.contains(getSet("y2", "x2", "z1")));

        Set<Set<Object>> xz = Util.cartesianProduct(x, z);
        assertEquals(2, xz.size());
        assertTrue(xz.contains(getSet("x1", "z1")));
        assertTrue(xz.contains(getSet("x2", "z1")));
    }

    @Test
    public void testCartesianProductEmptySet() {
        Set<String> x = new HashSet<String>();
        x.add("x1");
        x.add("x2");
        Set<String> emptySet = new HashSet<String>();

        exceptionRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct(x, emptySet);
    }

    @Test
    public void testCartesianProductException() {
        Set<String> x = new HashSet<String>();
        x.add("x1");
        x.add("x2");

        exceptionRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct(x);
    }

    @Test
    public void testCartesianProductNoArg() {
        exceptionRule.expect(IllegalArgumentException.class);
        Util.cartesianProduct();
    }

    private Set<String> getSet(String... strs) {
        Set<String> set = new HashSet<String>();
        for (String str : strs) {
            set.add(str);
        }
        return set;
    }

    @Test
    public void testGetgetJson() {
        String expected = "{\"name\":\"x\",\"value\":\"y\"}";
        Field field = FieldsUtil.createField("x", "y");
        String actual = Util.getJson(field, false);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetgetJsonPrettyPrint() {

        String line = System.lineSeparator();
        String expected = Util.buildString("{", line, "  \"name\": \"x\",", line,
                "  \"value\": \"y\"", line, "}");
        Field field = FieldsUtil.createField("x", "y");
        String actual = Util.getJson(field, true);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetgetIndentedJson() {
        String expected = "\t\t\t{\"name\":\"x\",\"value\":\"y\"}";
        Field field = FieldsUtil.createField("x", "y");
        String actual = Util.getIndentedJson(field, false);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetgetIndentedJsonPrettyPrint() {

        String line = System.lineSeparator();
        String indent = "\t\t\t";
        String expected = Util.buildString(indent, "{", line, indent,
                "  \"name\": \"x\",", line, indent, "  \"value\": \"y\"", line, indent,
                "}");
        Field field = FieldsUtil.createField("x", "y");
        String actual = Util.getIndentedJson(field, true);

        assertEquals(expected, actual);
    }

    @Test
    public void testBuildString() {
        assertEquals("a", Util.buildString("a"));
        assertEquals("ab", Util.buildString("a", "b"));
    }

    @Test
    public void testUnmarshal() throws JAXBException, FileNotFoundException {
        JAXBContext jc = JAXBContext.newInstance(Wrapper.class);
        Unmarshaller um = jc.createUnmarshaller();
        List<Object> list = Util.unmarshal(um, "/util/bean.xml");
        for (Object o : list) {
            @SuppressWarnings("unchecked")
            JAXBElement<Bean> je = (JAXBElement<Bean>) o;
            Bean bean = je.getValue();
            assertNotNull(bean);
        }
    }

    @Test
    public void testGetResourceAsStream() throws FileNotFoundException {
        InputStream is = Util.getResourceAsStream("/util/bean.xml");
        assertNotNull(is);

        exceptionRule.expect(FileNotFoundException.class);
        Util.getResourceAsStream("bean.xml");
    }

    @Test
    public void testPraseTemporalAmount() {
        TemporalAmount ta = Util.praseTemporalAmount("P2M");
        assertEquals(2, ta.get(ChronoUnit.MONTHS));

        ta = Util.praseTemporalAmount("PT5S");
        assertEquals(5, ta.get(ChronoUnit.SECONDS));

        exceptionRule.expect(DateTimeParseException.class);
        ta = Util.praseTemporalAmount("X2M");
    }

    @Test
    public void testStripe() {
        String str = Util.stripe("test", 2, null, null);
        assertTrue(StringUtils.startsWith(str, "test"));
        assertTrue(StringUtils.endsWith(str, "test"));
        assertEquals(0, StringUtils.countMatches(str, "\n"));

        str = Util.stripe("test", 2, "prefix", "suffix");
        assertEquals("prefixtestsuffix", str);
        assertEquals(0, StringUtils.countMatches(str, "\n"));

        str = Util.stripe("line1\nline2\nline3\nline4", 1, null, null);
        assertEquals(2, StringUtils.countMatches(str, "\n")); // head 1 dots 1

        str = Util.stripe("line1\nline2\nline3\nline4\nline5\nline6", 2, null, null);
        assertEquals(4, StringUtils.countMatches(str, "\n")); // head 2 dots 1
        // tail 1
    }

    @Test
    public void testHead() {
        String str = Util.head("line1\nline2\nline3\nline4\nline5\nline6", 3);
        assertTrue(str.equals("line1\nline2\nline3"));

        str = Util.head("line1\nline2", 1);
        assertEquals("line1", str);

        str = Util.head("line1\nline2", 0);
        assertEquals("line1", str);
    }

    @Test
    public void testTail() {
        String str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6", 3);
        assertTrue(str.equals("line4\nline5\nline6"));

        str = Util.tail("line1\nline2\nline3\nline4\nline5\nline6\n", 3);
        assertEquals("line4\nline5\nline6\n", str);

        str = Util.tail("line1\nline2", 1);
        assertEquals("line2", str);

        str = Util.tail("line1\nline2", 0);
        assertEquals("line2", str);
    }

    @Test
    public void testGetMessage() {
        String expected = "FieldNotFoundException: [test]";
        String actual = Util.getMessage(new FieldNotFoundException("test"));
        assertEquals(expected, actual);
    }

    @Test
    public void testWellDefined() throws NoSuchMethodException, InvocationTargetException,
    InstantiationException, IllegalAccessException {
        assertUtilityClassWellDefined(Util.class);
    }

    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Assert.assertTrue("class must be final", Modifier.isFinal(clazz.getModifiers()));
        Assert.assertEquals("There must be only one constructor", 1,
                clazz.getDeclaredConstructors().length);
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible()
                || !Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                Assert.fail("there exists a non-static method:" + method);
            }
        }
    }
}
