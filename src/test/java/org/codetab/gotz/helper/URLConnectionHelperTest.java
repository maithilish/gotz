package org.codetab.gotz.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * URLConnectionHelper tests.
 * @author Maithilish
 *
 */
public class URLConnectionHelperTest {

    private URLConnectionHelper ucHelper;

    @Before
    public void setUp() throws Exception {
        ucHelper = new URLConnectionHelper();
    }

    @Test
    public void testGetURLConnection() throws IOException {
        String urlSpec = "file:///home/x/a.txt";
        URL expected = new URL(urlSpec);

        // when
        URLConnection actual = ucHelper.getURLConnection(urlSpec);

        assertThat(actual.getURL()).isEqualTo(expected);
    }

    @Test
    public void testSetRequestProperty() throws IOException {
        URLConnection uc = ucHelper.getURLConnection("http://example.org");

        // when
        ucHelper.setRequestProperty(uc, "x", "y");

        assertThat(uc.getRequestProperty("x")).isEqualTo("y");
    }

}
