package org.codetab.gotz.steps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.stepbase.BaseEncoder;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Encode to spreadsheet layout with pipe delimited.
 * @author Maithilish
 *
 */
public final class CsvRecordEncoder extends BaseEncoder {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CsvRecordEncoder.class);

    /**
     * col padding.
     */
    static final int ITEM_COL_SIZE = 30;

    /**
     * fact padding.
     */
    static final int FACT_COL_SIZE = 10;

    /**
     * Returns this step.
     * @return IStep
     */
    @Override
    public IStep instance() {
        return this;
    }

    /**
     * <p>
     * Process data and append it to appenders. Sorts data using
     * {@see ColComparator} and {@see RowComparator}. It appends, Locator name
     * and group and then column header with distinct col values. Next, it
     * appends rows with fact for each column similar to spreadsheet layout.
     * Values are delimited with "|"
     *
     * <pre>
     *   name | group            -- locator info
     *   item   |c-1   |c-2      -- columns 1 and 2
     *   r-1    |f-1-1 |f-2-1    -- row 1
     *   r-2    |f-1-2 |f-2-2    -- row 2
     *   r-3    |f-1-3 |f-2-3    -- row 3
     * </pre>
     *
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

        StringBuilder builder = null;

        ColComparator cc = new ColComparator();
        Collections.sort(getData().getMembers(), cc);
        RowComparator rc = new RowComparator();
        Collections.sort(getData().getMembers(), rc);
        String prevRow = null;

        appendLocatorInfo();

        appendColumnHeader();

        // row line items
        builder = new StringBuilder();
        for (Member member : getData().getMembers()) {
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            if (prevRow == null) {
                builder.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                builder.append(" |");
            } else {
                // row break
                if (!prevRow.equals(row)) {
                    doAppend(builder.toString()); // output row
                    builder = new StringBuilder();
                    builder.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                    builder.append(" |");
                } else {
                    builder.append(" |");
                }
            }
            builder.append(StringUtils.leftPad(fact, FACT_COL_SIZE));
            prevRow = row;
        }
        doAppend(builder.toString()); // output last row
        setStepState(StepState.PROCESS);
        return true;
    }

    /**
     * <p>
     * Append locator name and group.
     *
     */
    private void appendLocatorInfo() {
        try {
            String locatorName =
                    FieldsUtil.getValue(getFields(), "locatorName");
            String locatorGroup =
                    FieldsUtil.getValue(getFields(), "locatorGroup");
            doAppend(locatorName + "|" + locatorGroup);
        } catch (FieldNotFoundException e) {
            String message = "unable to get locator name and group";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
        }
    }

    /**
     * <p>
     * Append column header.
     *
     */
    private void appendColumnHeader() {
        String header = StringUtils.rightPad("item", ITEM_COL_SIZE);
        int colCount = getColCount();
        for (int c = 0; c < colCount; c++) {
            header += " |";
            String col = getData().getMembers().get(c).getValue(AxisName.COL);
            header += StringUtils.leftPad(col, FACT_COL_SIZE);
        }
        doAppend(header);
    }

    /**
     * <p>
     * Get column count.
     * @return column count.
     */
    private int getColCount() {
        Set<String> cols = new HashSet<String>();
        for (Member member : getData().getMembers()) {
            cols.add(member.getValue(AxisName.COL));
        }
        return cols.size();
    }

}
