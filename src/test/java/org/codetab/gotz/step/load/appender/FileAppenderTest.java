package org.codetab.gotz.step.load.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.step.load.appender.Appender.Marker;
import org.codetab.gotz.testutil.XOBuilder;
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
        appender.setInitialized(true);
        appender.initializeQueue();

        appender.append(str);

        assertThat(appender.getQueue().take()).isEqualTo(str);
    }

    @Test
    public void testAppendNullParams() throws InterruptedException {
        try {
            appender.setInitialized(true);
            appender.append(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("object must not be null");
        }
    }

    @Test
    public void testRunFileFieldNotSet() throws InterruptedException,
            FieldsException, ConfigNotFoundException {

        appender.setFields(new Fields());
        appender.init();

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(FieldsNotFoundException.class));
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
        appender.init();
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
            FieldsException, ConfigNotFoundException, IllegalAccessException {

        PrintWriter writer = Mockito.mock(PrintWriter.class);
        FieldUtils.writeField(appender, "writer", writer, true);

        appender.setFields(new Fields());
        appender.setInitialized(true);
        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append("test");
        appender.append(Marker.EOF);

        t.join();

        verify(writer).close();

    }

    @Test
    public void testRunShouldLogAcivityOnInterruptedException()
            throws InterruptedException, IllegalAccessException, IOException,
            FieldsException {
        String fileName = "target/test.txt";

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:appender>")
          .add("  <xf:file>")
          .add(fileName)
          .add("  </xf:file>")
          .add("</xf:appender>")
          .buildFields();
        //@formatter:on

        appender.setFields(fields);
        appender.init();

        @SuppressWarnings("unchecked")
        BlockingQueue<Object> queue = Mockito.mock(BlockingQueue.class);
        FieldUtils.writeField(appender, "queue", queue, true);

        given(queue.take()).willThrow(InterruptedException.class)
                .willReturn(Marker.EOF);

        appender.run();

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(InterruptedException.class));
    }

    @Test
    public void testInitShouldLogAcivityOnIOException()
            throws InterruptedException, IllegalAccessException, IOException,
            FieldsException {
        String fileName = "/home/xyzz/test.txt";

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:appender>")
          .add("  <xf:file>")
          .add(fileName)
          .add("  </xf:file>")
          .add("</xf:appender>")
          .buildFields();
        //@formatter:on

        appender.setFields(fields);
        appender.init();

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(IOException.class));
    }

    @Test
    public void testInitIllegalState() throws IllegalAccessException {

        FieldUtils.writeField(appender, "ioHelper", null, true);

        try {
            appender.init();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("ioHelper is null");
        }
    }
}
