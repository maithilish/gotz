package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.shared.ConfigService;
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
    private URLConnectionHelper ucHelper;
    @Mock
    private ConfigService configService;

    @InjectMocks
    private URLLoader urlLoader;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private String urlStr;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        urlStr = "http://example.org";
        filePath = "target/test-classes/testdefs/urlloader/example.html";
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
        URL url = new URL(urlStr);
        URLConnection uc = url.openConnection();

        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("1000");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn("x");

        // when
        byte[] actual = urlLoader.fetchDocumentObject(urlStr);

        byte[] expected =
                IOUtils.toByteArray(new URL(new URL("file:"), filePath));

        assertThat(actual).isEqualTo(expected);

        assertThat(uc.getConnectTimeout()).isEqualTo(1000);
        assertThat(uc.getReadTimeout()).isEqualTo(1000);
        verify(ucHelper).setRequestProperty(uc, "User-Agent", "x");
    }

    @Test
    public void testFetchDocumentObjectFromWebHttps()
            throws IOException, ConfigNotFoundException {
        URL url = new URL(urlStr);
        URLConnection uc = url.openConnection();

        given(ucHelper.getProtocol(urlStr)).willReturn("https");
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("1000");
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willReturn("x");

        // when
        byte[] actual = urlLoader.fetchDocumentObject(urlStr);

        byte[] expected =
                IOUtils.toByteArray(new URL(new URL("file:"), filePath));

        assertThat(actual).isEqualTo(expected);

        assertThat(uc.getConnectTimeout()).isEqualTo(1000);
        assertThat(uc.getReadTimeout()).isEqualTo(1000);
        verify(ucHelper).setRequestProperty(uc, "User-Agent", "x");
    }

    // @Test
    // public void testFetchDocumentObjectFromWebUrlWithSpace()
    // throws IOException, ConfigNotFoundException {
    // String url = "http://localhost/with space";
    // String urlEscaped = "http://localhost/with%20space";
    // String standinLocalUrl = "test-classes/testdefs/urlloader/example.html";
    //
    // URL localUrl = new URL(new URL(url), standinLocalUrl);
    // URLConnection localUc = localUrl.openConnection();
    //
    // given(ucHelper.getProtocol(url)).willReturn("http");
    // given(ucHelper.getURLConnection(urlEscaped)).willReturn(localUc);
    // given(configService.getConfig("gotz.webClient.timeout"))
    // .willReturn("1000");
    // given(configService.getConfig("gotz.webClient.userAgent"))
    // .willReturn("x");
    //
    // // when
    // byte[] actual = urlLoader.fetchDocumentObject(url);
    //
    // byte[] expected = IOUtils.toByteArray(
    // new URL(new URL("file:"), "target/" + standinLocalUrl));
    //
    // assertThat(actual).isEqualTo(expected);
    //
    // assertThat(localUc.getConnectTimeout()).isEqualTo(1000);
    // assertThat(localUc.getReadTimeout()).isEqualTo(1000);
    // verify(ucHelper).setRequestProperty(localUc, "User-Agent", "x");
    // }

    @Test
    public void testFetchDocumentObjectFromWebDefaultConfigs()
            throws IOException, ConfigNotFoundException {
        URL url = new URL(urlStr);
        URLConnection localUc = url.openConnection();

        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(ucHelper.getURLConnection(urlStr)).willReturn(localUc);
        given(configService.getConfig("gotz.webClient.userAgent"))
                .willThrow(ConfigNotFoundException.class);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willThrow(ConfigNotFoundException.class);

        // when
        urlLoader.fetchDocumentObject(urlStr);

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
        URL url = new URL(urlStr);
        URLConnection uc = url.openConnection();

        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);
        given(configService.getConfig("gotz.webClient.timeout"))
                .willReturn("x");

        // when
        urlLoader.fetchDocumentObject(urlStr);

        int defaultTimeout = 120000;

        assertThat(uc.getConnectTimeout()).isEqualTo(defaultTimeout);
        assertThat(uc.getReadTimeout()).isEqualTo(defaultTimeout);
    }

    @Test
    public void testFetchDocumentObjectFromWebExpectExcetpion()
            throws IOException {
        URL url = new URL(urlStr + "x");
        URLConnection uc = url.openConnection();

        given(ucHelper.getProtocol(urlStr)).willReturn("http");
        given(ucHelper.getURLConnection(urlStr)).willReturn(uc);

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

        // when
        byte[] actual = urlLoader.fetchDocumentObject(url);

        assertThat(actual).isEqualTo(expected.getBytes());
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

        // when
        byte[] actual = urlLoader.fetchDocumentObject(resPath);

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
