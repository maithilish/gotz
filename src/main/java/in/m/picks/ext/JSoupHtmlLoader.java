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

public final class JSoupHtmlLoader extends Loader {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlLoader.class);
    private static final int TIMEOUT_MILLIS = 120000;

    @Override
    public IStep instance() {
        return new JSoupHtmlLoader();
    }

    @Override
    public Object fetchDocument(final String url) throws IOException {
        LOGGER.info("fetch web resource {}", url);
        Document doc;
        if (UrlValidator.getInstance().isValid(url)) {
            doc = Jsoup.connect(url).userAgent(getUserAgent())
                    .timeout(getTimeout()).get();
            LOGGER.debug("fetched web resource {}", url);
        } else {
            File file = new File(url);
            doc = Jsoup.parse(file, null);
            LOGGER.debug("fetched file resource {}", url);
        }
        return doc.toString();
    }

    private int getTimeout() {
        int timeout = TIMEOUT_MILLIS;
        String key = "picks.webClient.timeout";
        try {
            timeout = Integer.parseInt(ConfigService.INSTANCE.getConfig(key));
        } catch (NumberFormatException e) {
            // TODO add activity or update config with default
            String msg = "for config [" + key + "] using default value "
                    + timeout;
            LOGGER.warn("{}. {}", e, msg);
        }
        return timeout;
    }

    private String getUserAgent() {
        String userAgent = ConfigService.INSTANCE
                .getConfig("picks.webClient.userAgent");
        return userAgent;
    }
}
