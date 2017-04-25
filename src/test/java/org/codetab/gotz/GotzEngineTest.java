package org.codetab.gotz;

import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.shared.MonitorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class GotzEngineTest {

    @Spy
    private MonitorService monitorServiceMock;

    @InjectMocks
    GotzEngine sut;

    @InjectMocks
    private MonitorService monitorService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FieldUtils.writeDeclaredField(monitorService, "INSTANCE",
                monitorServiceMock, true);
    }

    @Test
    public void testStart() {

        sut.start();

        verify(monitorServiceMock).start();
        verify(monitorServiceMock).end();

    }

}
