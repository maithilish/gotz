package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.appender.Appender.Marker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.w3c.dom.Node;

public class AppenderServiceTest {

    @Spy
    private DInjector dInjector;
    @Mock
    private AppenderPoolService appenderPoolService;
    @Mock
    private Appender appender1, appender2;
    @Spy
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private AppenderService appenderService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateAppender()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, FieldsNotFoundException, FieldsException {

        String className = "org.codetab.gotz.step.load.appender.FileAppender";
        Fields fields = fieldsHelper.createFields();
        Node node = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addAttribute("class", className, node);

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        ConfigService configService = dInjector.instance(ConfigService.class);
        configService.init(userProvidedFile, defaultsFile);

        appenderService.createAppender("x", fields);

        Appender appender = appenderService.getAppender("x");
        appender.setFields(fields);

        assertThat(appender.getFields()).isEqualTo(fields);
        verify(appenderPoolService).submit("appender", appender);
    }

    @Test
    public void testCreateAppenderAlreadyExists() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            ConfigNotFoundException, FieldsException, FieldsNotFoundException {

        String className = "org.codetab.gotz.step.load.appender.FileAppender";
        String appenderName = "x";
        Fields fields = fieldsHelper.createFields();
        Node node = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addAttribute("name", appenderName, node);
        fieldsHelper.addAttribute("class", className, node);

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        ConfigService configService = dInjector.instance(ConfigService.class);
        configService.init(userProvidedFile, defaultsFile);

        appenderService.createAppender("x", fields);
        Appender appender = appenderService.getAppender("x");

        assertThat(appender.getFields()).isEqualTo(fields);
        verify(appenderPoolService).submit("appender", appender);

        // change class name to trigger error, but as appender with same name
        // exists it should not throw ClassCastException
        className = "org.codetab.gotz.appender.FileAppenderX";
        fields = fieldsHelper.createFields();
        node = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addAttribute("name", appenderName, node);
        fieldsHelper.addAttribute("class", className, node);

        appenderService.createAppender("x", fields);
    }

    @Test
    public void testCreateAppenderExpectException()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, FieldsException, FieldsNotFoundException {

        String className = "org.codetab.gotz.model.Axis";
        String appenderName = "x";
        Fields fields = fieldsHelper.createFields();
        Node node = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addAttribute("name", appenderName, node);
        fieldsHelper.addAttribute("class", className, node);

        exceptionRule.expect(ClassCastException.class);
        appenderService.createAppender("x", fields);
    }

    @Test
    public void testCloseAll()
            throws IllegalAccessException, InterruptedException {
        Map<String, Appender> appenders = new HashMap<String, Appender>();
        appenders.put("x", appender1);
        appenders.put("y", appender2);

        FieldUtils.writeDeclaredField(appenderService, "appenders", appenders,
                true);

        appenderService.closeAll();

        InOrder inOrder = inOrder(appender1, appender2);
        inOrder.verify(appender1).append(Marker.EOF);
        inOrder.verify(appender2).append(Marker.EOF);
    }

    @Test
    public void testClose()
            throws IllegalAccessException, InterruptedException {
        Map<String, Appender> appenders = new HashMap<String, Appender>();
        appenders.put("x", appender1);
        appenders.put("y", appender2);

        FieldUtils.writeDeclaredField(appenderService, "appenders", appenders,
                true);

        appenderService.close("x");

        verify(appender1).append(Marker.EOF);
    }

    @Test
    public void testCreateAppenderSynchronized() {
        Method method = MethodUtils.getMatchingMethod(AppenderService.class,
                "createAppender", String.class, Fields.class);
        assertThat(method).isNotNull();
        assertThat(Modifier.isSynchronized(method.getModifiers())).isTrue();
    }
}
