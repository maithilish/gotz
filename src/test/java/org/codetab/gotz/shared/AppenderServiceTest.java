package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.Appender.Marker;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.pool.AppenderPoolService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class AppenderServiceTest {

    @Spy
    DInjector dInjector;
    @Mock
    private AppenderPoolService appenderPoolService;
    @Mock
    private Appender appender1, appender2;

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
            IllegalAccessException, FieldNotFoundException {
        List<FieldsBase> fields = new ArrayList<>();
        Field field = new Field();
        field.setName("name");
        field.setValue("x");
        fields.add(field);

        field = new Field();
        field.setName("class");
        field.setValue("org.codetab.gotz.appender.FileAppender");
        fields.add(field);

        appenderService.createAppender("x", fields);

        Appender appender = appenderService.getAppender("x");

        assertThat(appender.getFields()).isEqualTo(fields);
        verify(appenderPoolService).submit("appender", appender);
    }

    @Test
    public void testCreateAppenderAlreadyExists()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, FieldNotFoundException {
        List<FieldsBase> fields = new ArrayList<>();
        Field field = new Field();
        field.setName("name");
        field.setValue("x");
        fields.add(field);

        field = new Field();
        field.setName("class");
        field.setValue("org.codetab.gotz.appender.FileAppender");
        fields.add(field);

        appenderService.createAppender("x", fields);

        Appender appender = appenderService.getAppender("x");

        assertThat(appender.getFields()).isEqualTo(fields);
        verify(appenderPoolService).submit("appender", appender);

        // change class name to trigger error
        ((Field) fields.get(1)).setValue("xyz");
        appenderService.createAppender("x", fields);
    }

    @Test
    public void testCreateAppenderExpectException()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, FieldNotFoundException {
        List<FieldsBase> fields = new ArrayList<>();
        Field field = new Field();
        field.setName("name");
        field.setValue("x");
        fields.add(field);

        field = new Field();
        field.setName("class");
        field.setValue("org.codetab.gotz.model.Axis");
        fields.add(field);

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
                "createAppender", String.class, List.class);
        assertThat(method).isNotNull();
        assertThat(Modifier.isSynchronized(method.getModifiers())).isTrue();
    }
}
