package org.codetab.gotz;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzTest {

    @Mock GotzEngine gotzEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMain() {
        then(Gotz.getGotzEngine()).isNotNull();

        Gotz.setGotzEngine(gotzEngine);

        Gotz.main(new String[]{"test1"});

        verify(gotzEngine).start();
    }

}
