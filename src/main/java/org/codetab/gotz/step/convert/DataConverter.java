package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseConverter;
import org.codetab.gotz.step.convert.converter.IConverter;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Apply converters to Data.
 * @author Maithilish
 *
 */
public final class DataConverter extends BaseConverter {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DataConverter.class);

    /**
     * Returns this step.
     * @return IStep
     */
    @Override
    public IStep instance() {
        return this;
    }

    /**
     * Get list of converters defined and apply it to applicable axis of Data.
     * @return true when no error
     */
    @Override
    public boolean process() {
        /*
         * don't append Marker.EOF here as other tasks may use same appender.
         * During shutdown, GTaskRunner.waitForFinish calls
         * AppenderService.closeAll which appends Marker.EOF for each appender.
         */

        Validate.validState(getFields() != null, "fields must not be null");
        Validate.validState(getData() != null, "data must not be null");

        List<Fields> converters = new ArrayList<>();
        try {
            converters = fieldsHelper.split(Util.buildString(
                    "/xf:fields/xf:task/xf:steps/xf:step[@name='",
                    getStepType(), "']/xf:converter"), getFields());
        } catch (FieldsException e) {
        }

        // convert data
        for (Member member : getData().getMembers()) {

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            col = convert(AxisName.COL, col, converters);
            row = convert(AxisName.ROW, row, converters);
            fact = convert(AxisName.FACT, fact, converters);

            member.setValue(AxisName.COL, col);
            member.setValue(AxisName.ROW, row);
            member.setValue(AxisName.FACT, fact);
        }
        setConvertedData(getData());
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    @SuppressWarnings("unchecked")
    private String convert(final AxisName axis, final String value,
            final List<Fields> converters) {
        String rvalue = value;
        for (Fields fields : converters) {
            try {
                String axisName = fieldsHelper
                        .getLastValue("//xf:converter/xf:axis", fields);
                if (axis.name().equalsIgnoreCase(axisName)) {
                    String className = fieldsHelper
                            .getLastValue("//xf:converter/@class", fields);
                    try {
                        @SuppressWarnings("rawtypes")
                        IConverter converter = (IConverter) stepService
                                .createInstance(className);
                        converter.setFields(fields);
                        rvalue = (String) converter.convert(rvalue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (FieldsNotFoundException e) {
            }
        }
        return rvalue;
    }
}
