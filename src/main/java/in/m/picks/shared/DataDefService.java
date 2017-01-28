package in.m.picks.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import in.m.picks.dao.DaoFactory.ORM;
import in.m.picks.dao.IDataDefDao;
import in.m.picks.dao.jdo.DaoFactory;
import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.ColComparator;
import in.m.picks.model.Data;
import in.m.picks.model.DataDef;
import in.m.picks.model.Member;
import in.m.picks.model.RowComparator;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.Util;
import in.m.picks.validation.DataDefValidator;

public enum DataDefService {

	INSTANCE;

	final Logger logger = LoggerFactory.getLogger(DataDefService.class);

	private Map<String, DataDef> dataDefsMap;

	private DataDefService() {
		logger.info("Initializing DataDefs Singleton");
		validateDataDefs();
		storeDataDefs();
		setDataDefsMap();
		traceDataDefs();
		traceDataStructure();
		logger.debug("Initialized DataDefs Singleton");
	}

	private void validateDataDefs() {
		DataDefValidator validator = new DataDefValidator();
		List<DataDef> dataDefs = getDataDefsFromBeans();
		boolean valid = true;
		for(DataDef dataDef : dataDefs){
			validator.setDataDef(dataDef);
			if(!validator.validate()){
			   valid = false;
			}			
		}
		if(!valid){
			MonitorService.INSTANCE.triggerFatal("Invalid Datadefs");
		}
	}

	private void storeDataDefs() {
		List<DataDef> newDataDefs = getDataDefsFromBeans();
		List<DataDef> oldDataDefs = loadDataDefsFromStore();

		for (DataDef newDataDef : newDataDefs) {
			String debugMessage;
			String name = newDataDef.getName();
			DataDef oldDataDef = getDataDef(oldDataDefs, name);
			String saveMode = getSaveMode(oldDataDef, newDataDef);
			switch (saveMode) {
			case "insert":
				debugMessage = "not in store and will persist";
				storeDataDef(newDataDef);
				break;
			case "update":
				debugMessage = "changed and will update stores";
				resetHighDate(oldDataDef); // reset to run date
				storeDataDef(oldDataDef); // and update
				storeDataDef(newDataDef); // insert new changes
				break;
			default:
				debugMessage = "no changes";
				break;
			}
			logger.debug("DataDef [{}] {}", name, debugMessage);
		}

	}

	private String getSaveMode(DataDef oldDataDef, DataDef newDataDef) {
		String saveMode = null;
		if (oldDataDef == null) {
			saveMode = "insert";
		} else {
			if (oldDataDef.equals(newDataDef)) {
				saveMode = ""; // default no change
			} else {
				saveMode = "update";
			}
		}
		return saveMode;
	}

	private DataDef getDataDef(List<DataDef> dataDefs, String name) {
		for (DataDef dataDef : dataDefs) {
			if (dataDef.getName().equals(name)) {
				return dataDef;
			}
		}
		return null;
	}

	private void setDataDefsMap() {
		/*
		 * afields are not persisted, so we need to copy them. As afields are
		 * nulled by persistence we need a fresh list of datadefs from Beans
		 */
		List<DataDef> newDataDefs = getDataDefsFromBeans();
		List<DataDef> storedDataDefs = loadDataDefsFromStore();
		copyAfields(newDataDefs, storedDataDefs);

		dataDefsMap = new HashMap<String, DataDef>();
		for (DataDef dataDef : storedDataDefs) {
			dataDefsMap.put(dataDef.getName(), dataDef);
		}

		// debug state - when afield group="debugstate" name="datadef"
		// value="true"
		for (DataDef dataDef : newDataDefs) {
			debugState(dataDef, "--- DataDef read from file ----");
		}
		for (DataDef dataDef : storedDataDefs) {
			debugState(dataDef, "--- DataDef loaded from store ----");
		}
	}

	private List<DataDef> getDataDefsFromBeans() {
		List<DataDef> newDataDefs = BeanService.INSTANCE.getBeans(DataDef.class);
		setDefaults(newDataDefs);
		setDates(newDataDefs);
		return newDataDefs;
	}

	private void copyAfields(List<DataDef> srcDataDefs, List<DataDef> destDataDefs) {

		for (DataDef sDataDef : srcDataDefs) {
			for (DataDef dDataDef : destDataDefs) {
				if (sDataDef.getName().equals(dDataDef.getName())) {
					dDataDef.setAfields(sDataDef.getAfields());
				}
			}
		}
	}

