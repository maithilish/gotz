package org.codetab.gotz.step.convert;

import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseEncoder;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Encodes data as comma separated values.
 * @author Maithilish
 *
 */
public final class CsvEncoder extends BaseEncoder {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(CsvEncoder.class);

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
            locatorName = fieldsHelper.getLastValue("/:fields/:locatorName",
                    getFields());
            locatorGroup = fieldsHelper.getLastValue("/:fields/:locatorGroup",
                    getFields());
        } catch (FieldsException e) {
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

        // encode and append data
        for (Member member : getData().getMembers()) {
            StringBuilder builder = new StringBuilder();

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            builder.append(locatorName);
            builder.append(" |");
            builder.append(locatorGroup);
            builder.append(" |");
            builder.append(col);
            builder.append(" |");
            builder.append(row);
            builder.append(" |");
            builder.append(fact);

            doAppend(builder.toString());
        }
        setStepState(StepState.PROCESS);
        return true;
    }
}
