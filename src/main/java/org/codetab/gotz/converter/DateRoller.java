package org.codetab.gotz.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;
import org.codetab.gotz.util.Util;

/**
 * <p>
 * String to Date converter.
 * @author Maithilish
 *
 */
public class DateRoller implements IConverter<String, String> {

    /**
     * fields.
     */
    private XField xField;

    @Inject
    private XFieldHelper xFieldHelper;

    /**
     * <p>
     * Convert input string to date and set a field to its maximum.
     * <p>
     * Fields should have following fields : inPattern - date pattern to parse
     * the input, outPattern - date pattern to format the returned date and
     * field - Calendar field which has to set to its maximum.
     * <p>
     * Date pattern is java date pattern as defined by {@link SimpleDateFormat}.
     * <p>
     * example : if field is DAY_OF_MONTH then date is set as month end date.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws XFieldException
     *             if pattern field is not found
     * @throws ParseException
     *             if parse error
     * @throws IllegalAccessException
     *             if no such Calendar field
     * @see SimpleDateFormat
     * @see Calendar
     */
    @Override
    public String convert(final String input)
            throws XFieldException, ParseException, IllegalAccessException {
        Validate.notNull(input, "input date string must not be null");
        Validate.validState(xField != null, "xfield is null");

        String patternIn = xFieldHelper.getLastValue("//:inPattern", xField);
        String patternOut = xFieldHelper.getLastValue("//:outPattern", xField);

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(DateUtils.parseDate(input, patternIn));

        // get map of calendar fields to roll
        String rollStr = xFieldHelper.getLastValue("//:roll", xField);
        Map<String, String> rollMap = Util.split(rollStr, "=", " ");

        // roll calendar fields
        for (String key : rollMap.keySet()) {

            int calField = (int) FieldUtils
                    .readDeclaredStaticField(Calendar.class, key);
            String value = rollMap.get(key).toLowerCase();

            switch (value) {
            case "ceil":
                cal.set(calField, cal.getActualMaximum(calField));
                break;
            case "floor":
                cal.set(calField, cal.getActualMinimum(calField));
                break;
            case "round":
                int max = cal.getActualMaximum(calField);
                int mid = max / 2;
                if (cal.get(calField) <= mid) {
                    cal.set(calField, cal.getActualMinimum(calField));
                } else {
                    cal.set(calField, max);
                }
                break;
            default:
                Integer amount = Integer.parseInt(value);
                cal.set(calField, amount);
                break;
            }
        }

        return DateFormatUtils.format(cal.getTime(), patternOut);
    }

    @Override
    public void setXField(final XField xField) {
        this.xField = xField;
    }
}
