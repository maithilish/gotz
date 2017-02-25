package in.m.picks.util;

import in.m.picks.model.AxisName;
import in.m.picks.model.DAxis;
import in.m.picks.model.DataDef;

public class DataDefUtil {

	public static DAxis getAxis(DataDef dataDef, AxisName axisName) {
		String axisNameStr = axisName.toString();
		for (DAxis dAxis : dataDef.getAxis()) {
			if (dAxis.getName().equalsIgnoreCase(axisNameStr)) {
				return dAxis;
			}
		}
		return null;
	}
}
