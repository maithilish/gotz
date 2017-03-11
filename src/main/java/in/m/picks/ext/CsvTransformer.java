package in.m.picks.ext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.appender.Appender;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.AxisName;
import in.m.picks.model.ColComparator;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.model.RowComparator;
import in.m.picks.shared.AppenderService;
import in.m.picks.step.IStep;
import in.m.picks.step.Transformer;
import in.m.picks.util.FieldsUtil;

public final class CsvTransformer extends Transformer {

    static final Logger LOGGER = LoggerFactory.getLogger(CsvTransformer.class);

    static final int ITEM_COL_SIZE = 30;
    static final int FACT_COL_SIZE = 10;
    static final String LINE_BREAK = System.getProperty("line.separator");

    private StringBuilder content;

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#instance()
     */
    @Override
    public IStep instance() {
        return new CsvTransformer();
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.Transformer#transform()
     */
    @Override
    protected void transform() {
        processStep();
    }

    /*
     *
     */
    public void processStep() {
        content = new StringBuilder();

        ColComparator cc = new ColComparator();
        Collections.sort(getData().getMembers(), cc);
        RowComparator rc = new RowComparator();
        Collections.sort(getData().getMembers(), rc);
        String prevRow = null;

        content.append(getHeader());
        for (Member member : getData().getMembers()) {
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            if (prevRow == null) {
                content.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                content.append(" |");
            } else {
                if (!prevRow.equals(row)) {
                    content.append(LINE_BREAK);
                    content.append(StringUtils.rightPad(row, ITEM_COL_SIZE));
                    content.append(" |");
                } else {
                    content.append(" |");
                }
            }
            content.append(StringUtils.leftPad(fact, FACT_COL_SIZE));
            prevRow = row;
        }
        content.append(LINE_BREAK);
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
    public void handover() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException, FieldNotFoundException {
        List<FieldsBase> appenders = FieldsUtil.getGroupFields(getFields(), "appender");
        for (FieldsBase f : appenders) {
            List<FieldsBase> fields = FieldsUtil.asList(f);
            AppenderService.INSTANCE.createAppender(fields);

            String appenderName = FieldsUtil.getValue(fields, "name");
            Appender appender = AppenderService.INSTANCE.getAppender(appenderName);

            appender.append(content);
        }
    }
}
