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
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetEncoder implements IEncoder<List<DataSet>> {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DataSetEncoder.class);

    private Fields fields;

    @Inject
    private EncoderHelper encoderHelper;
    @Inject
    private FieldsHelper fieldsHelper;
    @Inject
    private ActivityService activityService;

    @Override
    public List<DataSet> encode(final Data data) throws Exception {
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

        List<DataSet> encodedData = new ArrayList<>();

        // encode Data to DataSet
        for (Member member : data.getMembers()) {

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            DataSet dataSet =
                    new DataSet(locatorName, locatorGroup, col, row, fact);
            encodedData.add(dataSet);
        }
        return encodedData;
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

}
