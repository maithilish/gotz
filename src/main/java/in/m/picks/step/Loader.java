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
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Loader extends Step {

	final static Logger logger = LoggerFactory.getLogger(Loader.class);

	private Locator locator;
	private Document document;

	@Override
	public void run() {
		if (locator == null) {
			logger.warn("{}", "unable to run loader as Locator is not set");
		} else {
			try {
				load();
				store();
				handover();
			} catch (Exception e) {
				String message = Util.buildString("load Locator[name=",
						locator.getName(), " group=", locator.getGroup(), "]");
				logger.error("{} {}", message, Util.getMessage(e));
				logger.debug("{}", e);
				MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
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
			Util.logState(logger, "locator", "--- Locator loaded from store ---",
					locator.getFields(), locator);
		} else {
			logger.debug("{}", "Locator from file is used as it is not yet stored");
		}

		Long liveDocumentId = getLiveDocumentId();
		if (liveDocumentId == null) {
			Object object = fetchDocument(locator.getUrl());
			document = new Document();
			document.setName(locator.getName());
			document.setDocumentObject(object);
			document.setFromDate(ConfigService.INSTANCE.getRunDateTime());
			document.setToDate(getToDate());
			document.setUrl(locator.getUrl());
			locator.getDocuments().add(document);
			consistent = true;
			logger.info("create new document. Locator[name={} group={} toDate={}]",
					locator.getName(), locator.getGroup(), document.getToDate());
			logger.trace("create new document {}", document);
		} else {
			document = getDocument(liveDocumentId);
			consistent = true;
			logger.info("use stored document. Locator[name={} group={} toDate={}]",
					locator.getName(), locator.getGroup(), document.getToDate());
			logger.trace("found document {}", document);
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
			/*
			 * fields are not persistable, so need to set them from the
			 * fields.xml every time
			 */
			try {
				List<FieldsBase> fields = locator.getFields();
				ORM orm = DaoFactory
						.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
				ILocatorDao dao = DaoFactory.getDaoFactory(orm).getLocatorDao();
				dao.storeLocator(locator);
				// reload locator and document
				locator = dao.getLocator(locator.getId());
				locator.getFields().addAll(fields);
				document = getDocument(document.getId());
				logger.debug("Stored {}", locator);
				Util.logState(logger, "locator", "--- Locator now stored ---",
						locator.getFields(), locator);
			} catch (RuntimeException e) {
				logger.error("{}", e.getLocalizedMessage());
				logger.trace("", e);
				throw e;
			}
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
		List<FieldsBase> stepsFields = FieldsUtil.getGroupFields(locator.getFields(),
				"steps");
		List<FieldsBase> dataDefFields = FieldsUtil
				.getGroupFields(locator.getFields(), "datadef");
		if (dataDefFields.size() == 0) {
			logger.warn("{} {}", givenUpMessage, " No datadef field found.");
		}
		for (FieldsBase dataDefField : dataDefFields) {
			if (dataDefField instanceof Fields) {
				Fields fields = Util.deepClone(Fields.class, (Fields) dataDefField);
				if (isDocumentLoaded()) {
					Field field = FieldsUtil.createField("locatorName",
							locator.getName());
					fields.getFields().add(field);
					field = FieldsUtil.createField("locatorGroup",
							locator.getGroup());
					fields.getFields().add(field);
					field = FieldsUtil.createField("locatorUrl", locator.getUrl());
					fields.getFields().add(field);
					List<FieldsBase> fieldsList = new ArrayList<>();
					fieldsList.add(fields);
					fieldsList.addAll(stepsFields);

					pushTask(document, fieldsList);
				} else {
					logger.warn("Document not loaded - Locator [{}]", locator);
					MonitorService.INSTANCE.addActivity(Type.GIVENUP,
							"Document not loaded. " + givenUpMessage);
				}
			}
		}
	}

	@Override
	public boolean isConsistent() {
		return (consistent && isDocumentLoaded());
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
			logger.warn("{} - defaults to 0 day. Locator[name={}, group={}]", e,
					locator.getName(), locator.getGroup());
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
				logger.warn("{} field [live] {} {}. Defaults to 0 days", locator,
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
		// get Document with documentObject
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
