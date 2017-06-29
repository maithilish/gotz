package org.codetab.gotz.ext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.step.Encoder;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CsvRecordEncoder extends Encoder {

    static final Logger LOGGER =
            LoggerFactory.getLogger(CsvRecordEncoder.class);

    static final int ITEM_COL_SIZE = 30;
    static final int FACT_COL_SIZE = 10;
    static final String LINE_BREAK = Util.LINE;

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#instance()
     */
    @Override
    public IStep instance() {
        return this;
    }

    /*
     *
     */
    @Override
    public boolean process() {
        /*
         * don't append Marker.EOF here as other tasks may use same appender.
         * During shutdown, GTaskRunner.waitForFinish calls
         * AppenderService.closeAll which appends Marker.EOF for each appender.
         */
        StringBuilder builder = new StringBuilder();

        ColComparator cc = new ColComparator();
        Collections.sort(getData().getMembers(), cc);
        RowComparator rc = new RowComparator();
        Collections.sort(getData().getMembers(), rc);
        String prevRow = null;

        builder.append(getHeader());
        for (Member member : getData().getMembers()) {
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            if (prevRow == null) {
                builder.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                builder.append(" |");
            } else {
                if (!prevRow.equals(row)) {
                    builder.append(LINE_BREAK);
                    builder.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                    builder.append(" |");
                } else {
                    builder.append(" |");
                }
            }
            builder.append(StringUtils.leftPad(fact, FACT_COL_SIZE));
            prevRow = row;
        }
        builder.append(LINE_BREAK);
        doAppend(builder.toString());
        setStepState(StepState.PROCESS);
        return true;
    }

    private int getColCount() {
        Set<String> cols = new HashSet<String>();
        for (Member member : getData().getMembers()) {
            cols.add(member.getValue(AxisName.COL));
        }
        return cols.size();
    }

    private String getHeader() {
        String header = StringUtils.rightPad("item", ITEM_COL_SIZE);
        int colCount = getColCount();
        for (int c = 0; c < colCount; c++) {
            header += " |";
            String col = getData().getMembers().get(c).getValue(AxisName.COL);
            header += StringUtils.leftPad(col, FACT_COL_SIZE);
        }
        header += LINE_BREAK;
        return header;
    }

    @Override
    public boolean handover() {
        return false;
    }
}
