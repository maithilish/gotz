package org.codetab.gotz.step;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StepStateTest {

    @Test
    public void test() {
        assertEquals(StepState.INIT, StepState.values()[0]);
        assertEquals(StepState.LOAD, StepState.values()[1]);
        assertEquals(StepState.PROCESS, StepState.values()[2]);
        assertEquals(StepState.STORE, StepState.values()[3]);
        assertEquals(StepState.HANDOVER, StepState.values()[4]);

        assertEquals(StepState.INIT, StepState.valueOf("INIT"));
        assertEquals(StepState.LOAD, StepState.valueOf("LOAD"));
        assertEquals(StepState.PROCESS, StepState.valueOf("PROCESS"));
        assertEquals(StepState.STORE, StepState.valueOf("STORE"));
        assertEquals(StepState.HANDOVER, StepState.valueOf("HANDOVER"));
    }
}
