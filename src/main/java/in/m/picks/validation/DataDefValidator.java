package in.m.picks.validation;

import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Afields;
import in.m.picks.model.DataDef;
import in.m.picks.util.AccessUtil;

public class DataDefValidator {

	private DataDef dataDef;

	
	public void setDataDef(DataDef dataDef) {
		this.dataDef = dataDef;
	}


	public boolean validate(){
		boolean valid = true;
		valid = validateIndexRange();
		return valid;
	}
	
	private boolean validateIndexRange() {
		boolean valid = true;
		for(Afields afields : dataDef.getAllAfields()){
			try {
				AccessUtil.getRange(afields, "indexRange");
			} catch (NumberFormatException e) {				
				valid = false;
			} catch (AfieldNotFoundException e) {				
			}	
		}
		return valid;
	}
}