	private void setDefaults(List<DataDef> dataDefs) {
		for (DataDef dataDef : dataDefs) {
			dataDef.setDefaults();
		}
	}

	private void setDates(List<DataDef> dataDefs) {
		for (DataDef dataDef : dataDefs) {
			dataDef.setFromDate(ConfigService.INSTANCE.getRunDateTime());
			dataDef.setToDate(ConfigService.INSTANCE.getHighDate());
		}
	}

	private void resetHighDate(DataDef dataDef) {
		dataDef.setToDate(ConfigService.INSTANCE.getRunDateTime());
	}

	private void storeDataDef(DataDef dataDef) {
		try {
			ORM orm = DaoFactory.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			IDataDefDao dao = DaoFactory.getDaoFactory(orm).getDataDefDao();
			String name = dataDef.getName();
			logger.debug("Store DataDef");
			dao.storeDataDef(dataDef);
			if (dataDef.getId() != null) {
				logger.debug("Stored DataDef : {}", name);
			}
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

	private List<DataDef> loadDataDefsFromStore() {
		try {
			ORM orm = DaoFactory.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			IDataDefDao dao = DaoFactory.getDaoFactory(orm).getDataDefDao();
			List<DataDef> dataDefs = dao
					.getDataDefs(ConfigService.INSTANCE.getRunDateTime());
			logger.debug("DataDef loaded : [{}]", dataDefs.size());
			return dataDefs;
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

	public DataDef getDataDef(String name) throws DataDefNotFoundException {
		DataDef dataDef = dataDefsMap.get(name);
		if (dataDef == null) {
			throw new DataDefNotFoundException(name);
		}
		return dataDef;
	}

	public Data getDataTemplate(String dataDefName) throws DataDefNotFoundException {
		DataDef dataDef = getDataDef(dataDefName);
		return dataDef.getDataTemplate();
	}

	public int getCount() {
		return dataDefsMap.size();
	}

	public void debugState(DataDef dataDef, String heading) {
		try {
			if (AccessUtil.isAfieldTrue(dataDef, "debugstate", "datadef")) {
				MDC.put("entitytype", "datadef");
				logger.debug(heading);
				debugDataDef(dataDef);
				MDC.remove("entitytype");
			}
		} catch (AfieldNotFoundException e) {
		}
	}

	private void traceDataStructure() {
		if (!logger.isTraceEnabled()) {
			return;
		}
		logger.trace("---- Trace data structure ----");
		logger.trace("");
		for (String dataDefName : dataDefsMap.keySet()) {
			try {
				traceDataStructure(getDataTemplate(dataDefName));
			} catch (DataDefNotFoundException e) {
			}
		}
	}

	public void traceDataStructure(Data data) {
		String line = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("DataDef [name=");
		sb.append(data.getDataDef());
		sb.append("] data structure");
		sb.append(line);
		sb.append(line);
		Collections.sort(data.getMembers(), new RowComparator());
		Collections.sort(data.getMembers(), new ColComparator());
		for (Member member : data.getMembers()) {
			List<Axis> axes = new ArrayList<Axis>(member.getAxes());
			Collections.sort(axes);
			for (Axis axis : axes) {
				try {
					System.out.println(AccessUtil.getStringValue(axis, "region"));
				} catch (AfieldNotFoundException e) {
				}
				sb.append(axis.toString());
				sb.append(line);
			}
			sb.append(line);
		}
		logger.trace("{}", sb);
	}

	public void traceDataDefs() {
		if (!logger.isTraceEnabled()) {
			return;
		}
		logger.trace("--- Trace DataDefs ----");
		for (DataDef dataDef : dataDefsMap.values()) {
			StringBuilder sb = formattedDataDef(dataDef);
			logger.trace("{}", sb);
		}
	}

	private void debugDataDef(DataDef dataDef) {
		StringBuilder sb = formattedDataDef(dataDef);
		logger.debug("{}", sb);
	}

	private StringBuilder formattedDataDef(DataDef dataDef) {
		String line = System.lineSeparator();
		String json = Util.getIndentedJson(dataDef, true);
		StringBuilder sb = new StringBuilder();
		sb.append("DataDef [name=");
		sb.append(dataDef.getName());
		sb.append("]");
		sb.append(line);
		sb.append(json);
		return sb;
	}
}
