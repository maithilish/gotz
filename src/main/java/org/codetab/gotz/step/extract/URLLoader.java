package org.codetab.gotz.step.extract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseLoader;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;

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
     * Fetch document content from web, file system or classpath using the URL
     * and convert it to byte array. Where urlspec
     * <ul>
     * <li>starts with http or https - fetch from web</li>
     * <li>starts with file - fetch from file system, path can be abs or
     * relative</li>
     * <li>path without protocol - loads from classpath</li>
     * </ul>
     * @param urlSpec
     *            URL string
     * @return byte[] document content fetched from web, file system or
     *         classpath
     * @see org.codetab.gotz.step.base.BaseLoader#fetchDocumentObject(String)
     */
    @Override
    public byte[] fetchDocumentObject(final String urlSpec) throws IOException {
        // TODO charset encoding
        byte[] bytes = null;

        String protocol = ucHelper.getProtocol(urlSpec);
        if (protocol.equals("resource")) {
            LOGGER.info(Messages.getString("URLLoader.9"), urlSpec); //$NON-NLS-1$
            try {
                URL fileURL = URLLoader.class.getResource(urlSpec);
                bytes = IOUtils.toByteArray(fileURL);
                LOGGER.debug(Messages.getString("URLLoader.10"), urlSpec); //$NON-NLS-1$
                return bytes;
            } catch (IOException | NullPointerException e1) {
                throw new IOException(
                        Util.join("file not found [", urlSpec, "]"));
            }
        }

        if (protocol.equals("file")) {
            LOGGER.info(Messages.getString("URLLoader.3"), urlSpec); //$NON-NLS-1$
            try {
                URL fileURL = new URL(urlSpec); // $NON-NLS-1$
                bytes = IOUtils.toByteArray(fileURL);
                LOGGER.debug(Messages.getString("URLLoader.5"), urlSpec); //$NON-NLS-1$
                return bytes;
            } catch (IOException | NullPointerException e) {
                throw new IOException(
                        Util.join("file not found [", urlSpec, "]"));
            }
        }

        if (protocol.equals("http") || protocol.equals("https")) {
            String urlSpecEscaped = null;
            if (UrlValidator.getInstance().isValid(urlSpec)) {
                urlSpecEscaped = urlSpec;
            } else {
                urlSpecEscaped =
                        UrlEscapers.urlFragmentEscaper().escape(urlSpec);
            }

            LOGGER.info(Messages.getString("URLLoader.0"), urlSpecEscaped); //$NON-NLS-1$
            HttpURLConnection uc = (HttpURLConnection) ucHelper
                    .getURLConnection(urlSpecEscaped);

            int timeout = getTimeout();
            uc.setConnectTimeout(timeout);
            uc.setReadTimeout(timeout);
            ucHelper.setRequestProperty(uc, "User-Agent", getUserAgent()); //$NON-NLS-1$

            uc.connect();
            int respCode = uc.getResponseCode();
            if (respCode != HttpURLConnection.HTTP_OK) {
                throw new IOException(Util.join(
                        "HTTP response : " + respCode + ", URL : ", urlSpec));
            }

            bytes = IOUtils.toByteArray(uc);
            LOGGER.debug(Messages.getString("URLLoader.2"), urlSpecEscaped, //$NON-NLS-1$
                    bytes.length);

            return bytes;
        }

        throw new IOException(Util.join("unknown protocol [", urlSpec, "]"));
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
        String key = "gotz.webClient.timeout"; //$NON-NLS-1$
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            // TODO add activity or update config with default
            String msg = Util.join(Messages.getString("URLLoader.7"), key, //$NON-NLS-1$
                    Messages.getString("URLLoader.8"), String.valueOf(timeout)); //$NON-NLS-1$
            LOGGER.debug("{}. {}", e, msg); //$NON-NLS-1$
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
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0"; //$NON-NLS-1$
        String key = "gotz.webClient.userAgent"; //$NON-NLS-1$
        try {
            userAgent = configService.getConfig(key);
        } catch (ConfigNotFoundException e) {
            String msg = Util.join(Messages.getString("URLLoader.1"), key, //$NON-NLS-1$
                    Messages.getString("URLLoader.4"), userAgent); //$NON-NLS-1$
            LOGGER.debug("{}. {}", e, msg); //$NON-NLS-1$
        }
        return userAgent;
    }

}
