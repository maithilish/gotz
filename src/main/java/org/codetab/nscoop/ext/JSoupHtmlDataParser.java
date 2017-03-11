package org.codetab.nscoop.ext;

import org.codetab.nscoop.step.IStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlDataParser extends JSoupHtmlParser {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlDataParser.class);

    @Override
    public IStep instance() {
        return new JSoupHtmlDataParser();
    }

}
