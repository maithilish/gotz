package org.codetab.gotz.stepbase;

import org.codetab.gotz.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSeeder extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseSeeder.class);

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean store() {
        return false;
    }

}
