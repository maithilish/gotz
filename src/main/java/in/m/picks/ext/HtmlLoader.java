package in.m.picks.ext;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.shared.ConfigService;
import in.m.picks.step.IStep;
import in.m.picks.step.Loader;

public class HtmlLoader extends Loader {

	final static Logger logger = LoggerFactory.getLogger(HtmlLoader.class);

	@Override
	public IStep instance() {
		return new HtmlLoader();
	}

	@Override
	public Object fetchDocument(String url)
			throws Exception, MalformedURLException, IOException {
		WebClient webClient = getWebClient();
		logger.debug("fetch web resource {}", url);
		try {
			HtmlPage htmlPage = webClient.getPage(url);
			logger.debug("fetched web resource {}", url);			
			return htmlPage;
		} finally {
			webClient.setRefreshHandler(new ImmediateRefreshHandler());
			webClient.close();
		}
	}

	private WebClient getWebClient() {
		int timeout = 120000;
		String key = "picks.webClientTimeout";
		try {
			timeout = Integer.parseInt(ConfigService.INSTANCE.getConfig(key));
		} catch (NumberFormatException e) {
			// TODO add activity or update config with default
			String msg = "for config [" + key + "] using default value " + timeout;
			logger.warn("{}. {}", e, msg);
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
