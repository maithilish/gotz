package org.codetab.gotz.ext;

import java.util.Collections;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.step.Encoder;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CsvEncoder extends Encoder {

    static final Logger LOGGER = LoggerFactory.getLogger(CsvEncoder.class);

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
         * don't append Marker.EOF here as other tasks may use same appender. During
         * shutdown, GTaskRunner.waitForFinish calls AppenderService.closeAll which
         * appends Marker.EOF for each appender.
         */
        String locatorName = null;
        String locatorGroup = null;
        try {
            locatorName = OFieldsUtil.getValue(getFields(), "locatorName");
            locatorGroup = OFieldsUtil.getValue(getFields(), "locatorGroup");
        } catch (FieldNotFoundException e) {
            String message = "unable to get locator name and group";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
        }

        ColComparator cc = new ColComparator();
        Collections.sort(getData().getMembers(), cc);
        RowComparator rc = new RowComparator();
        Collections.sort(getData().getMembers(), rc);

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

    @Override
    public boolean handover() {
        return false;
    }
}
