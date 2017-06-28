package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

public class DateAdapterTest {

    @Test
    public void testParseDate() throws ParseException {
        // given
        String xsdDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-ddZZ");
        Date expected = DateUtils.parseDate(xsdDateStr, "yyyy-MM-ddZZ");

        // when
        Date actual = DateAdapter.parseDate(xsdDateStr);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testParseDateNull() {
        // given
        String xsdDateStr = null;

        // when
        Date actual = DateAdapter.parseDate(xsdDateStr);

        // then
        assertThat(actual).isNull();
    }

    @Test
    public void testPrintDate() throws ParseException {
        String xsdDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-ddZZ");
        Date date = DateUtils.parseDate(xsdDateStr, "yyyy-MM-ddZZ");

        // when
        String actual = DateAdapter.printDate(date);

        // then
        assertThat(actual).isEqualTo(xsdDateStr);
    }

    @Test
    public void testPrintDateNull() throws ParseException {
        Date date = null;

        // when
        String actual = DateAdapter.printDate(date);

        // then
        assertThat(actual).isNull();
    }

}
