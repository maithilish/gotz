package in.m.picks.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.step.IStep;

public final class JSoupHtmlDataParser extends JSoupHtmlParser {

    static final Logger LOGGER = LoggerFactory
            .getLogger(JSoupHtmlDataParser.class);

    @Override
    public IStep instance() {
        return new JSoupHtmlDataParser();
    }

}
