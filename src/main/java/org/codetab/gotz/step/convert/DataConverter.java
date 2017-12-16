package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
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
     * to trace changes
     */
    private Map<String, String> convertedValues = new HashMap<>();

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

        LOGGER.info(getLabeled("apply converters"));

        List<Fields> converters = new ArrayList<>();
        try {
            converters = fieldsHelper.split(
                    Util.join("/xf:fields/xf:task/xf:steps/xf:step[@name='",
                            getStepType(), "']/xf:converter"),
                    getFields());
        } catch (FieldsException e) {
        }

        // convert data
        for (Member member : getData().getMembers()) {

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            try {
                col = convert(AxisName.COL, col, converters);
                row = convert(AxisName.ROW, row, converters);
                fact = convert(AxisName.FACT, fact, converters);
            } catch (Exception e) {
                throw new StepRunException("unable to apply converter", e);
            }

            member.setValue(AxisName.COL, col);
            member.setValue(AxisName.ROW, row);
            member.setValue(AxisName.FACT, fact);
        }
        setConvertedData(getData());
        traceChanges();
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    private void traceChanges() {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        LOGGER.trace(getMarker(), "summary of values converted");
        for (String key : convertedValues.keySet()) {
            LOGGER.trace(getMarker(), "  {} -> {}", key,
                    convertedValues.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    private String convert(final AxisName axis, final String value,
            final List<Fields> converters) throws Exception {
        String orgValue = value;
        String rvalue = value;
        for (Fields fields : converters) {
            try {
                String axisName = fieldsHelper.getLastValue(
                        "/xf:fields/xf:converter/xf:axis", fields);
                if (axis.name().equalsIgnoreCase(axisName)) {
                    String className = fieldsHelper.getLastValue(
                            "/xf:fields/xf:converter/@class", fields);

                    try {
                        @SuppressWarnings("rawtypes")
                        IConverter converter = (IConverter) stepService
                                .createInstance(className);
                        converter.setFields(fields);
                        rvalue = (String) converter.convert(rvalue);
                    } catch (Exception e) {
                        String message = Util.join(e.getMessage(), " axis [",
                                axisName, "], value [", value, "], converter [",
                                className, "]");
                        throw new Exception(message, e);
                    }

                }
            } catch (FieldsNotFoundException e) {
            }
        }
        if (LOGGER.isTraceEnabled()) {
            if (!orgValue.equals(rvalue)) {
                convertedValues.put(orgValue, rvalue);
            }
        }
        return rvalue;
    }
}
