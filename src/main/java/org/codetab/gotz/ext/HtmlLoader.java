package org.codetab.gotz.ext;

import java.io.IOException;
import java.net.MalformedURLException;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public final class HtmlLoader extends Loader {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlLoader.class);
    private static final int TIMEOUT_MILLIS = 120000;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public Object fetchDocument(final String url)
            throws Exception, MalformedURLException, IOException {
        WebClient webClient = getWebClient();
        LOGGER.info("fetch web resource {}", url);
        try {
            HtmlPage htmlPage = webClient.getPage(url);
            LOGGER.debug("fetched web resource {}", url);
            return htmlPage;
        } finally {
            webClient.setRefreshHandler(new ImmediateRefreshHandler());
            webClient.close();
        }
    }

    private WebClient getWebClient() {
        int timeout = TIMEOUT_MILLIS;
        String key = "gotz.webClient.timeout";
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            // TODO add activity or update config with default
            String msg = "for config [" + key + "] using default value " + timeout;
            LOGGER.warn("{}. {}", e, msg);
        }

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.setRefreshHandler(new ThreadedRefreshHandler());

        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setTimeout(timeout);
        return webClient;
    }
}
