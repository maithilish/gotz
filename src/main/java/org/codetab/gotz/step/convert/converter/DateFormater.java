package org.codetab.gotz.step.convert.converter;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.messages.Messages;
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
     * @throws FieldsNotFoundException
     * @throws org.codetab.gotz.exception.FieldsException
     *             if pattern field is not found
     * @throws ParseException
     *             if parse error
     */
    @Override
    public String convert(final String input)
            throws FieldsNotFoundException, ParseException {
        Validate.notNull(input, Messages.getString("DateFormater.0")); //$NON-NLS-1$
        Validate.validState(fields != null,
                Messages.getString("DateFormater.1")); //$NON-NLS-1$

        String patternIn = fieldsHelper
                .getLastValue("/xf:fields/xf:converter/xf:inPattern", fields); //$NON-NLS-1$
        String patternOut = fieldsHelper
                .getLastValue("/xf:fields/xf:converter/xf:outPattern", fields); //$NON-NLS-1$

        Date date = DateUtils.parseDate(input, patternIn);
        return DateFormatUtils.format(date, patternOut);
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

}
