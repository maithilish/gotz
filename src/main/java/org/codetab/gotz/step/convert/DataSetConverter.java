package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.converter.IConverter;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseDataConverter;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Converts Data to list of DataSet and apply other defined conversion rules.
 * @author Maithilish
 *
 */
public final class DataSetConverter extends BaseDataConverter {

    /**
     * logger.
     */
    static final Logger LOGGER =
            LoggerFactory.getLogger(DataSetConverter.class);

    /**
     * Returns this step.
     * @return IStep
     */
    @Override
    public IStep instance() {
        return this;
    }

    /**
     * Process data and append it to appenders. Sorts data using
     * {@see ColComparator} and {@see RowComparator}. For each member in data,
     * locator name, locator group, COL,ROW and FACT axis values are appended
     * delimiting with "|"
     * @return true when no error
     * @throws StepRunException
     *             when unable to get locator name and group.
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

        String locatorName = null;
        String locatorGroup = null;
        try {
            locatorName = FieldsUtil.getValue(getFields(), "locatorName");
            locatorGroup = FieldsUtil.getValue(getFields(), "locatorGroup");
        } catch (FieldNotFoundException e) {
            String message = "unable to get locator name and group";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
        }

        // sort
        ColComparator cc = new ColComparator();
        Collections.sort(getData().getMembers(), cc);
        RowComparator rc = new RowComparator();
        Collections.sort(getData().getMembers(), rc);

        List<DataSet> dataSets = new ArrayList<>();

        List<FieldsBase> converters = new ArrayList<>();
        try {
            converters = FieldsUtil.filterByName(getFields(), "converter");
        } catch (FieldNotFoundException e) {
        }

        // encode and append data
        for (Member member : getData().getMembers()) {

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            col = convert(AxisName.COL, col, converters);
            row = convert(AxisName.ROW, row, converters);
            fact = convert(AxisName.FACT, fact, converters);

            DataSet dataSet =
                    new DataSet(locatorName, locatorGroup, col, row, fact);
            dataSets.add(dataSet);
        }
        setConvertedData(dataSets);
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    @SuppressWarnings("unchecked")
    private String convert(final AxisName axis, final String value,
            final List<FieldsBase> converters) {
        String rvalue = value;
        for (FieldsBase c : converters) {
            Fields cf = (Fields) c;
            try {
                String axisName = FieldsUtil.getValue(cf.getFields(), "axis");
                if (axis.name().equalsIgnoreCase(axisName)) {
                    String className =
                            FieldsUtil.getValue(cf.getFields(), "class");
                    try {
                        @SuppressWarnings("rawtypes")
                        IConverter converter = (IConverter) Class
                                .forName(className).newInstance();
                        converter.setFields(cf.getFields());
                        rvalue = (String) converter.convert(rvalue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (FieldNotFoundException e) {
            }
        }
        return rvalue;
    }
}
