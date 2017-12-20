package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.convert.DataFilter;
import org.codetab.gotz.testutil.FieldsBuilder;
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
public class BaseConverterTest {

    @Mock
    private StepService stepService;
    @Mock
    private ActivityService activityService;
    @Mock
    private DataDefService dataDefService;
    @Spy
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private DataFilter converter;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Labels labels = new Labels("x", "y");
        converter.setLabels(labels);
    }

    @Test
    public void testIsConsistent() {
        assertThat(converter.isConsistent()).isFalse();

        converter.setConsistent(false);
        assertThat(converter.isConsistent()).isFalse();

        converter.setConsistent(true);
        assertThat(converter.isConsistent()).isFalse();

        converter.setConsistent(false);

        converter.setInput(new Data());
        assertThat(converter.isConsistent()).isFalse();

        converter.setConsistent(true);
        assertThat(converter.isConsistent()).isFalse();

        converter.setConvertedData("data");
        assertThat(converter.isConsistent()).isTrue();
    }

    @Test
    public void testInitialize() {
        boolean actual = converter.initialize(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testHandover() throws FieldsException {

        //@formatter:off
        Fields fields = new FieldsBuilder()
                .add(" <xf:x>y</xf:x>")
                .build("xf");
        //@formatter:on

        converter.setFields(fields);

        Data data = new Data();
        converter.setInput(data);
        converter.setConvertedData(data);

        boolean actual = converter.handover();

        assertThat(actual).isTrue();
        verify(stepService).pushTask(converter, data, converter.getLabels(),
                fields);
    }

    @Test
    public void testHandoverEmptyFieldsShouldThrowException() {
        Data data = new Data();
        converter.setInput(data);
        converter.setConvertedData(data);

        converter.setFields(new Fields());

        testRule.expect(StepRunException.class);
        converter.handover();
    }

    @Test
    public void testHandoverIllegalState() {
        // step input is null
        testRule.expect(IllegalStateException.class);
        converter.handover();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        Data expected = new Data();
        converter.setInput(expected);
        Data actual = (Data) FieldUtils.readField(converter, "data", true);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    public void testSetInputShouldThrowException()
            throws IllegalAccessException {
        FieldUtils.writeField(converter, "data", null, true);
        testRule.expect(StepRunException.class);
        converter.setInput("xyz");
    }

    @Test
    public void testGetData() {
        Data data = new Data();
        converter.setInput(data);

        // when
        Data actual = converter.getData();

        assertThat(actual).isSameAs(data);
    }

    @Test
    public void testLoad() {
        boolean actual = converter.load(); // when
        assertThat(actual).isFalse();
    }

    @Test
    public void testStore() {
        boolean actual = converter.store(); // when
        assertThat(actual).isFalse();
    }

}
