package org.codetab.gotz.ext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Transformer;
import org.codetab.gotz.util.FieldsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CsvTransformer extends Transformer {

    static final Logger LOGGER = LoggerFactory.getLogger(CsvTransformer.class);

    static final int ITEM_COL_SIZE = 30;
    static final int FACT_COL_SIZE = 10;
    static final String LINE_BREAK = System.getProperty("line.separator");

    private StringBuilder content;

    private AppenderService appenderService;

    @Inject
    public void setAppenderService(AppenderService appenderService) {
        this.appenderService = appenderService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#instance()
     */
    @Override
    public IStep instance() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.Transformer#transform()
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
            appenderService.createAppender(fields);

            String appenderName = FieldsUtil.getValue(fields, "name");
            Appender appender = appenderService.getAppender(appenderName);

            appender.append(content);
        }
    }
}
