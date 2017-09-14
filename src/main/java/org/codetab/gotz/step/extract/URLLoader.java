package org.codetab.gotz.step.extract;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Create or loads document from persistence store and uses URL to fetch
 * document contents from web or file system.
 *
 * @author Maithilish
 *
 */
public final class URLLoader extends BaseLoader {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(URLLoader.class);

    /**
     * default timeout value in ms.
     */
    private static final int TIMEOUT_MILLIS = 120000;

    /**
     * Helper to handle URLConnection.
     */
    @Inject
    private URLConnectionHelper ucHelper;

    /**
     * Get instance of this class.
     * @return instance of this class
     */
    @Override
    public IStep instance() {
        // TODO check whether instance method is required in all steps as it
        // return this instead of a new instance
        return this;
    }

    /**
     * Fetch document content from web or file system using the URL and convert
     * it to byte array. URL can be address prefixed with http:// or file path -
     * absolute or relative to class path.
     * @param urlSpec
     *            URL string
     * @return byte[] document content fetched from web or file system
     * @see org.codetab.gotz.step.base.BaseLoader#fetchDocumentObject(String)
     */
    @Override
    public byte[] fetchDocumentObject(final String urlSpec) throws IOException {
        // TODO charset encoding
        byte[] bytes;
        if (UrlValidator.getInstance().isValid(urlSpec)) {
            LOGGER.info("fetch web resource {}", urlSpec);
            URLConnection uc = ucHelper.getURLConnection(urlSpec);

            int timeout = getTimeout();
            uc.setConnectTimeout(timeout);
            uc.setReadTimeout(timeout);
            ucHelper.setRequestProperty(uc, "User-Agent", getUserAgent());

            bytes = IOUtils.toByteArray(uc);
            LOGGER.debug("fetched web resource {}", urlSpec);
        } else {
            LOGGER.info("fetch file resource {}", urlSpec);
            URL fileURL = new URL(new URL("file:"), urlSpec);
            bytes = IOUtils.toByteArray(fileURL);
            LOGGER.debug("fetched file resource {}", urlSpec);
        }
        return bytes;
    }

    /**
     * <p>
     * Timeout value (in ms) for connection and read time out.
     * <p>
     * default value - 120000 ms
     * <p>
     * configurable using config key - gotz.webClient.timeout
     *
     * @return timeout value
     */
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

    /**
     * <p>
     * User Agent string used for request.
     * <p>
     * default value
     * <p>
     * Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0
     * <p>
     * configurable using config key - gotz.webClient.userAgent
     * @return user agent string
     */
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
