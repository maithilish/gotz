package in.m.picks.validation;

import java.util.ArrayList;
import java.util.List;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.DAxis;
import in.m.picks.model.DFilter;
import in.m.picks.model.DMember;
import in.m.picks.model.DataDef;
import in.m.picks.model.FieldsBase;
import in.m.picks.util.FieldsUtil;

public class DataDefValidator {

	private DataDef dataDef;

	public void setDataDef(DataDef dataDef) {
		this.dataDef = dataDef;
	}

	public boolean validate() {
		boolean valid = true;
		valid = validateIndexRange();
		return valid;
	}

	private boolean validateIndexRange() {
		boolean valid = true;
		for (List<FieldsBase> fc : getAllFields()) {
			try {
				FieldsUtil.getRange(fc, "indexRange");
			} catch (NumberFormatException e) {
				valid = false;
			} catch (FieldNotFoundException e) {
			}
		}
		return valid;
	}

	private List<List<FieldsBase>> getAllFields() {
		List<List<FieldsBase>> lists = new ArrayList<>();

		lists.add(dataDef.getFields());
		for (DAxis axis : dataDef.getAxis()) {
			lists.add(axis.getFields());
			for (DMember member : axis.getMember()) {
				if (member != null) {
					lists.add(member.getFields());
				}
			}
			DFilter filter = axis.getFilter();
			if (filter != null) {
				lists.add(filter.getFields());
			}
		}
		return lists;
	}
}
