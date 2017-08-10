package org.codetab.gotz.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * PassThroughEncoder tests.
 * @author Maithilish
 *
 */
public class PassThroughEncoderTest {

    @Mock
    private AppenderService appenderService;

    @InjectMocks
    private PassThroughEncoder encoder;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() {
        assertThat(encoder.isConsistent()).isFalse();
        assertThat(encoder.getStepType()).isNull();
        assertThat(encoder.instance()).isInstanceOf(PassThroughEncoder.class);
        assertThat(encoder.instance()).isSameAs(encoder.instance());
    }

    @Test
    public void testProcess() throws InterruptedException {
        FieldsBase a1 = TestUtil.createField("appender", "x");
        FieldsBase a2 = TestUtil.createField("appender", "y");
        encoder.setFields(new ArrayList<>());
        encoder.getFields().add(a1);
        encoder.getFields().add(a2);

        String obj = "xyz";
        encoder.setInput(obj);

        encoder.initialize();

        Appender appender1 = Mockito.mock(Appender.class);
        Appender appender2 = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(appender1);
        given(appenderService.getAppender("y")).willReturn(appender2);

        encoder.process();

        verify(appender1).append(obj);
        verify(appender2).append(obj);
        verifyNoMoreInteractions(appender1, appender2);
    }

    @Test
    public void testProcessIllegalState() throws IllegalAccessException {
        try {
            // mockito injects mocks to obj field so reset it to null
            FieldUtils.writeDeclaredField(encoder, "obj", null, true);
            encoder.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("input must not be null");
        }
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        String obj = "xyz";
        encoder.setInput(obj);

        Object actual = FieldUtils.readDeclaredField(encoder, "obj", true);

        assertThat(actual).isEqualTo(obj);
    }

    @Test
    public void testSetInputShouldThrowException() {
        try {
            encoder.setInput(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("input must not be null");
        }
    }

}
