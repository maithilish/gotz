package org.codetab.gotz.step.load;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.encoder.IEncoder;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DataAppenderTest {

    @Mock
    private StepService stepService;
    @Mock
    private FieldsHelper fieldsHelper;
    @Mock
    private AppenderService appenderService;
    @Mock
    private ActivityService activityService;

    @InjectMocks
    private DataAppender dataAppender;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private Data data;
    private Labels labels;
    private Map<String, Fields> appenderFieldsMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        dataAppender.setStepType("appender");

        labels = new Labels("x", "y");
        dataAppender.setLabels(labels);

        data = new Data();
        dataAppender.setInput(data);

        dataAppender.setFields(TestUtil.createEmptyFields());

        appenderFieldsMap = createAppenderFieldsMap();
        FieldUtils.writeField(dataAppender, "appenderFieldsMap",
                appenderFieldsMap, true);
    }

    @Test
    public void testInstance() {
        dataAppender = new DataAppender();

        assertThat(dataAppender.isConsistent()).isFalse();
        assertThat(dataAppender.getStepType()).isNull();
        assertThat(dataAppender.instance()).isInstanceOf(DataAppender.class);
        assertThat(dataAppender.instance()).isSameAs(dataAppender.instance());
    }

    @Test
    public void testProcess() throws Exception {

        Fields appenderFields1 = appenderFieldsMap.get("x");
        Fields appenderFields2 = appenderFieldsMap.get("y");

        Appender appender1 = Mockito.mock(Appender.class);
        Appender appender2 = Mockito.mock(Appender.class);

        Fields encoderFields1 = TestUtil.createEmptyFields();
        encoderFields1.setName("x");
        List<Fields> encoders1 = new ArrayList<>();
        encoders1.add(encoderFields1);
        String className1 = "x";

        Fields encoderFields2 = TestUtil.createEmptyFields();
        encoderFields2.setName("y");
        List<Fields> encoders2 = new ArrayList<>();
        encoders2.add(encoderFields2);
        String className2 = "y";

        @SuppressWarnings("rawtypes")
        IEncoder encoder1 = Mockito.mock(IEncoder.class);
        @SuppressWarnings("rawtypes")
        IEncoder encoder2 = Mockito.mock(IEncoder.class);

        String encodedData1 = "x";
        String encodedData2 = "y";

        given(appenderService.getAppender("x")).willReturn(appender1);
        given(appenderService.getAppender("y")).willReturn(appender2);

        given(fieldsHelper.split("/xf:fields/xf:appender/xf:encoder",
                appenderFields1)).willReturn(encoders1);
        given(fieldsHelper.split("/xf:fields/xf:appender/xf:encoder",
                appenderFields2)).willReturn(encoders2);

        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/@class",
                encoderFields1)).willReturn(className1);
        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/@class",
                encoderFields2)).willReturn(className2);

        given(stepService.createInstance(className1)).willReturn(encoder1);
        given(stepService.createInstance(className2)).willReturn(encoder2);

        given(encoder1.encode(data)).willReturn(encodedData1);
        given(encoder2.encode(data)).willReturn(encodedData2);

        dataAppender.process();

        verify(appender1).append(encodedData1);
        verify(appender2).append(encodedData2);
    }

    @Test
    public void testProcessCollection() throws Exception {

        appenderFieldsMap.remove("y");

        Fields appenderFields1 = appenderFieldsMap.get("x");

        Appender appender1 = Mockito.mock(Appender.class);

        Fields encoderFields1 = TestUtil.createEmptyFields();
        encoderFields1.setName("x");
        List<Fields> encoders1 = new ArrayList<>();
        encoders1.add(encoderFields1);
        String className1 = "x";

        @SuppressWarnings("rawtypes")
        IEncoder encoder1 = Mockito.mock(IEncoder.class);

        List<String> encodedData1 = Arrays.asList("a", "b", "c");

        given(appenderService.getAppender("x")).willReturn(appender1);

        given(fieldsHelper.split("/xf:fields/xf:appender/xf:encoder",
                appenderFields1)).willReturn(encoders1);

        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/@class",
                encoderFields1)).willReturn(className1);

        given(stepService.createInstance(className1)).willReturn(encoder1);

        given(encoder1.encode(data)).willReturn(encodedData1);

        // stream - false
        given(fieldsHelper.getValue("/xf:fields/xf:appender/@stream",
                appenderFields1)).willReturn("false");

        dataAppender.process();

        verify(appender1).append(encodedData1);
        verifyNoMoreInteractions(appender1);

        // stream - true
        given(fieldsHelper.getValue("/xf:fields/xf:appender/@stream",
                appenderFields1)).willReturn("true");

        dataAppender.process();

        verify(appender1).append(encodedData1.get(0));
        verify(appender1).append(encodedData1.get(1));
        verify(appender1).append(encodedData1.get(2));
        verifyNoMoreInteractions(appender1);
    }

    @Test
    public void testProcessThrowsException() throws Exception {

        appenderFieldsMap.remove("y");

        given(appenderService.getAppender("x")).willThrow(Exception.class);

        dataAppender.process();

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(Exception.class));
    }

    private Map<String, Fields> createAppenderFieldsMap() {
        Map<String, Fields> map = new HashMap<>();

        Fields fields = TestUtil.createEmptyFields();
        fields.setName("x");
        map.put("x", fields);

        fields = TestUtil.createEmptyFields();
        fields.setName("y");
        map.put("y", fields);

        return map;
    }
}
