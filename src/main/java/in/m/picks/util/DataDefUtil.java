package in.m.picks.util;

import in.m.picks.model.DAxis;
import in.m.picks.model.DataDef;

public class DataDefUtil {

	public static DAxis getAxis(DataDef dataDef, String axisName) {
		for (DAxis axis : dataDef.getAxis()) {
			if (axis.getName().equals(axisName)) {
				return axis;
			}
		}
		return null;
	}
}
