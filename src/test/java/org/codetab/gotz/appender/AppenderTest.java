package org.codetab.gotz.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.w3c.dom.Node;

/**
 * <p>
 * Appender tests.
 * @author Maithilish
 *
 */
public class AppenderTest {

    @Mock
    private ConfigService configService;
    @Mock
    private ActivityService activityService;
    @Spy
    private FieldsHelper xFieldHelper;

    @InjectMocks
    private ListAppender appender;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitializeQueueDefaultSize()
            throws FieldsException, ConfigNotFoundException {
        given(configService.getConfig("gotz.appender.queuesize"))
                .willThrow(ConfigNotFoundException.class);

        Fields fields = xFieldHelper.createXField();
        appender.setFields(fields);

        appender.initializeQueue();

        assertThat(appender.getQueue()).isNotNull();
        assertThat(appender.getQueue().remainingCapacity()).isEqualTo(4096);
    }

    @Test
    public void testInitializeQueueDefaultSizeNoGlobalConfig()
            throws ConfigNotFoundException, FieldsException {
        given(configService.getConfig("gotz.appender.queuesize"))
                .willThrow(ConfigNotFoundException.class);

        Fields fields = xFieldHelper.createXField();
        appender.setFields(fields);

        appender.initializeQueue();

        assertThat(appender.getQueue()).isNotNull();
        assertThat(appender.getQueue().remainingCapacity()).isEqualTo(4096);
    }

    @Test
    public void testInitializeQueueSizeFromGlobalConfig()
            throws ConfigNotFoundException, FieldsException {
        given(configService.getConfig("gotz.appender.queuesize"))
                .willReturn("10240");

        Fields fields = xFieldHelper.createXField();
        appender.setFields(fields);

        appender.initializeQueue();

        assertThat(appender.getQueue()).isNotNull();
        assertThat(appender.getQueue().remainingCapacity()).isEqualTo(10240);
    }

    @Test
    public void testInitializeQueueSizeFromAppenderField()
            throws ConfigNotFoundException, FieldsException {

        given(configService.getConfig("gotz.appender.queuesize"))
                .willReturn("10240");

        Fields fields = xFieldHelper.createXField();
        Node parent = xFieldHelper.addElement("appender", "", fields);
        xFieldHelper.addElement("queueSize", "2048", parent);

        appender.setFields(fields);

        appender.initializeQueue();

        assertThat(appender.getQueue()).isNotNull();
        assertThat(appender.getQueue().remainingCapacity()).isEqualTo(2048);
    }

    @Test
    public void testInitializeQueueInvalidSize()
            throws ConfigNotFoundException, FieldsException {

        given(configService.getConfig("gotz.appender.queuesize"))
                .willReturn("x");

        Fields fields = xFieldHelper.createXField();
        appender.setFields(fields);
        appender.initializeQueue();

        verify(activityService).addActivity(eq(Type.GIVENUP), any(String.class),
                any(NumberFormatException.class));
    }

    @Test
    public void testInitializeQueueIllegalState()
            throws IllegalAccessException {

        FieldUtils.writeField(appender, "activityService", null, true);

        try {
            appender.initializeQueue();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("activityService is null");
        }

        FieldUtils.writeField(appender, "configService", null, true);
        try {
            appender.initializeQueue();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("configService is null");
        }
    }

    @Test
    public void testGetQueue() throws IllegalAccessException {

        BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1024);
        FieldUtils.writeField(appender, "queue", queue, true);

        BlockingQueue<Object> actual = appender.getQueue();

        assertThat(actual).isSameAs(queue);
    }

    @Test
    public void testSetGetXField() throws FieldsException {

        Fields fields = xFieldHelper.createXField();
        appender.setFields(fields);

        Fields actual = appender.getFields();

        assertThat(actual).isSameAs(fields);
    }

    @Test
    public void testSetXFieldNullParams() {
        try {
            appender.setFields(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }
    }

    @Test
    public void testSetName() throws IllegalAccessException {

        appender.setName("x");

        String actual = (String) FieldUtils.readField(appender, "name", true);

        assertThat(actual).isEqualTo("x");
    }

    @Test
    public void testSetNameNullParams() {
        try {
            appender.setName(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("appenderName must not be null");
        }
    }

}
