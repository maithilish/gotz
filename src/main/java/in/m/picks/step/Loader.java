package in.m.picks.step;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import in.m.picks.dao.DaoFactory;
import in.m.picks.dao.DaoFactory.ORM;
import in.m.picks.dao.IDocumentDao;
import in.m.picks.dao.ILocatorDao;
import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Afield;
import in.m.picks.model.Afields;
import in.m.picks.model.Document;
import in.m.picks.model.Locator;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.ConfigService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.Util;

public abstract class Loader implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Loader.class);

	private Locator locator;
	private Document document;	
	@SuppressWarnings("unused")
	private Afields afields;

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
		debugState(locator, "--- Locator read from file ---", true);
		Locator savedLocator = getLocatorFromStore(locator.getName(),
				locator.getGroup());
		if (savedLocator != null) {
			// update existing locator with new values
			savedLocator.setAfields(locator.getAfields());
			savedLocator.setUrl(locator.getUrl());
			// switch locator with existing locator (detached locator)
			locator = savedLocator;
			debugState(locator, "--- Locator loaded from store ---", true);
		} else {
			debugState(locator, "Locator from file is used as it is not yet stored",
					false);
		}

		Long liveDocumentId = getLiveDocumentId();
		if (liveDocumentId == null) {
			try {
				Object object = fetchDocument(locator.getUrl());
				document = new Document();
				document.setDocumentObject(object);
				document.setFromDate(ConfigService.INSTANCE.getRunDateTime());
				document.setToDate(getToDate());
				document.setUrl(locator.getUrl());
				locator.addDocument(document);
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
			persist = AccessUtil.isAfieldTrue(locator, "persist");
		} catch (AfieldNotFoundException e) {
		}

		if (persist) {
			try {
				/*
				 * afields are not persistable, so need to set them from the
				 * afields.xml every time
				 */
				List<Afield> afields = locator.getAfields();
				ORM orm = DaoFactory
						.getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
				ILocatorDao dao = DaoFactory.getDaoFactory(orm).getLocatorDao();
				dao.storeLocator(locator);
				// reload locator and document
				locator = dao.getLocator(locator.getId());
				locator.setAfields(afields);
				document = getDocument(document.getId());
			} catch (Exception e) {
				logger.debug("{}", e.getMessage());
				throw e;
			}
			logger.debug("Stored {}", locator);
			debugState(locator, "--- Locator now stored ---", true);
		} else {
			logger.debug("Persist [false]. Not Stored {}", locator);
			debugState(locator, "Persist [false], Locator not stored", false);
		}
	}

	@Override
	public void handover() throws Exception {
		// TODO test separate instance for each call
		// for each dataDef create dedicated parser

		String givenUpMessage = Util.buildString("Create parser for locator [",
				locator.getName(), "] failed.");
		List<Afield> afieldList = locator.getAfieldsByGroup("datadef").getAfields();
		if (afieldList.size() == 0) {
			logger.warn("{} {}", givenUpMessage, " No datadef afield found.");
		}
		for (Afield afield : afieldList) {
			if (isDocumentLoaded()) {
				String parserClassName = AccessUtil.getStringValue(locator,
						"parser");
				String dataDefName = afield.getValue();
				pushParserTask(parserClassName, dataDefName);
			} else {
				logger.warn("Document not loaded - Locator [{}]", locator);
				MonitorService.INSTANCE.addActivity(Type.GIVENUP,
						"Document not loaded. " + givenUpMessage);
			}
		}
	}

	private void pushParserTask(String parserClassName, String dataDefName) {
		try {
			IStep task = createParser(parserClassName, dataDefName, document);
			//PerserPool.INSTANCE.submit(parser);
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

	private IStep createParser(String parserClassName, String dataDefName,
			Document input) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		IStep parserStep = StepService.INSTANCE.getStep(parserClassName).instance();
		parserStep.setInput(input);
		System.out.println(input.getDocumentObject());
		Afields afields = locator;
		afields.addAfield(new Afield("locatorName", locator.getName()));
		afields.addAfield(new Afield("datadefName", dataDefName));
		parserStep.setAfields(afields);
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
	public void setAfields(Afields afields) {
		this.afields = afields;
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
			live = AccessUtil.getStringValue(locator, "live");
		} catch (AfieldNotFoundException e) {
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

	public void debugState(Locator locator, String heading, boolean full) {
		try {
			if (AccessUtil.isAfieldTrue(locator, "debugstate", "locator")) {
				MDC.put("entitytype", "locator");
				logger.debug(heading);
				if (full) {
					debugLocator(locator);
				}
				MDC.remove("entitytype");
			}
		} catch (AfieldNotFoundException e) {
		}
	}

	private void debugLocator(Locator locator) {
		String line = System.lineSeparator();
		String json = Util.getIndentedJson(locator, true);
		String className = locator.getClass().getName();
		StringBuilder sb = new StringBuilder();
		sb.append(className);
		sb.append(line);
		sb.append(json);
		logger.debug("{}", sb);
	}

	// template method to be implemented by subclass
	public abstract Object fetchDocument(String url)
			throws Exception, MalformedURLException, IOException;
}
