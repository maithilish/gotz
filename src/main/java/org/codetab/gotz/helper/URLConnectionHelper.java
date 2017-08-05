package org.codetab.gotz.helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>
 * Helper for URLConnection. It is not possible to mock URL for tests so
 * extracted to this wrapper.
 * @author Maithilish
 *
 */
public class URLConnectionHelper {

    /**
     * <p>
     * Get connection for URL.
     * @param urlSpec
     *            web address or file path.
     * @return connection for the URL
     * @throws IOException
     *             if IO error
     * @throws MalformedURLException
     *             if url string is invalid
     */
    public URLConnection getURLConnection(final String urlSpec)
            throws IOException, MalformedURLException {
        URL url = new URL(urlSpec);
        URLConnection uc = url.openConnection();
        return uc;
    }

    /**
     * <p>
     * Sets request property. It is not possible to get the property value back
     * after connected and for tests, this method is useful.
     * @param uc
     *            connection
     * @param key
     *            property key
     * @param value
     *            property value
     */
    public void setRequestProperty(final URLConnection uc, final String key,
            final String value) {
        uc.setRequestProperty(key, value);
    }
}
