package org.codetab.gotz.step.convert.converter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
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
    private Fields fields;

    @Inject
    private FieldsHelper fieldsHelper;

    /**
     * <p>
     * Convert input string to date and set a field to its maximum.
     * <p>
     * Fields should have following fields : inPattern - date pattern to parse
     * the input, outPattern - date pattern to format the returned date and
     * field - Calendar field which has to set to its maximum.
     * <p>
     * Date pattern is java date pattern as defined by
     * {@link java.text.SimpleDateFormat}.
     * <p>
     * example : if field is DAY_OF_MONTH then date is set as month end date.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws FieldsNotFoundException
     * @throws org.codetab.gotz.exception.FieldsException
     *             if pattern field is not found
     * @throws ParseException
     *             if parse error
     * @throws IllegalAccessException
     *             if no such Calendar field
     * @see java.text.SimpleDateFormat
     * @see Calendar
     */
    @Override
    public String convert(final String input) throws FieldsNotFoundException,
            ParseException, IllegalAccessException {
        Validate.notNull(input, Messages.getString("DateRoller.0")); //$NON-NLS-1$
        Validate.validState(fields != null, Messages.getString("DateRoller.1")); //$NON-NLS-1$

        String patternIn = fieldsHelper
                .getLastValue("/xf:fields/xf:converter/xf:inPattern", fields); //$NON-NLS-1$
        String patternOut = fieldsHelper
                .getLastValue("/xf:fields/xf:converter/xf:outPattern", fields); //$NON-NLS-1$

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(DateUtils.parseDate(input, patternIn));

        // get map of calendar fields to roll
        String rollStr = fieldsHelper
                .getLastValue("/xf:fields/xf:converter/xf:roll", fields); //$NON-NLS-1$
        Map<String, String> rollMap = Util.split(rollStr, "=", " "); //$NON-NLS-1$ //$NON-NLS-2$

        // roll calendar fields
        for (String key : rollMap.keySet()) {

            int calField = (int) FieldUtils
                    .readDeclaredStaticField(Calendar.class, key);
            String value = rollMap.get(key).toLowerCase();

            switch (value) {
            case "ceil": //$NON-NLS-1$
                cal.set(calField, cal.getActualMaximum(calField));
                break;
            case "floor": //$NON-NLS-1$
                cal.set(calField, cal.getActualMinimum(calField));
                break;
            case "round": //$NON-NLS-1$
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
    public void setFields(final Fields fields) {
        this.fields = fields;
    }
}
