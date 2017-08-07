package org.codetab.gotz.stepbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.steps.DataFilter;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * BaseFilter tests.
 * @author Maithilish
 *
 */
public class BaseFilterTest {

    @Mock
    private StepService stepService;
    @Mock
    private ActivityService activityService;
    @Mock
    private DataDefService dataDefService;

    @InjectMocks
    private DataFilter filter;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsConsistent() {
        assertThat(filter.isConsistent()).isFalse();

        filter.setConsistent(false);
        assertThat(filter.isConsistent()).isFalse();

        filter.setConsistent(true);
        assertThat(filter.isConsistent()).isFalse();

        filter.setConsistent(false);

        filter.setInput(new Data());
        assertThat(filter.isConsistent()).isFalse();

        filter.setConsistent(true);
        assertThat(filter.isConsistent()).isTrue();
    }

    @Test
    public void testInitialize() {
        boolean actual = filter.initialize(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testHandover() {
        Data data = new Data();
        filter.setInput(data);

        Field field = TestUtil.createField("k", "v");
        List<FieldsBase> fields = TestUtil.asList(field);
        filter.setFields(fields);

        boolean actual = filter.handover();

        assertThat(actual).isTrue();
        verify(stepService).pushTask(filter, data, fields);
    }

    @Test
    public void testHandoverEmptyFieldsShouldThrowException() {
        Data data = new Data();
        filter.setInput(data);

        List<FieldsBase> fields = new ArrayList<>();
        filter.setFields(fields);

        testRule.expect(StepRunException.class);
        filter.handover();
    }

    @Test
    public void testHandoverIllegalState() {
        // step input is null
        testRule.expect(IllegalStateException.class);
        filter.handover();
    }

    @Test
    public void testSetInput() {
        Data actual = filter.getData();
        assertThat(actual).isNull();

        filter.setInput("some obj"); // when
        actual = filter.getData();
        assertThat(actual).isNull();

        Data data = new Data();
        filter.setInput(data); // when
        actual = filter.getData();
        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testGetData() {
        Data data = new Data();
        filter.setInput(data);

        // when
        Data actual = filter.getData();

        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testLoad() {
        boolean actual = filter.load(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testStore() {
        boolean actual = filter.store(); // when
        assertThat(actual).isFalse();
    }

}
