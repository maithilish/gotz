package in.m.picks.ext;

import java.io.File;
import java.io.IOException;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.shared.ConfigService;
import in.m.picks.step.IStep;
import in.m.picks.step.Loader;

public class JSoupHtmlLoader extends Loader {

	final static Logger logger = LoggerFactory.getLogger(JSoupHtmlLoader.class);

	@Override
	public IStep instance() {
		return new JSoupHtmlLoader();
	}

	@Override
	public Object fetchDocument(String url) throws IOException {
		logger.info("fetch web resource {}", url);
		try {
			Document doc;
			if (UrlValidator.getInstance().isValid(url)) {
				doc = Jsoup.connect(url).userAgent(getUserAgent())
						.timeout(getTimeout()).get();
				logger.debug("fetched web resource {}", url);
			} else {
				File file = new File(url);
				doc = Jsoup.parse(file, null);
				logger.debug("fetched file resource {}", url);
			}
			return doc.toString();
		} finally {

		}
	}

	private int getTimeout() {
		int timeout = 120000; // millis
		String key = "picks.webClient.timeout";
		try {
			timeout = Integer.parseInt(ConfigService.INSTANCE.getConfig(key));
		} catch (NumberFormatException e) {
			// TODO add activity or update config with default
			String msg = "for config [" + key + "] using default value "
					+ timeout;
			logger.warn("{}. {}", e, msg);
		}
		return timeout;
	}

	private String getUserAgent() {
		String userAgent = ConfigService.INSTANCE
				.getConfig("picks.webClient.userAgent");
		return userAgent;
	}
}
