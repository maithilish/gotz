package in.m.picks.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.step.IStep;

public class JSoupHtmlDataParser extends JSoupHtmlParser {

	final static Logger logger = LoggerFactory.getLogger(JSoupHtmlDataParser.class);

	@Override
	public IStep instance() {
		return new JSoupHtmlDataParser();
	}

}
