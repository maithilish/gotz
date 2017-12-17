package org.codetab.gotz.step.load.encoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvRecordEncoder implements IEncoder<List<String>> {

    /**
     * logger.
     */
    static final Logger LOGGER =
            LoggerFactory.getLogger(CsvRecordEncoder.class);

    /**
     * col padding.
     */
    static final int ITEM_COL_SIZE = 30;

    /**
     * fact padding.
     */
    static final int FACT_COL_SIZE = 10;

    private Fields fields;
    private Labels labels;

    @Inject
    private EncoderHelper encoderHelper;

    @Override
    public List<String> encode(final Data data) throws Exception {
        Validate.validState(fields != null,
                Messages.getString("CsvRecordEncoder.0")); //$NON-NLS-1$
        Validate.validState(data != null,
                Messages.getString("CsvRecordEncoder.1")); //$NON-NLS-1$

        encoderHelper.sort(data, fields);

        // add header
        List<String> encodedData = new ArrayList<>();
        String delimiter = encoderHelper.getDelimiter(fields);
        encodedData.add(getLocatorInfo(delimiter));
        encodedData.add(getColumnHeader(data, delimiter));

        // TODO check is it possible to encode when no sort
        // encode data
        StringBuilder sb = new StringBuilder();
        String prevRow = null;

        for (Member member : data.getMembers()) {
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            if (prevRow == null) {
                sb.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                sb.append(" |"); //$NON-NLS-1$
            } else {
                // row break
                if (!prevRow.equals(row)) {
                    encodedData.add(sb.toString()); // output row
                    sb = new StringBuilder();
                    sb.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                    sb.append(delimiter);
                } else {
                    sb.append(delimiter);
                }
            }
            sb.append(StringUtils.leftPad(fact, FACT_COL_SIZE));
            prevRow = row;
        }
        return encodedData;
    }

    /**
     * <p>
     * Get locator name and group.
     *
     */
    private String getLocatorInfo(final String delimiter) {
        return String.join(delimiter, labels.getName(), labels.getGroup());
    }

    /**
     * <p>
     * Get column header.
     *
     */
    private String getColumnHeader(final Data data, final String delimiter) {
        String header = StringUtils.rightPad(
                Messages.getString("CsvRecordEncoder.2"), ITEM_COL_SIZE); //$NON-NLS-1$
        int colCount = getColCount(data);
        for (int c = 0; c < colCount; c++) {
            header += delimiter;
            String col = data.getMembers().get(c).getValue(AxisName.COL);
            header += StringUtils.leftPad(col, FACT_COL_SIZE);
        }
        return header;
    }

    /**
     * <p>
     * Get column count.
     * @return column count.
     */
    private int getColCount(final Data data) {
        Set<String> cols = new HashSet<String>();
        for (Member member : data.getMembers()) {
            cols.add(member.getValue(AxisName.COL));
        }
        return cols.size();
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

    @Override
    public void setLabels(final Labels labels) {
        this.labels = labels;
    }

}
