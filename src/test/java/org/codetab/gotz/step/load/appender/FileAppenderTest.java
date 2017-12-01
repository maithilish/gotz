package org.codetab.gotz.step.load.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.step.load.appender.Appender.Marker;
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
 * FileAppender tests.
 * @author Maithilish
 *
 */
public class FileAppenderTest {

    @Mock
    private ConfigService configService;
    @Mock
    private ActivityService activityService;
    @Spy
    private IOHelper ioHelper;
    @Spy
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private FileAppender appender;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAppend() throws InterruptedException,
            ConfigNotFoundException, FieldsException {

        String str = "test object";

        appender.setFields(new Fields());

        appender.initializeQueue();

        appender.append(str);

        assertThat(appender.getQueue().take()).isEqualTo(str);
    }

    @Test
    public void testAppendNullParams() throws InterruptedException {
        try {
            appender.append(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("object must not be null");
        }
    }

    @Test
    public void testRunFileFieldNotSet() throws InterruptedException,
            FieldsException, ConfigNotFoundException {

        String str1 = "test1";

        appender.setFields(new Fields());

        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append(str1);
        appender.append(Marker.EOF);

        t.join();

        verify(activityService).addActivity(eq(Type.GIVENUP), any(String.class),
                any(FieldsException.class));
    }

    @Test
    public void testRun() throws InterruptedException, IOException,
            FieldsException, ConfigNotFoundException {

        String fileName = "target/test.txt";

        String str1 = "test1";
        String str2 = "test2";

        Fields fields = fieldsHelper.createFields();
        Node parent = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addElement("file", fileName, parent);
        appender.setFields(fields);

        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append(str1);
        appender.append(str2);
        appender.append(Marker.EOF);

        t.join();

        String br = System.lineSeparator();
        String expected = str1 + br + str2 + br;

        File file = FileUtils.getFile(fileName);
        String actual = FileUtils.readFileToString(file, "UTF-8");
        FileUtils.forceDelete(file);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testRunWriterClose() throws InterruptedException, IOException,
            FieldsException, ConfigNotFoundException {

        String fileName = "target/test.txt";

        String str1 = "test1";

        Fields fields = fieldsHelper.createFields();
        Node parent = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addElement("file", fileName, parent);
        appender.setFields(fields);

        appender.initializeQueue();

        PrintWriter writer = Mockito.mock(PrintWriter.class);
        given(ioHelper.getPrintWriter(fileName)).willReturn(writer);
        doThrow(IOException.class).when(writer).println(str1);

        Thread t = new Thread(appender);
        t.start();

        appender.append(str1);
        appender.append(Marker.EOF);

        t.join();

        verify(writer).close();

    }

    @Test
    public void testRunShouldLogAcivityOnInterruptedException()
            throws InterruptedException, IllegalAccessException, IOException,
            FieldsException {
        String fileName = "target/test.txt";

        Fields fields = fieldsHelper.createFields();
        Node parent = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addElement("file", fileName, parent);
        appender.setFields(fields);

        @SuppressWarnings("unchecked")
        BlockingQueue<Object> queue = Mockito.mock(BlockingQueue.class);
        FieldUtils.writeField(appender, "queue", queue, true);

        given(queue.take()).willThrow(InterruptedException.class)
                .willReturn(Marker.EOF);

        appender.run();

        verify(activityService).addActivity(eq(Type.GIVENUP), any(String.class),
                any(InterruptedException.class));
    }

    @Test
    public void testRunShouldLogAcivityOnIOException()
            throws InterruptedException, IllegalAccessException, IOException,
            FieldsException {
        String fileName = "/home/xyzz/test.txt";

        Fields fields = fieldsHelper.createFields();
        Node parent = fieldsHelper.addElement("appender", "", fields);
        fieldsHelper.addElement("file", fileName, parent);
        appender.setFields(fields);

        @SuppressWarnings("unchecked")
        BlockingQueue<Object> queue = Mockito.mock(BlockingQueue.class);
        FieldUtils.writeField(appender, "queue", queue, true);

        appender.run();

        verify(activityService).addActivity(eq(Type.GIVENUP), any(String.class),
                any(IOException.class));
    }

    @Test
    public void testRunIllegalState() throws IllegalAccessException {

        FieldUtils.writeField(appender, "ioHelper", null, true);

        try {
            appender.run();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("ioHelper is null");
        }
    }
}
