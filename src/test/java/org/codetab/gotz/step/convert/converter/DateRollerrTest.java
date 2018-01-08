package org.codetab.gotz.step.convert.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.testutil.FieldsBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
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

    private static DInjector dInjector;

    private IConverter<String, String> dt;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        dInjector = new DInjector();
    }

    @Before
    public void setUp() throws Exception {
        dt = dInjector.instance(DateRoller.class);
    }

    @Test
    public void testConvertCeil() throws Exception {

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>MMM ''YY</xf:inPattern>")
                .add("    <xf:outPattern>yyyy-MM-dd</xf:outPattern>")
                .add("    <xf:roll>DATE=ceil</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
        dt.setFields(fields);

        String actual = dt.convert("Dec '17");
        assertThat(actual).isEqualTo("2017-12-31");

        actual = dt.convert("Mar '17");
        assertThat(actual).isEqualTo("2017-03-31");

        actual = dt.convert("Feb '16");
        assertThat(actual).isEqualTo("2016-02-29");

        actual = dt.convert("Feb '17");
        assertThat(actual).isEqualTo("2017-02-28");

        actual = dt.convert("Dec '16");
        assertThat(actual).isEqualTo("2016-12-31");

        actual = dt.convert("Dec '12");
        assertThat(actual).isEqualTo("2012-12-31");
    }

    @Test
    public void testConvertFloor() throws Exception {

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>MMM ''YY</xf:inPattern>")
                .add("    <xf:outPattern>YYYY-MM-dd</xf:outPattern>")
                .add("    <xf:roll>DAY_OF_MONTH=floor</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
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

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>dd-MM-YYYY</xf:inPattern>")
                .add("    <xf:outPattern>YYYY-MM-dd</xf:outPattern>")
                .add("    <xf:roll>DAY_OF_MONTH=round</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
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

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>MMM ''YY</xf:inPattern>")
                .add("    <xf:outPattern>YYYY-MM-dd</xf:outPattern>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
        dt.setFields(fields);

        testRule.expect(FieldsNotFoundException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutParseException() throws Exception {

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>MMM ''YY</xf:inPattern>")
                .add("    <xf:outPattern>x</xf:outPattern>")
                .add("    <xf:roll>DAY_OF_MONTH=ceil</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
        dt.setFields(fields);

        testRule.expect(IllegalArgumentException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternInNotFound() throws Exception {
        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:x>MMM ''YY</xf:x>")
                .add("    <xf:outPattern>YYYY-MM-dd</xf:outPattern>")
                .add("    <xf:roll>DAY_OF_MONTH=ceil</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
        dt.setFields(fields);

        testRule.expect(FieldsNotFoundException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutNotFound() throws Exception {

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add("  <xf:converter name='date'>")
                .add("    <xf:axis>col</xf:axis>")
                .add("    <xf:inPattern>MMM ''YY</xf:inPattern>")
                .add("    <xf:x>YYYY-MM-dd</xf:x>")
                .add("    <xf:roll>DAY_OF_MONTH=ceil</xf:roll>")
                .add("  </xf:converter>")
                .build("xf");
        //@formatter:on
        dt.setFields(fields);

        testRule.expect(FieldsNotFoundException.class);
        dt.convert("Mar '17");
    }
}
