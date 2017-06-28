package org.codetab.gotz.util;

import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DataDef;

public final class DataDefUtil {

    private DataDefUtil() {
    }

    public static DAxis getAxis(final DataDef dataDef,
            final AxisName axisName) {
        String axisNameStr = axisName.toString();
        for (DAxis dAxis : dataDef.getAxis()) {
            if (dAxis.getName().equalsIgnoreCase(axisNameStr)) {
                return dAxis;
            }
        }
        return null;
    }
}
