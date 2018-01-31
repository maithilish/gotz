package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
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

        Validate.validState(getFields() != null,
                Messages.getString("DataConverter.0")); //$NON-NLS-1$
        Validate.validState(getData() != null,
                Messages.getString("DataConverter.1")); //$NON-NLS-1$

        LOGGER.info(getLabeled(Messages.getString("DataConverter.2"))); //$NON-NLS-1$

        List<Fields> converters = new ArrayList<>();
        try {
            String xpath =
                    Util.join("/xf:fields/xf:task/xf:steps/xf:step[@name='", //$NON-NLS-1$
                            getStepType(), "']/xf:converter");
            converters = fieldsHelper.split(xpath, getFields());
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
                throw new StepRunException(
                        Messages.getString("DataConverter.5"), e); //$NON-NLS-1$
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
        LOGGER.trace(getMarker(), Messages.getString("DataConverter.6")); //$NON-NLS-1$
        for (String key : convertedValues.keySet()) {
            LOGGER.trace(getMarker(), "  {} -> {}", key, //$NON-NLS-1$
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
                        "/xf:fields/xf:converter/xf:axis", fields); //$NON-NLS-1$
                if (axis.name().equalsIgnoreCase(axisName)) {
                    String className = fieldsHelper.getLastValue(
                            "/xf:fields/xf:converter/@class", fields); //$NON-NLS-1$

                    try {
                        @SuppressWarnings("rawtypes")
                        IConverter converter = (IConverter) stepService
                                .createInstance(className);
                        converter.setFields(fields);
                        rvalue = (String) converter.convert(rvalue);
                    } catch (Exception e) {
                        String message = Util.join(e.getMessage(),
                                Messages.getString("DataConverter.10"), //$NON-NLS-1$
                                axisName,
                                Messages.getString("DataConverter.11"), value, //$NON-NLS-1$
                                Messages.getString("DataConverter.12"), //$NON-NLS-1$
                                className, "]"); //$NON-NLS-1$
                        throw new Exception(message, e);
                    }

                }
            } catch (FieldsNotFoundException e) {
            }
        }
        if (LOGGER.isTraceEnabled()) {
            if (orgValue != null && !orgValue.equals(rvalue)) {
                convertedValues.put(orgValue, rvalue);
            }
        }
        return rvalue;
    }
}
