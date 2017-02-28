package in.m.picks.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.step.IStep;

public class HtmlDataParser extends HtmlParser {

	final static Logger logger = LoggerFactory.getLogger(HtmlDataParser.class);

	@Override
	public IStep instance() {
		return new HtmlDataParser();
	}

}
