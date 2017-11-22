package org.codetab.gotz.converter;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;

/**
 * <p>
 * String to Date converter.
 * @author Maithilish
 *
 */
public class DateFormater implements IConverter<String, String> {

    /**
     * fields.
     */
    private Fields fields;

    @Inject
    private FieldsHelper fieldsHelper;

    /**
     * Convert input string to date and round it off to month end.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws FieldsException
     *             if pattern field is not found
     * @throws ParseException
     *             if parse error
     */
    @Override
    public String convert(final String input)
            throws FieldsException, ParseException {
        Validate.notNull(input, "input date string must not be null");
        Validate.validState(fields != null, "fields is null");

        String patternIn = fieldsHelper.getLastValue("//:inPattern", fields);
        String patternOut = fieldsHelper.getLastValue("//:outPattern", fields);

        Date date = DateUtils.parseDate(input, patternIn);
        return DateFormatUtils.format(date, patternOut);
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

}
