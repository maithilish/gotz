package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.step.IStep;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class StepServiceTest {

    @Spy
    private DInjector dInjector;
    @InjectMocks
    private StepService stepService;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetStep() throws ClassNotFoundException,
    InstantiationException, IllegalAccessException {
        // given
        String clzName = "org.codetab.gotz.ext.HtmlLoader";
        Class<?> stepClass = Class.forName(clzName);

        // when
        IStep step = stepService.getStep(clzName);

        // then
        verify(dInjector).instance(stepClass);
        assertThat(step).isInstanceOf(IStep.class);
        assertThat(step.getClass()).isEqualTo(stepClass);
    }

    @Test
    public void testGetStepClassCastException() throws ClassNotFoundException,
    InstantiationException, IllegalAccessException {
        // given
        String clzName = "org.codetab.gotz.model.Locator";

        expected.expect(ClassCastException.class);

        // when
        stepService.getStep(clzName);
    }

    @Test
    public void testGetStepClassNotFoundException()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        // given
        String clzName = "org.codetab.gotz.XYZ";

        expected.expect(ClassNotFoundException.class);

        // when
        stepService.getStep(clzName);
    }

}
