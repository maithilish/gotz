package org.codetab.gotz.stepbase;

import org.codetab.gotz.step.Step;

/**
 * <p>
 * Base seeder step.
 * @author Maithilish
 *
 */
public abstract class BaseSeeder extends Step {

    /**
     * <p>
     * do nothing.
     * @return false
     */
    @Override
    public boolean load() {
        return false;
    }

    /**
     * <p>
     * do nothing.
     * @return false
     */
    @Override
    public boolean store() {
        return false;
    }

}
