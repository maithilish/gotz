package org.codetab.gotz.step.load.encoder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
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

    private Labels labels;

    @Override
    public List<String> encode(final Data data) throws Exception {
        Validate.validState(fields != null, "fields must not be null");
        Validate.validState(data != null, "data must not be null");

        encoderHelper.sort(data, fields);

        List<String> encodedData = new ArrayList<>();

        String delimiter = encoderHelper.getDelimiter(fields);
        // encode and append data
        for (Member member : data.getMembers()) {
            String col = member.getValue(AxisName.COL);
            String row = member.getValue(AxisName.ROW);
            String fact = member.getValue(AxisName.FACT);

            StringBuilder sb = new StringBuilder();
            sb.append(labels.getName());
            sb.append(delimiter);
            sb.append(labels.getGroup());
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

    @Override
    public void setLabels(final Labels labels) {
        this.labels = labels;
    }
}
