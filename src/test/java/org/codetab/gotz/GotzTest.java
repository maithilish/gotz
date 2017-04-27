package org.codetab.gotz;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzTest {

    @Mock
    GotzEngine gotzEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {
        Gotz gotz = new Gotz(gotzEngine);

        verifyZeroInteractions(gotzEngine);

        gotz.start();

        verify(gotzEngine).start();
    }

}
