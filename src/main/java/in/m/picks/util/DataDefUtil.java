package in.m.picks.util;

import in.m.picks.model.AxisName;
import in.m.picks.model.DAxis;
import in.m.picks.model.DataDef;

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
