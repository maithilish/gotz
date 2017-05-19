package org.codetab.gotz.ext;

import org.codetab.gotz.step.IStepO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HtmlDataParser extends HtmlParser {

    static final Logger LOGGER = LoggerFactory.getLogger(HtmlDataParser.class);

    @Override
    public IStepO instance() {
        return new HtmlDataParser();
    }

}
