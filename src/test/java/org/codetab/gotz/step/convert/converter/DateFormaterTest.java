package org.codetab.gotz.step.convert.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DateFormaterTest {

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
        dt = dInjector.instance(DateFormater.class);
    }

    @Test
    public void testConvert() throws Exception {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("  <xf:converter name='date'>")
          .add("    <xf:axis>col</xf:axis>")
          .add("    <xf:inPattern>dd-MM-yyyy</xf:inPattern>")
          .add("    <xf:outPattern>yyyy-MM-dd</xf:outPattern>")
          .add("  </xf:converter>")
          .buildFields();
        //@formatter:on

        dt.setFields(fields);

        String actual = dt.convert("10-12-2017");
        assertThat(actual).isEqualTo("2017-12-10");
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
            dt.convert("10-12-2017");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("fields is null");
        }
    }

    @Test
    public void testConvertPatternInParseException() throws Exception {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("  <xf:converter name='date'>")
          .add("    <xf:axis>col</xf:axis>")
          .add("    <xf:inPattern>x</xf:inPattern>")
          .add("    <xf:outPattern>yyyy-MM-dd</xf:outPattern>")
          .add("  </xf:converter>")
          .buildFields();
        //@formatter:on

        dt.setFields(fields);

        testRule.expect(IllegalArgumentException.class);
        dt.convert("10-12-2017");
    }

    @Test
    public void testConvertPatternOutParseException() throws Exception {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("  <xf:converter name='date'>")
          .add("    <xf:axis>col</xf:axis>")
          .add("    <xf:inPattern>dd-MM-yyyy</xf:inPattern>")
          .add("    <xf:outPattern>x</xf:outPattern>")
          .add("  </xf:converter>")
          .buildFields();
        //@formatter:on

        dt.setFields(fields);

        testRule.expect(IllegalArgumentException.class);
        dt.convert("10-12-2017");
    }

    @Test
    public void testConvertPatternInNotFound() throws Exception {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("  <xf:converter name='date'>")
          .add("    <xf:axis>col</xf:axis>")
          .add("    <xf:x>dd-MM-yyyy</xf:x>")
          .add("    <xf:outPattern>yyyy-MM-dd</xf:outPattern>")
          .add("    <xf:roll>DATE=floor</xf:roll>")
          .add("  </xf:converter>")
          .buildFields();
        //@formatter:on

        dt.setFields(fields);

        testRule.expect(FieldsNotFoundException.class);
        dt.convert("10-12-2017");
    }

    @Test
    public void testConvertPatternOutNotFound() throws Exception {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("  <xf:converter name='date'>")
          .add("    <xf:axis>col</xf:axis>")
          .add("    <xf:inPattern>dd-MM-yyyy</xf:inPattern>")
          .add("    <xf:x>yyyy-MM-dd</xf:x>")
          .add("    <xf:roll>DATE=floor</xf:roll>")
          .add("  </xf:converter>")
          .buildFields();
        //@formatter:on

        dt.setFields(fields);

        testRule.expect(FieldsNotFoundException.class);
        dt.convert("10-12-2017");
    }
}
