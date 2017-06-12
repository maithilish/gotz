package org.codetab.gotz.ext;

import java.io.File;
import java.io.IOException;

import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Loader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlLoader extends Loader {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlLoader.class);
    private static final int TIMEOUT_MILLIS = 120000;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public Object fetchDocument(final String url) throws IOException {
        LOGGER.info("fetch web resource {}", url);
        Document doc;
        if (UrlValidator.getInstance().isValid(url)) {
            doc = Jsoup.connect(url).userAgent(getUserAgent()).timeout(getTimeout())
                    .get();
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
        String key = "gotz.webClient.timeout";
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            // TODO add activity or update config with default
            String msg = "for config [" + key + "] using default value " + timeout;
            LOGGER.warn("{}. {}", e, msg);
        }
        return timeout;
    }

    private String getUserAgent() {
        String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
        String key = "gotz.webClient.userAgent";
        try {
            userAgent = configService.getConfig(key);
        } catch (ConfigNotFoundException e) {
            String msg = "for config [" + key + "] using default value " + userAgent;
            LOGGER.warn("{}. {}", e, msg);
        }
        return userAgent;
    }
}
