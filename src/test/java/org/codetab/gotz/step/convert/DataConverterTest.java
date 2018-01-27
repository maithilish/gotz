package org.codetab.gotz.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.convert.converter.IConverter;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataConverterTest {

    @Mock
    private FieldsHelper fieldsHelper;
    @Mock
    private StepService stepService;
    @InjectMocks
    private DataConverter dataConverter;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private Data data;
    private Labels labels;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        dataConverter.setStepType("process");

        labels = new Labels("x", "y");
        dataConverter.setLabels(labels);

        data = createTestData();
        dataConverter.setInput(data);

        dataConverter.setFields(TestUtil.createEmptyFields());
    }

    @Test
    public void testInstance() {
        dataConverter = new DataConverter();

        assertThat(dataConverter.isConsistent()).isFalse();
        assertThat(dataConverter.getStepType()).isNull();
        assertThat(dataConverter.instance()).isInstanceOf(DataConverter.class);
        assertThat(dataConverter.instance()).isSameAs(dataConverter.instance());
    }

    @Test
    public void testProcessConvert() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        List<Fields> converters = createTestConverters();

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willReturn(converters);

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(0))).willReturn("col");
        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/@class",
                converters.get(0))).willReturn("ColConverter");
        given(stepService.createInstance("ColConverter"))
                .willReturn(new ColConverter());

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(1))).willReturn("row");
        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/@class",
                converters.get(1))).willReturn("RowConverter");
        given(stepService.createInstance("RowConverter"))
                .willReturn(new ColConverter());

        dataConverter.process();

        Member member = data.getMembers().get(0);

        assertThat(member.getAxis(AxisName.COL).getValue())
                .isEqualTo("cvctest");
        assertThat(member.getAxis(AxisName.ROW).getValue())
                .isEqualTo("rvctest");
        assertThat(member.getAxis(AxisName.FACT).getValue()).isEqualTo("fv");
    }

    @Test
    public void testProcessNoConverters() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willThrow(FieldsException.class);

        dataConverter.process();

        Member member = data.getMembers().get(0);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("cv");
        assertThat(member.getAxis(AxisName.ROW).getValue()).isEqualTo("rv");
        assertThat(member.getAxis(AxisName.FACT).getValue()).isEqualTo("fv");
    }

    @Test
    public void testProcessNullValue() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        List<Fields> converters = createTestConverters();

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willReturn(converters);

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(0))).willReturn("col");
        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/@class",
                converters.get(0))).willReturn("ColConverter");
        given(stepService.createInstance("ColConverter"))
                .willReturn(new ColConverter());

        Member member = data.getMembers().get(0);
        member.getAxis(AxisName.COL).setValue(null);

        dataConverter.process();

        assertThat(member.getAxis(AxisName.COL).getValue())
                .isEqualTo("nullctest");
        assertThat(member.getAxis(AxisName.ROW).getValue()).isEqualTo("rv");
        assertThat(member.getAxis(AxisName.FACT).getValue()).isEqualTo("fv");
    }

    @Test
    public void testProcessConvertThrowsException() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        List<Fields> converters = createTestConverters();

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willReturn(converters);

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(0))).willReturn("col");
        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/@class",
                converters.get(0))).willReturn("ColConverter");
        given(stepService.createInstance("ColConverter"))
                .willThrow(NullPointerException.class);

        testRule.expect(StepRunException.class);
        dataConverter.process();
    }

    @Test
    public void testProcessConvertNoClassName() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        List<Fields> converters = createTestConverters();

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willReturn(converters);

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(0))).willReturn("col");
        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/@class",
                converters.get(0))).willThrow(FieldsNotFoundException.class);

        dataConverter.process();

        Member member = data.getMembers().get(0);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("cv");
        assertThat(member.getAxis(AxisName.ROW).getValue()).isEqualTo("rv");
        assertThat(member.getAxis(AxisName.FACT).getValue()).isEqualTo("fv");
    }

    @Test
    public void testProcessConvertNoAxisName() throws FieldsException,
            FieldsNotFoundException, ClassNotFoundException {

        List<Fields> converters = createTestConverters();

        given(fieldsHelper.split(
                "/xf:fields/xf:task/xf:steps/xf:step[@name='process']/xf:converter",
                dataConverter.getFields())).willReturn(converters);

        given(fieldsHelper.getLastValue("/xf:fields/xf:converter/xf:axis",
                converters.get(0))).willThrow(FieldsNotFoundException.class);

        dataConverter.process();

        Member member = data.getMembers().get(0);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("cv");
        assertThat(member.getAxis(AxisName.ROW).getValue()).isEqualTo("rv");
        assertThat(member.getAxis(AxisName.FACT).getValue()).isEqualTo("fv");
    }

    @Test
    public void testProcessIllegalState() throws IllegalAccessException {

        dataConverter = new DataConverter();

        try {
            dataConverter.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }

        dataConverter.setFields(TestUtil.createEmptyFields());

        try {
            dataConverter.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

    private List<Fields> createTestConverters() {
        List<Fields> converters = new ArrayList<>();

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:task>")
          .add("  <xf:steps>")
          .add("    <xf:step name='process' >")
          .add("      <xf:converter name='col' />")
          .add("  </xf:step>")
          .add("  </xf:steps>")
          .add("</xf:task>")
          .buildFields();
        //@formatter:on

        converters.add(fields);

        //@formatter:off
        fields = new XOBuilder<Fields>()
          .add("<xf:task>")
          .add("  <xf:steps>")
          .add("    <xf:step name='process' >")
          .add("      <xf:converter name='row' />")
          .add("  </xf:step>")
          .add("  </xf:steps>")
          .add("</xf:task>")
          .buildFields();
        //@formatter:on

        converters.add(fields);

        return converters;
    }

    private Data createTestData() {
        Fields fields = new XOBuilder<Fields>()
                .add("<xf:indexRange value='1-1' />").buildFields();
        Axis col = new Axis();
        col.setName(AxisName.COL);
        col.setIndex(1);
        col.setOrder(1);
        col.setValue("cv");
        col.setFields(fields);
        Axis row = new Axis();
        row.setName(AxisName.ROW);
        row.setIndex(1);
        row.setOrder(1);
        row.setValue("rv");
        row.setFields(fields);
        Axis fact = new Axis();
        fact.setName(AxisName.FACT);
        fact.setIndex(1);
        fact.setOrder(1);
        fact.setValue("fv");
        fact.setFields(fields);

        Member member = new Member();
        member.addAxis(col);
        member.addAxis(row);
        member.addAxis(fact);

        Data testData = new Data();
        testData.addMember(member);

        return testData;
    }

}

class RowConverter implements IConverter<String, String> {

    @Override
    public String convert(final String input) throws Exception {
        return input + "rtest";
    }

    @Override
    public void setFields(final Fields fields) {
    }

}

class ColConverter implements IConverter<String, String> {

    @Override
    public String convert(final String input) throws Exception {
        return input + "ctest";
    }

    @Override
    public void setFields(final Fields fields) {
    }

}
