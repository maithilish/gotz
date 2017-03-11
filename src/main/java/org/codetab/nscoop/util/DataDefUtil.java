package org.codetab.nscoop.util;

import org.codetab.nscoop.model.AxisName;
import org.codetab.nscoop.model.DAxis;
import org.codetab.nscoop.model.DataDef;

public final class DataDefUtil {

    private DataDefUtil() {
    }

    public static DAxis getAxis(final DataDef dataDef, final AxisName axisName) {
        String axisNameStr = axisName.toString();
        for (DAxis dAxis : dataDef.getAxis()) {
            if (dAxis.getName().equalsIgnoreCase(axisNameStr)) {
                return dAxis;
            }
        }
        return null;
    }
}
