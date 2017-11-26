package org.codetab.gotz.step.load.encoder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvEncoder implements IEncoder<List<String>> {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(CsvEncoder.class);

    private Fields fields;

    @Inject
    private EncoderHelper encoderHelper;
    @Inject
    private FieldsHelper fieldsHelper;
    @Inject
    private ActivityService activityService;

    @Override
    public List<String> encode(final Data data) throws Exception {
        Validate.validState(fields != null, "fields must not be null");
        Validate.validState(data != null, "data must not be null");

        String locatorName = null;
        String locatorGroup = null;
        try {
            locatorName =
                    fieldsHelper.getLastValue("/:fields/:locatorName", fields);
            locatorGroup =
                    fieldsHelper.getLastValue("/:fields/:locatorGroup", fields);
        } catch (FieldsException e) {
            String message = "unable to get locator name and group";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
        }

        encoderHelper.sort(data, fields);

        List<String> encodedData = new ArrayList<>();

        String delimiter = encoderHelper.getDelimiter(fields);
        // encode and append data
        for (Member member : data.getMembers()) {
            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            StringBuilder sb = new StringBuilder();
            sb.append(locatorName);
            sb.append(delimiter);
            sb.append(locatorGroup);
            sb.append(delimiter);
            sb.append(col);
            sb.append(delimiter);
            sb.append(row);
            sb.append(delimiter);
            sb.append(fact);

            encodedData.add(sb.toString());
        }
        return encodedData;
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

}
