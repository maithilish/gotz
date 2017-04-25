package org.codetab.gotz.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codetab.gotz.step.IStep;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StepServiceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testStepService() {
        StepService ss1 = StepService.INSTANCE;
        StepService ss2 = StepService.INSTANCE;
        assertEquals(ss1, ss2);
    }

    @Test
    public void testGetStep() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        StepService stepService = StepService.INSTANCE;
        IStep step = stepService.getStep("org.codetab.gotz.ext.HtmlLoader");
        assertTrue(step instanceof IStep);
    }

    @Test
    public void testGetStepForNonStepType() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        StepService stepService = StepService.INSTANCE;

        exception.expect(ClassCastException.class);
        IStep step = stepService.getStep("java.lang.String");
    }

    @Test
    public void testEnum() {
        StepService instance = StepService.valueOf(StepService.INSTANCE.toString());
        assertEquals(StepService.INSTANCE, instance);
    }

}
