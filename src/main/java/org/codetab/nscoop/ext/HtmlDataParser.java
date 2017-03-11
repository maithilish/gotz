package org.codetab.nscoop.ext;

import org.codetab.nscoop.step.IStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlDataParser extends HtmlParser {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlDataParser.class);

    @Override
    public IStep instance() {
        return new HtmlDataParser();
    }

}
