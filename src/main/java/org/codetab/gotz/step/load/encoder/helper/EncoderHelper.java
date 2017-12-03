package org.codetab.gotz.step.load.encoder.helper;

import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;

public class EncoderHelper {

    @Inject
    private FieldsHelper fieldsHelper;
    @Inject
    private ActivityService activityService;

    public void sort(final Data data, final Fields fields) {
        String sortOrder = "";
        String xpath = "/xf:fields/xf:encoder/xf:sortOrder";
        if (fieldsHelper.isDefined(xpath, true, fields)) {
            sortOrder = fieldsHelper.getValue(xpath, fields);
        } else {
            sortOrder = "col,row"; // default
        }

        if (StringUtils.isBlank(sortOrder)) {
            activityService.addActivity(Type.WARN,
                    "sortOrder is blank, data not sorted");
            return;
        }

        sortOrder = sortOrder.toUpperCase();
        for (String axisName : sortOrder.split(",")) {
            AxisName axis = AxisName.valueOf(axisName);
            Comparator<Member> comparator = null;
            switch (axis) {
            case COL:
                comparator = new ColComparator();
                break;
            case ROW:
                comparator = new RowComparator();
                break;
            default:
                break;
            }
            if (comparator != null) {
                Collections.sort(data.getMembers(), comparator);
            }
        }
    }

    public String getDelimiter(final Fields fields) {
        String delimiter = "|";
        try {
            delimiter = fieldsHelper
                    .getLastValue("/xf:fields/xf:encoder/xf:delimiter", fields);
        } catch (FieldsNotFoundException e) {
        }
        return delimiter;
    }

}
