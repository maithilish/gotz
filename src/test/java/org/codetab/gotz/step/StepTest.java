package org.codetab.gotz.step;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.step.extract.LocatorSeeder;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

public class StepTest {

    @InjectMocks
    private LocatorSeeder step;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetFields() {
        Fields fields = TestUtil.createEmptyFields();
        step.setFields(fields);

        assertThat(step.getFields()).isSameAs(fields);
    }

    @Test
    public void testIsConsistent() {
        assertThat(step.isConsistent()).isFalse();
        step.setConsistent(true);
        assertThat(step.isConsistent()).isTrue();
    }

    @Test
    public void testGetStepType() {
        step.setStepType("x");
        assertThat(step.getStepType()).isEqualTo("x");
    }

    @Test
    public void testGetStepState() {
        step.setStepState(StepState.HANDOVER);
        assertThat(step.getStepState()).isEqualTo(StepState.HANDOVER);
    }

    @Test
    public void testGetLabels() {
        Labels labels = new Labels("n", "g");
        step.setLabels(labels);
        assertThat(step.getLabels()).isEqualTo(labels);
    }

    @Test
    public void testGetMarker() {
        Labels labels = new Labels("n", "g");
        step.setLabels(labels);

        Marker actual = step.getMarker();

        assertThat(actual.toString()).isEqualTo("LOG_N_G_NA");
    }

    @Test
    public void testGetLabel() {
        Labels labels = new Labels("n", "g");
        step.setLabels(labels);

        assertThat(step.getLabel()).isEqualTo("n:g:na");
    }

    @Test
    public void testGetLabeled() {
        Labels labels = new Labels("n", "g");
        step.setLabels(labels);

        assertThat(step.getLabeled("x")).isEqualTo("[n:g:na] x");
    }

}
