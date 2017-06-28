package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

public class Adapter1Test {

    @Test
    public void testUnmarshalString() throws ParseException {
        Adapter1 adapter = new Adapter1();
        String dateStr = "2017-10-31";
        String[] parsePattern = { "YYYY-MM-dd" };
        Date expected = DateUtils.parseDate(dateStr, parsePattern);
        Date actual = adapter.unmarshal(dateStr);
        assertEquals(expected, actual);
    }

    @Test
    public void testMarshalDate() throws ParseException {
        Adapter1 adapter = new Adapter1();
        String[] parsePattern = { "YYYY-MM-dd" };
        Date date = DateUtils.parseDate("2017-03-31", parsePattern);
        String formatPattern = "YYYY-MM-ddZZ";
        String expected = DateFormatUtils.format(date, formatPattern);
        String actual = adapter.marshal(date);
        assertEquals(expected, actual);
    }

}
