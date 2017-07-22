package org.codetab.gotz.steps;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.stepbase.BaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLLoader extends BaseLoader {

    static final Logger LOGGER = LoggerFactory.getLogger(URLLoader.class);

    private static final int TIMEOUT_MILLIS = 120000;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public byte[] fetchDocument(final String url) throws IOException {
        // TODO charset encoding
        byte[] bytes;
        if (UrlValidator.getInstance().isValid(url)) {
            LOGGER.info("fetch web resource {}", url);
            URL webURL = new URL(url);
            URLConnection uc = webURL.openConnection();

            int timeout = getTimeout();
            uc.setConnectTimeout(timeout);
            uc.setReadTimeout(timeout);

            uc.setRequestProperty("User-Agent", getUserAgent());

            bytes = IOUtils.toByteArray(uc);
            LOGGER.debug("fetched web resource {}", url);
        } else {
            LOGGER.info("fetch file resource {}", url);
            URL fileURL = new URL(new URL("file:"), url);
            bytes = IOUtils.toByteArray(fileURL);
            LOGGER.debug("fetched file resource {}", url);
        }
        return bytes;
    }

    private int getTimeout() {
        int timeout = TIMEOUT_MILLIS;
        String key = "gotz.webClient.timeout";
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            // TODO add activity or update config with default
            String msg =
                    "for config [" + key + "] using default value " + timeout;
            LOGGER.warn("{}. {}", e, msg);
        }
        return timeout;
    }

    private String getUserAgent() {
        String userAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
        String key = "gotz.webClient.userAgent";
        try {
            userAgent = configService.getConfig(key);
        } catch (ConfigNotFoundException e) {
            String msg =
                    "for config [" + key + "] using default value " + userAgent;
            LOGGER.warn("{}. {}", e, msg);
        }
        return userAgent;
    }

}
