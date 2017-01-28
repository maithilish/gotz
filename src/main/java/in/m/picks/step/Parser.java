package in.m.picks.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.DaoFactory;
import in.m.picks.dao.DaoFactory.ORM;
import in.m.picks.dao.IDataDao;
import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Afield;
import in.m.picks.model.Afields;
import in.m.picks.model.Data;
import in.m.picks.model.Document;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.DataDefService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.Util;

public abstract class Parser implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Parser.class);

	protected String dataDefName;
	protected String locatorName;
	protected Document document;
	protected Afields afields;

	protected Data data;

	@Override
	public void run() {
		processStep();
	}

	// template method
	private void processStep() {
		try {
			initialize();
			load();
			if (data == null) {
				prepareData();
				parse();
				store();				
			}
			handover();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initialize()
			throws AfieldNotFoundException, DataDefNotFoundException {
		dataDefName = AccessUtil.getStringValue(getAfields(), "datadefName");
		locatorName = AccessUtil.getStringValue(getAfields(), "locatorName");

	}

	private void prepareData() throws DataDefNotFoundException {
		data = DataDefService.INSTANCE.getDataTemplate(dataDefName);
		data.setDataDefId(DataDefService.INSTANCE.getDataDef(dataDefName).getId());
		data.setDocumentId(getDocument().getId());
	}

	// implementation delegated to concrete implementation
	protected abstract Object parse() throws Exception;

	@Override
	public void load() throws Exception {
		Long dataDefId = DataDefService.INSTANCE.getDataDef(dataDefName).getId();
		Long documentId = getDocument().getId();
		data = getDataFromStore(dataDefId, documentId);
	}

	@Override
	public void store() throws Exception {
		boolean persist = true;
		try {
			persist = AccessUtil.isAfieldTrue(afields, "persistdata");
		} catch (AfieldNotFoundException e) {
		}
		if (persist) {
			try {
				ORM orm = DaoFactory
						.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
				IDataDao dao = DaoFactory.getDaoFactory(orm).getDataDao();
				dao.storeData(data);
				data = dao.getData(data.getId());
			} catch (Exception e) {
				logger.debug("{}", e.getMessage());
				throw e;
			}
			logger.debug("Stored {}", data);
		} else {
			logger.debug("Persist Data [false]. Not Stored {}", data);
		}
	}

	@Override
	public void handover() throws Exception {
		String givenUpMessage = Util.buildString("Create transformer for locator [",
				locatorName, "] failed.");
		List<Afield> afieldList = afields.getAfieldsByGroup("transformer")
				.getAfields();
		if (afieldList.size() == 0) {
			logger.warn("{} {}", givenUpMessage, " No transformer afield found.");
		}
		for (Afield afield : afieldList) {
			if (data != null) {
				String transformerClassName = afield.getValue();
				pushTransformerTask(transformerClassName);
			} else {
				logger.warn("Data not loaded - Locator [{}]", locatorName);
				MonitorService.INSTANCE.addActivity(Type.GIVENUP,
						"Data not loaded. " + givenUpMessage);
			}
		}
	}

	private void pushTransformerTask(String transformerClassName) {
		try {
			IStep task = createTransformer(transformerClassName, data);
			TaskPoolService.getInstance().submit("transformer", task);
			logger.debug("Transformer instance [{}] pushed to pool. Locator [{}]",
					task.getClass(), locatorName);
		} catch (Exception e) {
			logger.warn("Unable to create parser [{}] for locator [{}]", e,
					locatorName);
			String givenUpMessage = Util.buildString("Create parser for locator [",
					locatorName, "] failed.");
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, givenUpMessage, e);
		}
	}

	private IStep createTransformer(String transformerClassName, Data input)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		IStep transformerStep = StepService.INSTANCE.getStep(transformerClassName)
				.instance();
		transformerStep.setInput(input);
		transformerStep.setAfields(afields);
		return transformerStep;
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Document) {
			this.document = (Document) input;
		} else {
			logger.warn("Input is not instance of Document type. {}",
					input.getClass().toString());
		}
	}

	@Override
	public void setAfields(Afields afields) {
		this.afields = afields;
	}

	public Afields getAfields() {
		return afields;
	}

	public Document getDocument() {
		return document;
	}

	private Data getDataFromStore(Long dataDefId, Long documentId) {
		try {
			ORM orm = DaoFactory
					.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			IDataDao dao = DaoFactory.getDaoFactory(orm).getDataDao();
			Data data = dao.getData(documentId, dataDefId);
			return data;
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

}
