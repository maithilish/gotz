package org.codetab.gotz.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.text.ParseException;
import java.util.List;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * DateConverter tests.
 * @author Maithilish
 *
 */
public class DateRollerrTest {

    private IConverter<String, String> dt;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dt = new DateRoller();
    }

    @Test
    public void testConvertCeil() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "MMM ''YY");
        FieldsBase patternOut =
                TestUtil.createField("outpattern", "YYYY-MM-dd");
        FieldsBase calField = TestUtil.createField("roll", "DAY_OF_MONTH=ceil");

        List<FieldsBase> fields =
                TestUtil.asList(patternIn, patternOut, calField);
        dt.setFields(fields);

        String actual = dt.convert("Mar '17");
        assertThat(actual).isEqualTo("2017-03-31");

        actual = dt.convert("Feb '16");
        assertThat(actual).isEqualTo("2016-02-29");

        actual = dt.convert("Feb '17");
        assertThat(actual).isEqualTo("2017-02-28");
    }

    @Test
    public void testConvertFloor() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "MMM ''YY");
        FieldsBase patternOut =
                TestUtil.createField("outpattern", "YYYY-MM-dd");
        FieldsBase calField =
                TestUtil.createField("roll", "DAY_OF_MONTH=floor");

        List<FieldsBase> fields =
                TestUtil.asList(patternIn, patternOut, calField);
        dt.setFields(fields);

        String actual = dt.convert("Mar '17");
        assertThat(actual).isEqualTo("2017-03-01");

        actual = dt.convert("Feb '16");
        assertThat(actual).isEqualTo("2016-02-01");

        actual = dt.convert("Feb '17");
        assertThat(actual).isEqualTo("2017-02-01");
    }

    @Test
    public void testConvertRound() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "dd-MM-YYYY");
        FieldsBase patternOut =
                TestUtil.createField("outpattern", "YYYY-MM-dd");
        FieldsBase calField =
                TestUtil.createField("roll", "DAY_OF_MONTH=round");

        List<FieldsBase> fields =
                TestUtil.asList(patternIn, patternOut, calField);
        dt.setFields(fields);

        String actual = dt.convert("16-03-2017");
        assertThat(actual).isEqualTo("2017-03-31");

        actual = dt.convert("15-03-2017");
        assertThat(actual).isEqualTo("2017-03-01");
    }

    @Test
    public void testConvertNullParams() throws Exception {
        try {
            dt.convert(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("input date string must not be null");
        }
    }

    @Test
    public void testConvertIllegalState() throws Exception {
        try {
            dt.convert("Mar 17");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("fields is null");
        }
    }

    @Test
    public void testConvertPatternInParseException() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "YY");
        FieldsBase patternOut =
                TestUtil.createField("outpattern", "YYYY-MM-dd");
        List<FieldsBase> fields = TestUtil.asList(patternIn, patternOut);
        dt.setFields(fields);

        testRule.expect(ParseException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutParseException() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "MMM ''YY");
        FieldsBase patternOut = TestUtil.createField("outpattern", "x");
        FieldsBase calField = TestUtil.createField("roll", "DAY_OF_MONTH=ceil");

        List<FieldsBase> fields =
                TestUtil.asList(patternIn, patternOut, calField);
        dt.setFields(fields);

        testRule.expect(IllegalArgumentException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternInNotFound() throws Exception {
        FieldsBase patternIn = TestUtil.createField("x", "MMM ''YY");
        FieldsBase patternOut =
                TestUtil.createField("outpattern", "YYYY-MM-dd");
        List<FieldsBase> fields = TestUtil.asList(patternIn, patternOut);
        dt.setFields(fields);

        testRule.expect(FieldNotFoundException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutNotFound() throws Exception {
        FieldsBase patternIn = TestUtil.createField("inpattern", "MMM ''YY");
        FieldsBase patternOut = TestUtil.createField("x", "YYYY-MM-dd");
        List<FieldsBase> fields = TestUtil.asList(patternIn, patternOut);
        dt.setFields(fields);

        testRule.expect(FieldNotFoundException.class);
        dt.convert("Mar '17");
    }
}
