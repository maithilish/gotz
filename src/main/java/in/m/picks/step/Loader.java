package in.m.picks.step;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.DaoFactory;
import in.m.picks.dao.DaoFactory.ORM;
import in.m.picks.dao.IDocumentDao;
import in.m.picks.dao.ILocatorDao;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Document;
import in.m.picks.model.Field;
import in.m.picks.model.Fields;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Locator;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Loader implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Loader.class);

	private Locator locator;
	private Document document;
	@SuppressWarnings("unused")
	private List<FieldsBase> fields;

	@Override
	public void run() {
		if (locator == null) {
			logger.warn("{}", "Unable to run loader as input [Locator] is not set");
		} else {
			try {
				load();
				store();
				handover();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void load() throws Exception {
		Locator savedLocator = getLocatorFromStore(locator.getName(),
				locator.getGroup());
		if (savedLocator != null) {
			// update existing locator with new values
			savedLocator.getFields().addAll(locator.getFields());
			savedLocator.setUrl(locator.getUrl());
			// switch locator with existing locator (detached locator)
			locator = savedLocator;
			Util.logState(logger,"locator", "--- Locator loaded from store ---",
					locator.getFields(), locator);
		} else {
			logger.debug("{}", "Locator from file is used as it is not yet stored");
		}

		Long liveDocumentId = getLiveDocumentId();
		if (liveDocumentId == null) {
			try {
				Object object = fetchDocument(locator.getUrl());
				document = new Document();
				document.setName(locator.getName());
				document.setDocumentObject(object);
				document.setFromDate(ConfigService.INSTANCE.getRunDateTime());
				document.setToDate(getToDate());
				document.setUrl(locator.getUrl());
				locator.getDocuments().add(document);
				logger.trace("created new document {}", document);
			} catch (Exception e) {
				logger.warn("{}", e);
			}
		} else {
			document = getDocument(liveDocumentId);
			logger.trace("found live document {}", document);
		}
	}

	@Override
	public void store() throws Exception {
		boolean persist = true;
		try {
			persist = FieldsUtil.isFieldTrue(locator.getFields(), "persist");
		} catch (FieldNotFoundException e) {
		}

		if (persist) {
			try {
				/*
				 * fields are not persistable, so need to set them from the
				 * fields.xml every time
				 */
				List<FieldsBase> fields = locator.getFields();
				ORM orm = DaoFactory
						.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
				ILocatorDao dao = DaoFactory.getDaoFactory(orm).getLocatorDao();
				dao.storeLocator(locator);
				// reload locator and document
				locator = dao.getLocator(locator.getId());
				locator.getFields().addAll(fields);
				document = getDocument(document.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("{}", e.getMessage());
				throw e;
			}
			logger.debug("Stored {}", locator);
			Util.logState(logger,"locator", "--- Locator now stored ---", locator.getFields(),
					locator);
		} else {
			logger.debug("Persist [false]. Not Stored {}", locator);
		}
	}

	@Override
	public void handover() throws Exception {
		// TODO test separate instance for each call
		// for each dataDef create dedicated parser

		String givenUpMessage = Util.buildString("Create parser for locator [",
				locator.getName(), "] failed.");
		List<FieldsBase> dataDefFields = FieldsUtil
				.getGroupFields(locator.getFields(), "datadef");
		if (dataDefFields.size() == 0) {
			logger.warn("{} {}", givenUpMessage, " No datadef field found.");
		}
		for (FieldsBase dataDefField : dataDefFields) {
			if (dataDefField instanceof Fields) {
				Fields fields = (Fields) dataDefField;
				if (isDocumentLoaded()) {
					pushParserTask(fields);
				} else {
					logger.warn("Document not loaded - Locator [{}]", locator);
					MonitorService.INSTANCE.addActivity(Type.GIVENUP,
							"Document not loaded. " + givenUpMessage);
				}
			}
		}
	}

	private void pushParserTask(Fields fields) {
		try {
			IStep task = createParser(fields, document);
			// PerserPool.INSTANCE.submit(parser);
			TaskPoolService.getInstance().submit("parser", task);
			logger.debug("Parser instance [{}] pushed to pool. Locator [{}]",
					task.getClass(), locator.getName());
		} catch (Exception e) {
			logger.warn("Unable to create parser [{}] for locator [{}]", e,
					locator.getName());
			String givenUpMessage = Util.buildString("Create parser for locator [",
					locator.getName(), "] failed.");
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, givenUpMessage, e);
		}
	}

	private IStep createParser(Fields fields, Document input)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, FieldNotFoundException {
		String parserClassName = FieldsUtil.getValue(fields, "parser");
		IStep parserStep = StepService.INSTANCE.getStep(parserClassName).instance();
		parserStep.setInput(input);

		Field field = new Field();
		field.setName("locatorName");
		field.setValue(locator.getName());

		fields.getFields().add(field);
		List<FieldsBase> handoverFields = new ArrayList<>();
		handoverFields.add(fields);

		parserStep.setFields(handoverFields);

		return parserStep;
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Locator) {
			this.locator = (Locator) input;
		} else {
			logger.warn("Input is not instance of Locator type. {}",
					input.getClass().toString());
		}
	}

	@Override
	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
	}

	protected Locator getLocatorFromStore(String locName, String locGroup) {
		try {
			ORM orm = DaoFactory
					.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			ILocatorDao dao = DaoFactory.getDaoFactory(orm).getLocatorDao();
			Locator existingLocator = dao.getLocator(locName, locGroup);
			return existingLocator;
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

	protected Long getLiveDocumentId() {
		Long liveDocumentId = null;
		if (locator.getId() == null) {
			// new locator so no document
			return null;
		} else {
			for (Document r : locator.getDocuments()) {
				Date toDate = r.getToDate();
				Date runDateTime = ConfigService.INSTANCE.getRunDateTime();
				// toDate > today
				if (toDate.compareTo(runDateTime) >= 0) {
					liveDocumentId = r.getId();
				}
			}
		}
		return liveDocumentId;
	}

	protected Date getToDate() {
		ZonedDateTime fromDate = ZonedDateTime.ofInstant(
				document.getFromDate().toInstant(), ZoneId.systemDefault());
		ZonedDateTime toDate = null;
		String live = null;
		try {
			live = FieldsUtil.getValue(locator.getFields(), "live");
		} catch (FieldNotFoundException e) {
			logger.warn("{} - defaults to 0 day. {}", e, locator);
		}
		if (StringUtils.equals(live, "0") || StringUtils.isBlank(live)) {
			live = "PT0S"; // zero second
		}
		try {
			TemporalAmount ta = Util.praseTemporalAmount(live);
			toDate = fromDate.plus(ta);
		} catch (DateTimeParseException e) {
			String[] patterns = ConfigService.INSTANCE
					.getConfigArray("picks.dateParsePattern");
			try {
				// multiple patterns so needs DateUtils
				Date td = DateUtils.parseDateStrictly(live, patterns);
				toDate = ZonedDateTime.ofInstant(td.toInstant(),
						ZoneId.systemDefault());
			} catch (ParseException pe) {
				logger.warn("{} afield [live] {} {}. Defaults to 0 days", locator,
						live, e);
				TemporalAmount ta = Util.praseTemporalAmount("PT0S");
				toDate = fromDate.plus(ta);
			}
		}
		logger.trace("Document.toDate. [live] {} [toDate] {} : {}", live, toDate,
				locator);
		return Date.from(Instant.from(toDate));
	}

	protected Document getDocument(Long id) {
		// get fully loaded Document
		try {
			ORM orm = DaoFactory
					.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
			IDocumentDao dao = DaoFactory.getDaoFactory(orm).getDocumentDao();
			return dao.getDocument(id);
		} catch (RuntimeException e) {
			logger.error("{}", e.getMessage());
			logger.trace("", e);
			throw e;
		}
	}

	public boolean isDocumentLoaded() {
		return Objects.nonNull(document);
	}

	// template method to be implemented by subclass
	public abstract Object fetchDocument(String url)
			throws Exception, MalformedURLException, IOException;
}
