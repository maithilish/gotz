package org.codetab.gotz;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzTest {

    @Mock
    GotzEngine gotzEngine;

    @InjectMocks
    Gotz gotz;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {
        verifyZeroInteractions(gotzEngine);

        // when
        gotz.start();

        // then
        verify(gotzEngine).start();
    }

}
