package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.load.DataAppender;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.encoder.CsvEncoder;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseAppenderTest {

    @Mock
    private StepService stepService;
    @Mock
    private DInjector dInjector;

    @Mock
    private AppenderService appenderService;
    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;
    @Mock
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private DataAppender appender;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private Labels labels;
    private Data data;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        appender.setStepType("appender");
        appender.setFields(TestUtil.createEmptyFields());

        labels = new Labels("x", "y");
        appender.setLabels(labels);

        data = new Data();
        appender.setInput(data);
    }

    @Test
    public void testInitialize() throws FieldsException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            FieldsNotFoundException {

        String xpath =
                "/xf:fields/xf:task/xf:steps/xf:step[@name='appender']/xf:appender";
        List<Fields> appenders = createTestAppenders();
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willReturn(appenders);
        xpath = "/xf:fields/xf:appender/@name";
        given(fieldsHelper.getLastValue(xpath, appenders.get(0)))
                .willReturn("x");
        given(fieldsHelper.getLastValue(xpath, appenders.get(1)))
                .willReturn("y");

        appender.initialize();

        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) FieldUtils.readField(appender,
                "appenderNames", true);
        assertThat(names).containsExactly("x", "y");

        Map<String, Fields> map = appender.getAppenderFieldsMap();
        assertThat(map).hasSize(2);
        assertThat(map.get("x")).isEqualTo(appenders.get(0));
        assertThat(map.get("y")).isEqualTo(appenders.get(1));

        verify(appenderService).createAppender("x", appenders.get(0));
        verify(appenderService).createAppender("y", appenders.get(1));
    }

    @Test
    public void testInitializeCreateAppenderThrowsException()
            throws FieldsException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            FieldsNotFoundException {

        List<Fields> appenders = createTestAppenders();
        appenders.remove(1);

        String xpath =
                "/xf:fields/xf:task/xf:steps/xf:step[@name='appender']/xf:appender";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willReturn(appenders);
        xpath = "/xf:fields/xf:appender/@name";
        given(fieldsHelper.getLastValue(xpath, appenders.get(0)))
                .willReturn("x");

        doThrow(FieldsNotFoundException.class)
                .doThrow(ClassNotFoundException.class)
                .doThrow(InstantiationException.class)
                .doThrow(IllegalAccessException.class).when(appenderService)
                .createAppender("x", appenders.get(0));

        appender.initialize();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(FieldsNotFoundException.class));

        appender.initialize();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(ClassNotFoundException.class));

        appender.initialize();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(InstantiationException.class));

        appender.initialize();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(IllegalAccessException.class));
    }

    @Test
    public void testInitializeSplitFieldsThrowsException()
            throws FieldsException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            FieldsNotFoundException {

        String xpath =
                "/xf:fields/xf:task/xf:steps/xf:step[@name='appender']/xf:appender";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willThrow(FieldsException.class);

        testRule.expect(StepRunException.class);
        appender.initialize();
    }

    @Test
    public void testInitializeIllegalState() throws IllegalAccessException {
        appender.setFields(null);

        testRule.expect(IllegalStateException.class);
        appender.initialize();
    }

    @Test
    public void testDoAppend() throws InterruptedException {
        Appender mockAppender = Mockito.mock(Appender.class);
        String obj = "xyz";

        appender.doAppend(mockAppender, obj);

        verify(mockAppender).append(obj);
    }

    @Test
    public void testGetAppender()
            throws FieldsException, FieldsNotFoundException {

        Appender mockAppender = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(mockAppender);

        Appender actual = appender.getAppender("x");

        assertThat(actual).isSameAs(mockAppender);
    }

    @Test
    public void testGetAppenderThrowsException()
            throws FieldsException, FieldsNotFoundException {

        given(appenderService.getAppender("x")).willReturn(null);

        testRule.expect(NullPointerException.class);
        appender.getAppender("x");

    }

    @Test
    public void testEncode() throws Exception {
        String encoderClass = "org.codetab.gotz.step.load.encoder.CsvEncoder";

        Fields encoder = TestUtil.createEmptyFields();
        List<Fields> encoders = new ArrayList<>();
        encoders.add(encoder);

        List<String> encodedObj = new ArrayList<>();

        CsvEncoder mockEncoder = Mockito.mock(CsvEncoder.class);

        String xpath = "/xf:fields/xf:appender/xf:encoder";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willReturn(encoders);
        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/@class",
                encoder)).willReturn(encoderClass);
        given(stepService.createInstance(encoderClass)).willReturn(mockEncoder);
        given(mockEncoder.encode(appender.getData())).willReturn(encodedObj);

        Object actual = appender.encode("x", appender.getFields());

        verify(mockEncoder).setFields(encoder);
        verify(mockEncoder).setLabels(appender.getLabels());
        verify(mockEncoder).encode(appender.getData());
        verifyNoMoreInteractions(mockEncoder);

        assertThat(actual).isSameAs(encodedObj);
    }

    @Test
    public void testEncodeSplitEncoderThrowsException() throws Exception {

        String xpath = "/xf:fields/xf:appender/xf:encoder";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willThrow(FieldsException.class);

        testRule.expect(FieldsException.class);
        appender.encode("x", appender.getFields());
    }

    @Test
    public void testEncodeNoEncoderThrowsException() throws Exception {

        List<Fields> encoders = new ArrayList<>();
        encoders.add(TestUtil.createEmptyFields());
        encoders.add(TestUtil.createEmptyFields());

        String xpath = "/xf:fields/xf:appender/xf:encoder";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willReturn(encoders);

        testRule.expect(FieldsException.class);
        appender.encode("x", appender.getFields());
    }

    @Test
    public void testEncodeMultipleEncoderThrowsException() throws Exception {

        List<Fields> encoders = new ArrayList<>();

        String xpath = "/xf:fields/xf:appender/xf:encoder";
        given(fieldsHelper.split(xpath, appender.getFields()))
                .willReturn(encoders);

        testRule.expect(FieldsException.class);
        appender.encode("x", appender.getFields());
    }

    @Test
    public void testIsConsistent() throws IllegalAccessException {
        boolean actual = appender.isConsistent();
        assertThat(actual).isFalse();

        appender.setConsistent(true);

        FieldUtils.writeField(appender, "data", null, true);
        actual = appender.isConsistent();
        assertThat(actual).isFalse();

        FieldUtils.writeField(appender, "data", new Data(), true);

        actual = appender.isConsistent();
        assertThat(actual).isTrue();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        Data expected = new Data();
        appender.setInput(expected);
        Data actual = (Data) FieldUtils.readField(appender, "data", true);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    public void testSetInputShouldThrowException()
            throws IllegalAccessException {
        FieldUtils.writeField(appender, "data", null, true);
        testRule.expect(StepRunException.class);
        appender.setInput("xyz");
    }

    @Test
    public void testGetData() {
        assertThat(appender.getData()).isSameAs(data);
    }

    @Test
    public void testGetEncodedData() {
        assertThat(appender.getEncodedData()).isSameAs(data);
    }

    @Test
    public void testLoad() {
        assertThat(appender.load()).isFalse();
    }

    @Test
    public void testStore() {
        assertThat(appender.store()).isFalse();
    }

    @Test
    public void testHandover() {
        assertThat(appender.handover()).isFalse();
    }

    private List<Fields> createTestAppenders() {

        //@formatter:off
        Fields x = new XOBuilder<Fields>()
          .add("      <xf:appender name='x' class='xc'>")
          .add("        <xf:encoder name='xe' class='xec' />")
          .add("      </xf:appender>")
          .buildFields();
        //@formatter:on

        //@formatter:off
        Fields y = new XOBuilder<Fields>()
          .add("      <xf:appender name='y' class='yc'>")
          .add("        <xf:encoder name='ye' class='yec' />")
          .add("      </xf:appender>")
          .buildFields();
        //@formatter:on

        List<Fields> appenders = new ArrayList<>();
        appenders.add(x);
        appenders.add(y);

        return appenders;
    }
}
