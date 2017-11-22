package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.convert.DataFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
    @Spy
    private FieldsHelper fieldsHelper;

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
    public void testHandover() throws FieldsException {

        Fields fields = fieldsHelper.createFields();
        fieldsHelper.addElement("x", "xv", fields);
        filter.setFields(fields);

        Data data = new Data();
        filter.setInput(data);

        boolean actual = filter.handover();

        assertThat(actual).isTrue();
        verify(stepService).pushTask(filter, data, fields);
    }

    @Test
    public void testHandoverEmptyFieldsShouldThrowException()
            throws FieldsException {
        Data data = new Data();
        filter.setInput(data);

        filter.setFields(new Fields());

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
