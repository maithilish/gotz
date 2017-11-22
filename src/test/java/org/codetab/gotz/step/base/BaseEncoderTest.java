package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.convert.CsvEncoder;
import org.codetab.gotz.util.Util;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.w3c.dom.Node;

/**
 * <p>
 * BaseEncoder Test.
 * @author Maithilish
 *
 */
public class BaseEncoderTest {

    @Mock
    private AppenderService appenderService;
    @Mock
    private ActivityService activityService;
    @Spy
    private FieldsHelper xFieldHelper;

    @InjectMocks
    private CsvEncoder encoder;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsConsistent() {
        assertThat(encoder.isConsistent()).isFalse();

        encoder.setConsistent(false);
        assertThat(encoder.isConsistent()).isFalse();

        encoder.setConsistent(true);
        assertThat(encoder.isConsistent()).isFalse();

        encoder.setConsistent(false);

        encoder.setInput(new Data());
        assertThat(encoder.isConsistent()).isFalse();

        encoder.setConsistent(true);
        assertThat(encoder.isConsistent()).isTrue();
    }

    @Test
    public void testInitialize() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, FieldsException {

        // xfield/task/steps/step[@name='encoder']/appender
        Fields fields = xFieldHelper.createXField();
        Node step = createStepNode(fields);

        Node node = xFieldHelper.addElement("appender", "", step);
        xFieldHelper.addElement("name", "x", node);
        node = xFieldHelper.addElement("appender", "", step);
        xFieldHelper.addElement("name", "y", node);

        encoder.setFields(fields);
        encoder.setStepType("encoder");

        String splitXpath =
                Util.buildString("/:xfield/:task/:steps/:step[@name='",
                        "encoder", "']/:appender");
        List<Fields> appenders = xFieldHelper.split(splitXpath, fields);

        given(xFieldHelper.split(splitXpath, fields)).willReturn(appenders);

        boolean actual = encoder.initialize();

        assertThat(actual).isTrue();
        assertThat(encoder.getStepState()).isEqualTo(StepState.INIT);

        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) FieldUtils.readField(encoder,
                "appenderNames", true);

        assertThat(names).containsExactly("x", "y");

        verify(appenderService).createAppender("x", appenders.get(0));
        verify(appenderService).createAppender("y", appenders.get(1));
        verifyNoMoreInteractions(appenderService);
    }

    @Test
    public void testInitializeEmptyFieldsShouldThrowException() {

        encoder.setFields(new Fields());

        // when
        try {
            encoder.initialize();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldsException.class));
        }

        testRule.expect(StepRunException.class);
        encoder.initialize();
    }

    @Test
    public void testInitializeCreateAppenderErrorShouldLogActivity()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, FieldsException, FieldsException {
        Fields fields = xFieldHelper.createXField();
        Node step = createStepNode(fields);
        Node node = xFieldHelper.addElement("appender", "", step);
        xFieldHelper.addElement("name", "x", node);

        encoder.setFields(fields);
        encoder.setStepType("encoder");

        // when
        try {
            doThrow(new ClassNotFoundException()).when(appenderService)
                    .createAppender("x", fields);
            encoder.initialize();
        } catch (ClassNotFoundException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(ClassNotFoundException.class));
        }

        // when
        try {
            doThrow(new InstantiationException()).when(appenderService)
                    .createAppender("x", fields);
            encoder.initialize();
        } catch (InstantiationException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(InstantiationException.class));
        }

        // when
        try {
            doThrow(new IllegalAccessException()).when(appenderService)
                    .createAppender("x", fields);
            encoder.initialize();
        } catch (IllegalAccessException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(IllegalAccessException.class));
        }

        // when
        try {
            doThrow(new FieldsException("f")).when(appenderService)
                    .createAppender("x", fields);
            encoder.initialize();
        } catch (FieldsException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldsException.class));
        }
    }

    private Node createStepNode(final Fields fields) throws FieldsException {
        // xfield/task/steps/step[@name='encoder']/appender
        Node task = xFieldHelper.addElement("task", "", fields);
        Node steps = xFieldHelper.addElement("steps", "", task);
        Node step = xFieldHelper.addElement("step", "", steps);
        xFieldHelper.addAttribute("name", "encoder", step);
        return step;
    }

    @Test
    public void testInitializeIllegalState() {
        // fields is null
        testRule.expect(IllegalStateException.class);
        encoder.initialize();
    }

    @Test
    public void testDoAppend() throws InterruptedException, FieldsException,
            IllegalAccessException {
        Appender appender1 = Mockito.mock(Appender.class);
        Appender appender2 = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(appender1);
        given(appenderService.getAppender("y")).willReturn(appender2);

        List<String> appenderNames = new ArrayList<>();
        appenderNames.add("x");
        appenderNames.add("y");
        FieldUtils.writeField(encoder, "appenderNames", appenderNames, true);

        Object obj = "xyz";
        encoder.doAppend(obj);

        verify(appender1).append(obj);
        verify(appender2).append(obj);
        verifyNoMoreInteractions(appender1, appender2);
    }

    @Test
    public void testDoAppendShouldThrowException()
            throws InterruptedException, FieldsException {
        Fields fields = xFieldHelper.createXField();
        Node step = createStepNode(fields);
        Node node = xFieldHelper.addElement("appender", "", step);
        xFieldHelper.addElement("name", "x", node);

        encoder.setFields(fields);
        encoder.setStepType("encoder");
        encoder.initialize();

        Appender appender1 = Mockito.mock(Appender.class);
        given(appenderService.getAppender("x")).willReturn(appender1);
        doThrow(InterruptedException.class).when(appender1).append("xyz");

        try {
            encoder.doAppend("xyz");
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(InterruptedException.class));
        }

        testRule.expect(StepRunException.class);
        encoder.doAppend("xyz");
    }

    @Test
    public void testSetInput() {
        Data actual = encoder.getData();
        assertThat(actual).isNull();

        encoder.setInput("some obj"); // when
        actual = encoder.getData();
        assertThat(actual).isNull();

        Data data = new Data();
        encoder.setInput(data); // when
        actual = encoder.getData();
        assertThat(actual).isSameAs(data);
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

    @Test
    public void testGetData() {
        Data data = new Data();
        encoder.setInput(data);

        // when
        Data actual = encoder.getData();

        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testLoad() {
        boolean actual = encoder.load(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testStore() {
        boolean actual = encoder.store(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testHandover() {
        boolean actual = encoder.handover(); // when
        assertThat(actual).isFalse();
    }

}
