package org.codetab.gotz.step.load.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DataSetPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.step.load.appender.Appender.Marker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DbAppenderTest {

    @Mock
    private ConfigService configService;
    @Mock
    private ActivityService activityService;
    @Mock
    private DataSetPersistence dataSetPersistence;
    @Mock
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private DbAppender appender;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() {
        appender.init();

        assertThat(appender.isInitialized()).isTrue();
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
    public void testRun() throws InterruptedException, IOException,
            FieldsException, ConfigNotFoundException {

        List<DataSet> dataSets1 = new ArrayList<>();
        DataSet dataSet = new DataSet("x", "xg", "xc", "xr", "xf");
        dataSets1.add(dataSet);

        List<DataSet> dataSets2 = new ArrayList<>();
        dataSet = new DataSet("y", "yg", "yc", "yr", "yf");
        dataSets1.add(dataSet);

        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append(dataSets1);
        appender.append(dataSets2);
        appender.append(Marker.EOF);

        t.join();

        InOrder inOrder = inOrder(dataSetPersistence);
        inOrder.verify(dataSetPersistence).storeDataSet(dataSets1);
        inOrder.verify(dataSetPersistence).storeDataSet(dataSets2);
        verifyNoMoreInteractions(dataSetPersistence);
    }

    @Test
    public void testRunThrowsInterruptedException() throws InterruptedException,
            IOException, FieldsException, ConfigNotFoundException {

        List<DataSet> dataSets1 = new ArrayList<>();
        DataSet dataSet = new DataSet("x", "xg", "xc", "xr", "xf");
        dataSets1.add(dataSet);

        List<DataSet> dataSets2 = new ArrayList<>();
        dataSet = new DataSet("y", "yg", "yc", "yr", "yf");
        dataSets1.add(dataSet);

        doThrow(InterruptedException.class).when(dataSetPersistence)
                .storeDataSet(dataSets2);
        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append(dataSets1);
        appender.append(dataSets2);
        appender.append(Marker.EOF);

        t.join();

        InOrder inOrder = inOrder(dataSetPersistence);
        inOrder.verify(dataSetPersistence).storeDataSet(dataSets1);
        inOrder.verify(dataSetPersistence).storeDataSet(dataSets2);
        verifyNoMoreInteractions(dataSetPersistence);

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(InterruptedException.class));
    }

    @Test
    public void testRunThrowsStepPersistenceException()
            throws InterruptedException, IOException, FieldsException,
            ConfigNotFoundException {

        List<DataSet> dataSets1 = new ArrayList<>();
        DataSet dataSet = new DataSet("x", "xg", "xc", "xr", "xf");
        dataSets1.add(dataSet);

        List<DataSet> dataSets2 = new ArrayList<>();
        dataSet = new DataSet("y", "yg", "yc", "yr", "yf");
        dataSets1.add(dataSet);

        doThrow(StepPersistenceException.class).when(dataSetPersistence)
                .storeDataSet(dataSets1);
        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append(dataSets1);
        appender.append(dataSets2);

        t.join();

        InOrder inOrder = inOrder(dataSetPersistence);
        inOrder.verify(dataSetPersistence).storeDataSet(dataSets1);
        verifyNoMoreInteractions(dataSetPersistence);

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(StepPersistenceException.class));
    }

    @Test
    public void testRunItemNotList() throws InterruptedException, IOException,
            FieldsException, ConfigNotFoundException {

        appender.initializeQueue();

        Thread t = new Thread(appender);
        t.start();

        appender.append("not dataset"); // should break from loop

        t.join();

        verify(activityService).addActivity(eq(Type.FAIL), any(String.class));
        verifyZeroInteractions(dataSetPersistence);
    }

}
