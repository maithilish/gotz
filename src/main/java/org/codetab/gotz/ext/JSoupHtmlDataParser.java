package org.codetab.gotz.ext;

import org.codetab.gotz.step.IStepO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlDataParser extends JSoupHtmlParser {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlDataParser.class);

    @Override
    public IStepO instance() {
        return this;
    }

}
