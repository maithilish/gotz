package org.codetab.gotz.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;
import org.codetab.gotz.exception.FieldNotFoundException;
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
 * FieldsUtil tests.
 * @author Maithilish
 *
 */
public class FieldsUtilTest {

    private List<FieldsBase> fields;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        fields = createTestObjects();
    }

    @Test
    public void testUtilityClassWellDefined()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        TestUtil.assertUtilityClassWellDefined(FieldsUtil.class);
    }

    @Test
    public void testCreateField() {

        Field actual = FieldsUtil.createField("x", "y");

        assertThat(actual.getName()).isEqualTo("x");
        assertThat(actual.getValue()).isEqualTo("y");
    }

    @Test
    public void testAsList() {
        Field f = FieldsUtil.createField("x", "y");

        List<FieldsBase> actual = FieldsUtil.asList(f);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).contains(f);
    }

    @Test
    public void testDeepCloneFieldsBase() {
        Field f = TestUtil.createField("a", "va");
        Fields fs = TestUtil.createFields("aa", "vaa", f);

        FieldsBase actual = FieldsUtil.deepClone(fs);

        assertThat(actual).isEqualTo(fs);
        assertThat(actual).isNotSameAs(fs);

        assertThat(((Fields) actual).getFields().get(0)).isEqualTo(f);
        assertThat(((Fields) actual).getFields().get(0)).isNotSameAs(f);
    }

    @Test
    public void testDeepCloneFieldsBaseList() {
        Field f = TestUtil.createField("a", "va");
        Fields fs = TestUtil.createFields("aa", "vaa", f);
        fields = TestUtil.asList(fs);

        List<FieldsBase> actual = FieldsUtil.deepClone(fields);

        assertThat(actual).isEqualTo(fields);
        assertThat(actual).isNotSameAs(fields);

        Fields actualFields = (Fields) actual.get(0);

        assertThat(actualFields.getFields().get(0)).isEqualTo(f);
        assertThat(actualFields.getFields().get(0)).isNotSameAs(f);
    }

    @Test
    public void testGetValue() throws FieldNotFoundException {

        String actual = FieldsUtil.getValue(fields, "a");
        assertThat(actual).isEqualTo("va");

        actual = FieldsUtil.getValue(fields, "bb");
        assertThat(actual).isEqualTo("vbb");
    }

    @Test
    public void testGetValueNull() throws FieldNotFoundException {
        fields = null;

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.getValue(fields, "a");
    }

    @Test
    public void testGetValueEmptyList() throws FieldNotFoundException {
        fields = new ArrayList<>();

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.getValue(fields, "bb");
    }

    @Test
    public void testGetValueFieldNotFound() throws FieldNotFoundException {
        fields = null;

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.getValue(fields, "zzz");
    }

    @Test
    public void testGetRange()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "1-2"));

        Range<Integer> actual = FieldsUtil.getRange(fields, "indexrange");

        assertThat(actual.getMinimum()).isEqualTo(1);
        assertThat(actual.getMaximum()).isEqualTo(2);
    }

    @Test
    public void testGetRangeInvalid()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "a-b"));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetRangeInvalidMix()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "a-2"));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetRangeInvalidStart()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "-1"));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetRangeMinEqualsMax()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "5-5"));
        Range<Integer> actual = FieldsUtil.getRange(fields, "indexrange");

        assertThat(actual.getMinimum()).isEqualTo(5);
        assertThat(actual.getMaximum()).isEqualTo(5);
    }

    @Test
    public void testGetRangeMinGreaterThanMax()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "2-1"));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetRangeSingleNumber()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "5"));
        Range<Integer> actual = FieldsUtil.getRange(fields, "indexrange");

        assertThat(actual.getMinimum()).isEqualTo(5);
        assertThat(actual.getMaximum()).isEqualTo(5);
    }

    @Test
    public void testGetRangeExtraLength()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", "1-2-3"));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetRangeZeroLength()
            throws NumberFormatException, FieldNotFoundException {
        fields = TestUtil.asList(TestUtil.createField("indexrange", ""));

        testRule.expect(NumberFormatException.class);
        FieldsUtil.getRange(fields, "indexrange");
    }

    @Test
    public void testGetField() throws FieldNotFoundException {

        Field actual = FieldsUtil.getField(fields, "bb");

        assertThat(actual.getName()).isEqualTo("bb");
        assertThat(actual.getValue()).isEqualTo("vbb");
    }

    @Test
    public void testGetFieldException() throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.getField(fields, "z");
    }

    @Test
    public void testGetFields() throws FieldNotFoundException {

        FieldsBase expected = null;
        for (FieldsBase fb : fields) {
            if (fb.getName().equals("b")) {
                expected = fb;
            }
        }

        Fields actual = FieldsUtil.getFields(fields, "b");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetFieldsException() throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.getFields(fields, "z");
    }

    @Test
    public void testFilterByValue() throws FieldNotFoundException {

        List<FieldsBase> actual = FieldsUtil.filterByValue(fields, "bb", "vbb");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getName()).isEqualTo("bb");
        assertThat(actual.get(0).getValue()).isEqualTo("vbb");
    }

    @Test
    public void testFilterByValueMulti() throws FieldNotFoundException {

        List<FieldsBase> actual = FieldsUtil.filterByValue(fields, "a", "va");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getName()).isEqualTo("a");
        assertThat(actual.get(0).getValue()).isEqualTo("va");

        actual = FieldsUtil.filterByValue(fields, "a", "va2");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getName()).isEqualTo("a");
        assertThat(actual.get(0).getValue()).isEqualTo("va2");
    }

    @Test
    public void testFilterByValueExceptionValue()
            throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterByValue(fields, "bb", "z");
    }

    @Test
    public void testFilterByValueExceptionName() throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterByValue(fields, "z", "vbb");
    }

    @Test
    public void testFilterByName() throws FieldNotFoundException {
        List<FieldsBase> actual = FieldsUtil.filterByName(fields, "e");

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0).getName()).isEqualTo("e");
        assertThat(actual.get(0).getValue()).isEqualTo("ve");

        assertThat(actual.get(1).getName()).isEqualTo("e");
        assertThat(actual.get(1).getValue()).isEqualTo("veee");
    }

    @Test
    public void testFilterByNameExpectException()
            throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterByName(fields, "x");
    }

    @Test
    public void testFilterByNameFromGroup() throws FieldNotFoundException {
        List<FieldsBase> actual =
                FieldsUtil.filterByGroupName(fields, "g2", "dd");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getName()).isEqualTo("dd");
        assertThat(actual.get(0).getValue()).isEqualTo("vdd");
    }

    @Test
    public void testFilterChildrenByName() throws FieldNotFoundException {
        List<FieldsBase> actual = FieldsUtil.filterChildrenByName(fields, "e");

        assertThat(actual.size()).isEqualTo(1);

        assertThat(actual.get(0).getName()).isEqualTo("e");
        assertThat(actual.get(0).getValue()).isEqualTo("veee");
    }

    @Test
    public void testFilterChildrenByNameException()
            throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterChildrenByName(fields, "x");
    }

    @Test
    public void testFilterByGroup() throws FieldNotFoundException {
        List<FieldsBase> group = FieldsUtil.filterByGroup(fields, "g1");

        assertThat(group.size()).isEqualTo(2);

        Fields actual1 = (Fields) group.get(0);

        assertThat(actual1.getName()).isEqualTo("group");
        assertThat(actual1.getValue()).isEqualTo("g1");

        assertThat(actual1.getFields().size()).isEqualTo(2);
        assertThat(actual1.getFields())
                .contains(TestUtil.createField("c", "vc"));
        assertThat(actual1.getFields())
                .contains(TestUtil.createField("cc", "vcc"));

        Field actual2 = (Field) group.get(1);
        assertThat(actual2).isEqualTo(TestUtil.createField("group", "g1"));
    }

    @Test
    public void testFilterByGroupExpectException()
            throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterByGroup(fields, "x");
    }

    @Test
    public void testFilterByGroupAsFields() throws FieldNotFoundException {
        List<Fields> group = FieldsUtil.filterByGroupAsFields(fields, "g1");

        assertThat(group.size()).isEqualTo(1);

        Fields actual = group.get(0);

        assertThat(actual.getName()).isEqualTo("group");
        assertThat(actual.getValue()).isEqualTo("g1");

        assertThat(actual.getFields().size()).isEqualTo(2);
        assertThat(actual.getFields())
                .contains(TestUtil.createField("c", "vc"));
        assertThat(actual.getFields())
                .contains(TestUtil.createField("cc", "vcc"));

    }

    @Test
    public void testFilterByGroupAsFieldsExpectException()
            throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.filterByGroupAsFields(fields, "x");
    }

    @Test
    public void testPrefixValue() {
        Field f1 = TestUtil.createField("a", "foo");
        Field f2 = TestUtil.createField("a", "bar");
        fields = TestUtil.asList(f1, f2);

        String actual = FieldsUtil.suffixValue(fields, "xyz");

        assertThat(actual).isEqualTo("barfooxyz");
    }

    @Test
    public void testPrefixValueIgnoreFields() {
        Field f1 = TestUtil.createField("a", "foo");
        Field f2 = TestUtil.createField("a", "bar");
        Fields fs = TestUtil.createFields("a", "bar");
        fields = TestUtil.asList(fs, f1, f2);

        String actual = FieldsUtil.suffixValue(fields, "xyz");

        assertThat(actual).isEqualTo("barfooxyz");
    }

    @Test
    public void testReplaceVariablesOfField() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Field f1 = TestUtil.createField("f1", "x");

        Map<String, Field> map = new HashMap<>();
        map.put("F1", f1);

        Field x = TestUtil.createField("x", "some value %{f1.name}");
        Field y = TestUtil.createField("y", "some value %{f1.value}");

        Field ex = TestUtil.createField("x", "some value f1");
        Field ey = TestUtil.createField("y", "some value x");

        fields = TestUtil.asList(x, y);

        List<FieldsBase> actual = FieldsUtil.replaceVariables(fields, map);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactly(ex, ey);

    }

    @Test
    public void testReplaceVariablesIgnoreFields()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Field f1 = TestUtil.createField("f1", "x");

        Map<String, Field> map = new HashMap<>();
        map.put("F1", f1);

        Field x = TestUtil.createField("x", "some value %{f1.name}");
        Fields y = TestUtil.createFields("y", "some value %{f1.value}", x);

        Field ex = TestUtil.createField("x", "some value f1");

        fields = TestUtil.asList(y);

        List<FieldsBase> actual = FieldsUtil.replaceVariables(fields, map);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).containsExactly(ex);

    }

    @Test
    public void testReplaceVariablesNullString() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Field f1 = TestUtil.createField("f1", "x");

        Map<String, Field> map = new HashMap<>();
        map.put("F1", f1);

        Field x = TestUtil.createField("x", null);
        Field y = TestUtil.createField("y", "some value %{f1.value}");

        Field ex = TestUtil.createField("x", null);
        Field ey = TestUtil.createField("y", "some value x");

        fields = TestUtil.asList(x, y);

        List<FieldsBase> actual = FieldsUtil.replaceVariables(fields, map);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactly(ex, ey);

    }

    @Test
    public void testContainsListOfFieldsBaseStringString() {
        assertThat(FieldsUtil.contains(fields, "e", "ve")).isTrue();
        assertThat(FieldsUtil.contains(fields, "ee", "vee")).isTrue();
        assertThat(FieldsUtil.contains(fields, "e", "vx")).isFalse();
        assertThat(FieldsUtil.contains(fields, "x", "ve")).isFalse();
        assertThat(FieldsUtil.contains(fields, "x", "x")).isFalse();

        fields = null;
        assertThat(FieldsUtil.contains(fields, "x", "x")).isFalse();
    }

    @Test
    public void testContainsFieldsStringString() {
        Field f1 = TestUtil.createField("e", "ve");
        Fields fs = TestUtil.createFields("ee", "vee", f1);

        assertThat(FieldsUtil.contains(fs, "e", "ve")).isTrue();
        assertThat(FieldsUtil.contains(fs, "e", "vx")).isFalse();
        assertThat(FieldsUtil.contains(fs, "ee", "vee")).isFalse();
        assertThat(FieldsUtil.contains(fs, "x", "x")).isFalse();

        fs = null;
        assertThat(FieldsUtil.contains(fs, "x", "x")).isFalse();
    }

    @Test
    public void testIsTrueListOfFieldsBaseStringString()
            throws FieldNotFoundException {
        Field f1 = TestUtil.createField("x", "true");
        Field f2 = TestUtil.createField("y", "True");
        Field f3 = TestUtil.createField("z", "false");
        fields = TestUtil.asList(f1, f2, f3);

        assertThat(FieldsUtil.isTrue(fields, "x")).isTrue();
        assertThat(FieldsUtil.isTrue(fields, "y")).isTrue();
        assertThat(FieldsUtil.isTrue(fields, "z")).isFalse();
    }

    @Test
    public void testIsTrueExpectException() throws FieldNotFoundException {

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.isTrue(fields, "x");
    }

    @Test
    public void testIsTrueListOfFieldsBaseString()
            throws FieldNotFoundException {
        Field f1 = TestUtil.createField("x", "true");
        Fields fs1 = TestUtil.createFields("group", "gx", f1);
        fields = TestUtil.asList(fs1);

        assertThat(FieldsUtil.isTrue(fields, "gx", "x")).isTrue();
    }

    @Test
    public void testIsTrueGroupNameExpectException()
            throws FieldNotFoundException {
        Field f1 = TestUtil.createField("x", "true");
        Fields fs1 = TestUtil.createFields("group", "gx", f1);
        fields = TestUtil.asList(fs1);

        testRule.expect(FieldNotFoundException.class);
        FieldsUtil.isTrue(fields, "gy", "x");
    }

    @Test
    public void testIsDefined() {
        assertThat(FieldsUtil.isDefined(fields, "e")).isTrue();
        assertThat(FieldsUtil.isDefined(fields, "ee")).isTrue();
        assertThat(FieldsUtil.isDefined(fields, "x")).isFalse();
    }

    @Test
    public void testIsAnyDefined() {
        assertThat(FieldsUtil.isAnyDefined(fields, "e", "x", "y")).isTrue();
        assertThat(FieldsUtil.isAnyDefined(fields, "x", "y")).isFalse();
    }

    @Test
    public void testIsAllDefined() {
        assertThat(FieldsUtil.isAllDefined(fields, "e", "ee")).isTrue();
        assertThat(FieldsUtil.isAllDefined(fields, "e", "ee", "x")).isFalse();
    }

    @Test
    public void testCountField() {
        assertThat(FieldsUtil.countField(fields)).isEqualTo(12);
    }

    private List<FieldsBase> createTestObjects() {

        /* @formatter:off

         - {name: a, value: va},
         - fields: {name: b, value: vb}
            - {name: bb, value: vbb}
            - {name: bbb, value: vbbb},
         - {name: a, value: va2},
         - fields: {name: group, value: g1}
            - {name: c, value: vc}
            - {name: cc, value: vcc},
         - fields: {name: group, value: g2}
            - fields: {name: d, value: vd}
               - {name: dd, value: vdd}
               - {name: ddd, value: vddd}
               - {name: dddd, value: vdddd}

         * @formatter:on */

        List<FieldsBase> list = new ArrayList<>();

        Field f1 = TestUtil.createField("a", "va");
        list.add(f1);

        Field f2 = TestUtil.createField("bb", "vbb");
        Field f3 = TestUtil.createField("bbb", "vbbb");
        Fields fs1 = TestUtil.createFields("b", "vb", f2, f3);
        list.add(fs1);

        f1 = TestUtil.createField("a", "va2");
        list.add(f1);

        Field f4 = TestUtil.createField("c", "vc");
        Field f5 = TestUtil.createField("cc", "vcc");
        Fields g1 = TestUtil.createFields("group", "g1", f4, f5);
        list.add(g1);
        Field g1f = TestUtil.createField("group", "g1");
        list.add(g1f);

        Field f6 = TestUtil.createField("dd", "vdd");
        Field f7 = TestUtil.createField("ddd", "vddd");
        Field f8 = TestUtil.createField("dddd", "vdddd");
        Fields fs2 = TestUtil.createFields("d", "vd", f6, f7, f8);
        Fields g2 = TestUtil.createFields("group", "g2", fs2);
        list.add(g2);

        f1 = TestUtil.createField("e", "ve");
        f2 = TestUtil.createField("e", "veee");
        fs1 = TestUtil.createFields("ee", "vee", f2);
        list.add(f1);
        list.add(fs1);

        // System.out.println(list);
        return list;
    }
}
