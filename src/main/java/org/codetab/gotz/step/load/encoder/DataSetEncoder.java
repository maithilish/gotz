package org.codetab.gotz.step.load.encoder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetEncoder implements IEncoder<List<DataSet>> {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DataSetEncoder.class);

    private Fields fields;
    private Labels labels;

    @Inject
    private EncoderHelper encoderHelper;

    @Override
    public List<DataSet> encode(final Data data) throws Exception {
        Validate.validState(fields != null, "fields must not be null");
        Validate.validState(data != null, "data must not be null");

        encoderHelper.sort(data, fields);

        List<DataSet> encodedData = new ArrayList<>();

        // encode Data to DataSet
        for (Member member : data.getMembers()) {

            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            DataSet dataSet = new DataSet(labels.getName(), labels.getGroup(),
                    col, row, fact);
            encodedData.add(dataSet);
        }
        return encodedData;
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
