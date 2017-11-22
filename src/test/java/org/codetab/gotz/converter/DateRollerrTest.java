package org.codetab.gotz.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.text.ParseException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.FieldsHelper;
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
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "MMM ''YY", xField);
        xFieldHelper.addElement("outPattern", "YYYY-MM-dd", xField);
        xFieldHelper.addElement("roll", "DAY_OF_MONTH=ceil", xField);
        dt.setXField(xField);

        String actual = dt.convert("Mar '17");
        assertThat(actual).isEqualTo("2017-03-31");

        actual = dt.convert("Feb '16");
        assertThat(actual).isEqualTo("2016-02-29");

        actual = dt.convert("Feb '17");
        assertThat(actual).isEqualTo("2017-02-28");
    }

    @Test
    public void testConvertFloor() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "MMM ''YY", xField);
        xFieldHelper.addElement("outPattern", "YYYY-MM-dd", xField);
        xFieldHelper.addElement("roll", "DAY_OF_MONTH=floor", xField);
        dt.setXField(xField);

        String actual = dt.convert("Mar '17");
        assertThat(actual).isEqualTo("2017-03-01");

        actual = dt.convert("Feb '16");
        assertThat(actual).isEqualTo("2016-02-01");

        actual = dt.convert("Feb '17");
        assertThat(actual).isEqualTo("2017-02-01");
    }

    @Test
    public void testConvertRound() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "dd-MM-YYYY", xField);
        xFieldHelper.addElement("outPattern", "YYYY-MM-dd", xField);
        xFieldHelper.addElement("roll", "DAY_OF_MONTH=round", xField);
        dt.setXField(xField);

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
            assertThat(e.getMessage()).isEqualTo("xfield is null");
        }
    }

    @Test
    public void testConvertPatternInParseException() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "YY", xField);
        xFieldHelper.addElement("outPattern", "YYYY-MM-dd", xField);
        dt.setXField(xField);

        testRule.expect(ParseException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutParseException() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "MMM ''YY", xField);
        xFieldHelper.addElement("outPattern", "x", xField);
        xFieldHelper.addElement("roll", "DAY_OF_MONTH=ceil", xField);
        dt.setXField(xField);

        testRule.expect(IllegalArgumentException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternInNotFound() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("x", "MMM ''YY", xField);
        xFieldHelper.addElement("outPattern", "YYYY-MM-dd", xField);
        dt.setXField(xField);

        testRule.expect(FieldsException.class);
        dt.convert("Mar '17");
    }

    @Test
    public void testConvertPatternOutNotFound() throws Exception {
        FieldsHelper xFieldHelper = new FieldsHelper();
        XField xField = xFieldHelper.createXField();
        xFieldHelper.addElement("inPattern", "MMM ''YY", xField);
        xFieldHelper.addElement("x", "YYYY-MM-dd", xField);
        dt.setXField(xField);

        testRule.expect(FieldsException.class);
        dt.convert("Mar '17");
    }
}
