package org.codetab.gotz.converter;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.util.FieldsUtil;

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
    private List<FieldsBase> fields;

    /**
     * Convert input string to date and round it off to month end.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws FieldNotFoundException
     *             if pattern field is not found
     * @throws ParseException
     *             if parse error
     */
    @Override
    public String convert(final String input)
            throws FieldNotFoundException, ParseException {
        Validate.notNull(input, "input date string must not be null");
        Validate.validState(fields != null, "fields is null");

        String patternIn = FieldsUtil.getValue(fields, "inpattern");
        String patternOut = FieldsUtil.getValue(fields, "outpattern");

        Date date = DateUtils.parseDate(input, patternIn);
        return DateFormatUtils.format(date, patternOut);
    }

    @Override
    public void setFields(final List<FieldsBase> fields) {
        this.fields = fields;
    }

}
