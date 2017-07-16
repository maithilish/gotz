package org.codetab.gotz.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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

public class FieldsUtilTest {

    private List<FieldsBase> fields;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        fields = createTestObjects();
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
    public void testFilterByName() {

    }

    @Test
    public void testFilterByNameFromGroup() {

    }

    @Test
    public void testFilterChildrenByName() {

    }

    @Test
    public void testFilterByGroup() {

    }

    @Test
    public void testFilterByGroupAsFields() {

    }

    @Test
    public void testPrefixValue() {

    }

    @Test
    public void testReplaceVariables() {

    }

    @Test
    public void testContainsListOfFieldsBaseStringString() {

    }

    @Test
    public void testContainsFieldsStringString() {

    }

    @Test
    public void testIsTrueListOfFieldsBaseStringString() {

    }

    @Test
    public void testIsTrueListOfFieldsBaseString() {

    }

    @Test
    public void testIsDefined() {

    }

    @Test
    public void testIsAnyDefined() {

    }

    @Test
    public void testIsAllDefined() {

    }

    @Test
    public void testCountField() {

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

        Field f6 = TestUtil.createField("dd", "vdd");
        Field f7 = TestUtil.createField("ddd", "vddd");
        Field f8 = TestUtil.createField("dddd", "vdddd");
        Fields fs2 = TestUtil.createFields("d", "vd", f6, f7, f8);
        Fields g2 = TestUtil.createFields("group", "g2", fs2);
        list.add(g2);

        // System.out.println(list);
        return list;
    }
}
