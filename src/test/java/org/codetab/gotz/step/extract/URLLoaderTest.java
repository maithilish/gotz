package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.metrics.MetricsHelper;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

/**
 * <p>
 * Tests for URLLoader.
 * @author Maithilish
 *
 */
public class URLLoaderTest {

    @Mock
    private URLConnectionHelper ucHelper;
    @Mock
    private ConfigService configService;
    @Mock
    private MetricsHelper metricsHelper;

    @InjectMocks
    private URLLoader urlLoader;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private String urlStr;
    private String filePath;
    private Counter counter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        urlStr = "http://example.org";
        filePath = "target/test-classes/testdefs/urlloader/example.html";
        counter = new Counter();
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
        int timeout = 1000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);
        byte[] contents = "test".getBytes();

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn(String.valueOf(timeout));
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willReturn(contents);
        given(metricsHelper.getCounter(urlLoader, "fetch", "web"))
                .willReturn(counter);

        // when
        byte[] result = urlLoader.fetchDocumentObject(urlStr);

        assertThat(result).isSameAs(contents);
        assertThat(counter.getCount()).isEqualTo(1);

        InOrder inOrder = inOrder(uc, ucHelper);
        inOrder.verify(ucHelper).escapeUrl(urlStr);
        inOrder.verify(uc).setConnectTimeout(timeout);
        inOrder.verify(uc).setReadTimeout(timeout);
        inOrder.verify(ucHelper).setRequestProperty(uc, "User-Agent",
                userAgent);
        inOrder.verify(uc).connect();
    }

    @Test
    public void testFetchDocumentObjectFromWebHttps()
            throws IOException, ConfigNotFoundException {
        int timeout = 1000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);
        byte[] contents = "test".getBytes();

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("https");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn(String.valueOf(timeout));
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willReturn(contents);
        given(metricsHelper.getCounter(urlLoader, "fetch", "web"))
                .willReturn(counter);

        // when
        byte[] result = urlLoader.fetchDocumentObject(urlStr);

        assertThat(result).isSameAs(contents);
        assertThat(counter.getCount()).isEqualTo(1);

        InOrder inOrder = inOrder(uc, ucHelper);
        inOrder.verify(ucHelper).escapeUrl(urlStr);
        inOrder.verify(uc).setConnectTimeout(timeout);
        inOrder.verify(uc).setReadTimeout(timeout);
        inOrder.verify(ucHelper).setRequestProperty(uc, "User-Agent",
                userAgent);
        inOrder.verify(uc).connect();
    }

    @Test
    public void testFetchDocumentObjectFromWebUrlWithSpace()
            throws IOException, ConfigNotFoundException {

        urlStr = "http://example.org/with space/";
        String escapedUrlStr = "http://example.org/with%20space/";
        int timeout = 1000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);
        byte[] contents = "test".getBytes();

        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(ucHelper.escapeUrl(urlStr)).willReturn(escapedUrlStr);
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn(String.valueOf(timeout));
        given(ucHelper.getURLConnection(escapedUrlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willReturn(contents);
        given(metricsHelper.getCounter(urlLoader, "fetch", "web"))
                .willReturn(counter);

        // when
        byte[] result = urlLoader.fetchDocumentObject(urlStr);

        assertThat(result).isSameAs(contents);
        assertThat(counter.getCount()).isEqualTo(1);

        InOrder inOrder = inOrder(uc, ucHelper);
        inOrder.verify(ucHelper).escapeUrl(urlStr);
        inOrder.verify(uc).setConnectTimeout(timeout);
        inOrder.verify(uc).setReadTimeout(timeout);
        inOrder.verify(ucHelper).setRequestProperty(uc, "User-Agent",
                userAgent);
        inOrder.verify(uc).connect();
    }

    @Test
    public void testFetchDocumentObjectFromWebDefaultConfigs()
            throws IOException, ConfigNotFoundException {

        int timeout = 120000;
        String userAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);
        byte[] contents = "test".getBytes();

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willThrow(ConfigNotFoundException.class);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willThrow(ConfigNotFoundException.class);
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willReturn(contents);
        given(metricsHelper.getCounter(urlLoader, "fetch", "web"))
                .willReturn(counter);

        // when
        byte[] result = urlLoader.fetchDocumentObject(urlStr);

        assertThat(result).isSameAs(contents);
        assertThat(counter.getCount()).isEqualTo(1);

        InOrder inOrder = inOrder(uc, ucHelper);
        inOrder.verify(ucHelper).escapeUrl(urlStr);
        inOrder.verify(uc).setConnectTimeout(timeout);
        inOrder.verify(uc).setReadTimeout(timeout);
        inOrder.verify(ucHelper).setRequestProperty(uc, "User-Agent",
                userAgent);
        inOrder.verify(uc).connect();
    }

    @Test
    public void testFetchDocumentObjectFromWebInvalidConfig()
            throws IOException, ConfigNotFoundException {

        int timeout = 120000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);
        byte[] contents = "test".getBytes();

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("https");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("x");
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willReturn(contents);
        given(metricsHelper.getCounter(urlLoader, "fetch", "web"))
                .willReturn(counter);

        // when
        urlLoader.fetchDocumentObject(urlStr);

        assertThat(counter.getCount()).isEqualTo(1);

        verify(uc).setConnectTimeout(timeout);
        verify(uc).setReadTimeout(timeout);
    }

    @Test
    public void testFetchDocumentObjectFromWebExpectExcetpion()
            throws IOException, ConfigNotFoundException {
        int timeout = 1000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn(String.valueOf(timeout));
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode()).willReturn(HttpURLConnection.HTTP_OK);
        given(ucHelper.getContent(uc)).willThrow(IOException.class);

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(urlStr);
    }

    @Test
    public void testFetchDocumentObjectFromBadRequest()
            throws IOException, ConfigNotFoundException {
        int timeout = 1000;
        String userAgent = "IE";
        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);

        given(ucHelper.escapeUrl(urlStr)).willReturn(urlStr);
        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn(userAgent);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn(String.valueOf(timeout));
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(uc.getResponseCode())
                .willReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(urlStr);
    }

    @Test
    public void testFetchDocumentObjectFromFile()
            throws IOException, ConfigNotFoundException {
        String expected = "xyz";

        FileUtils.write(new File("/tmp/x.txt"), expected, "UTF-8");

        String url = "file:///tmp/x.txt";

        given(ucHelper.getProtocol(url)).willReturn("file");
        given(metricsHelper.getCounter(urlLoader, "fetch", "file"))
                .willReturn(counter);

        // when
        byte[] actual = urlLoader.fetchDocumentObject(url);

        assertThat(actual).isEqualTo(expected.getBytes());
        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void testFetchDocumentObjectFromFileExpectException()
            throws IOException, ConfigNotFoundException {
        String url = "file:///tmp/xtestx.txt";

        given(ucHelper.getProtocol(url)).willReturn("file");

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(url);
    }

    @Test
    public void testFetchDocumentObjectFromClasspath()
            throws IOException, ConfigNotFoundException {
        String resPath = "/testdefs/urlloader/example.html";
        given(ucHelper.getProtocol(resPath)).willReturn("resource");
        given(metricsHelper.getCounter(urlLoader, "fetch", "resource"))
                .willReturn(counter);

        // when
        byte[] actual = urlLoader.fetchDocumentObject(resPath);
        assertThat(counter.getCount()).isEqualTo(1);

        URL fileURL = URLLoader.class.getResource(resPath);
        byte[] expected = IOUtils.toByteArray(fileURL);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testFetchDocumentObjectFromClasspathExpectException()
            throws IOException, ConfigNotFoundException {
        String url = "/testdefs/urlloader/non-exist.html";

        given(ucHelper.getProtocol(url)).willReturn("resource");

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(url);
    }

    @Test
    public void testFetchDocumentObjectInvalidProtocol()
            throws IOException, ConfigNotFoundException {

        given(ucHelper.getProtocol(filePath)).willReturn("invalid");

        // when
        testRule.expect(IOException.class);
        urlLoader.fetchDocumentObject(filePath);
    }

}
