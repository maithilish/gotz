package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.step.extract.URLLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * Tests for URLLoader.
 * @author Maithilish
 *
 */
public class URLLoaderTest {

    @Mock
    private URLConnection uc;
    @Mock
    private URLConnectionHelper ucHelper;
    @Mock
    private ConfigService configService;

    @InjectMocks
    private URLLoader urlLoader;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() {
        assertThat(urlLoader.isConsistent()).isFalse();
        assertThat(urlLoader.getStepType()).isNull();
        assertThat(urlLoader.instance()).isInstanceOf(URLLoader.class);
        assertThat(urlLoader.instance()).isSameAs(urlLoader.instance());
    }

    @Test
    public void testFetchDocumentObjectFromWeb()
            throws IOException, ConfigNotFoundException {
        String url = "http://example.com";
        String standinLocalUrl =
                "target/test-classes/testdefs/urlloader/example.html";

        URL localUrl = new URL(new URL("file:"), standinLocalUrl);
        URLConnection localUc = localUrl.openConnection();

        given(ucHelper.getURLConnection(url)).willReturn(localUc);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("1000");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn("x");

        // when
        byte[] actual = urlLoader.fetchDocumentObject(url);

        byte[] expected =
                IOUtils.toByteArray(new URL(new URL("file:"), standinLocalUrl));

        assertThat(actual).isEqualTo(expected);

        assertThat(localUc.getConnectTimeout()).isEqualTo(1000);
        assertThat(localUc.getReadTimeout()).isEqualTo(1000);
        verify(ucHelper).setRequestProperty(localUc, "User-Agent", "x");
    }

    @Test
    public void testFetchDocumentObjectFromWebDefaultConfigs()
            throws IOException, ConfigNotFoundException {
        String url = "http://example.com";
        String standinLocalUrl =
                "target/test-classes/testdefs/urlloader/example.html";

        URL localUrl = new URL(new URL("file:"), standinLocalUrl);
        URLConnection localUc = localUrl.openConnection();

        given(ucHelper.getURLConnection(url)).willReturn(localUc);
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willThrow(ConfigNotFoundException.class);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willThrow(ConfigNotFoundException.class);

        // when
        urlLoader.fetchDocumentObject(url);

        String defaultUserAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
        int defaultTimeout = 120000;

        assertThat(localUc.getConnectTimeout()).isEqualTo(defaultTimeout);
        assertThat(localUc.getReadTimeout()).isEqualTo(defaultTimeout);

        verify(ucHelper).setRequestProperty(localUc, "User-Agent",
                defaultUserAgent);
    }

    @Test
    public void testFetchDocumentObjectFromWebInvalidConfig()
            throws IOException, ConfigNotFoundException {
        String url = "http://example.com";
        String standinLocalUrl =
                "target/test-classes/testdefs/urlloader/example.html";

        URL localUrl = new URL(new URL("file:"), standinLocalUrl);
        URLConnection localUc = localUrl.openConnection();

        given(ucHelper.getURLConnection(url)).willReturn(localUc);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("x");

        // when
        urlLoader.fetchDocumentObject(url);

        int defaultTimeout = 120000;

        assertThat(localUc.getConnectTimeout()).isEqualTo(defaultTimeout);
        assertThat(localUc.getReadTimeout()).isEqualTo(defaultTimeout);
    }

    @Test
    public void testFetchDocumentObjectFromWebExpectExcetpion()
            throws IOException {
        String url = "http://example.com";
        String standinLocalUrl =
                "target/test-classes/testdefs/urlloader/non-exist.html";

        URL localUrl = new URL(new URL("file:"), standinLocalUrl);
        URLConnection localUc = localUrl.openConnection();

        given(ucHelper.getURLConnection(url)).willReturn(localUc);

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(url);
    }

    @Test
    public void testFetchDocumentObjectFromFile()
            throws IOException, ConfigNotFoundException {
        String url = "target/test-classes/testdefs/urlloader/example.html";

        // when
        byte[] actual = urlLoader.fetchDocumentObject(url);

        byte[] expected = IOUtils.toByteArray(new URL(new URL("file:"), url));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testFetchDocumentObjectFromFileExpectException()
            throws IOException, ConfigNotFoundException {
        String url = "target/test-classes/testdefs/urlloader/non-exist.html";

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(url);
    }

}
