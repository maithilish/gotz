package org.codetab.gotz.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataDefDefaultsTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private DataDefHelper dataDefDefaults;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddFact() {
        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefDefaults.addFact(dataDef);

        // then
        DMember fact = new DMember();
        fact.setAxis("fact");
        fact.setName("fact");
        fact.setOrder(0);
        fact.setValue(null);

        DAxis factAxis = getAxis(dataDef, "fact");
        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        assertThat(factAxis.getMember().size()).isEqualTo(1);
        assertThat(factAxis.getMember()).contains(fact);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(rowAxis.getMember().size()).isEqualTo(1);
    }

    @Test
    public void testSetOrder() {
        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefDefaults.setOrder(dataDef);

        // then
        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        DMember m1 = new DMember();
        m1.setAxis("col");
        m1.setOrder(0);
        DMember m2 = new DMember();
        m2.setAxis("col");
        m2.setOrder(1);
        DMember m3 = new DMember();
        m3.setAxis("row");
        m3.setOrder(10);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(colAxis.getMember()).contains(m1);
        assertThat(colAxis.getMember()).contains(m2);

        assertThat(rowAxis.getMember().size()).isEqualTo(1);
        assertThat(rowAxis.getMember()).contains(m3);
    }

    @Test
    public void testAddIndexRange() {
        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefDefaults.addIndexRange(dataDef);

        // then
        Field field = new Field();
        field.setName("indexRange");
        field.setValue("1-1");

        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        DMember m1 = new DMember();
        m1.getFields().add(field);
        DMember m2 = new DMember();
        m2.getFields().add(field);
        DMember m3 = new DMember();
        m3.setOrder(10);
        m3.getFields().add(field);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(colAxis.getMember()).contains(m1);
        assertThat(colAxis.getMember()).contains(m2);

        assertThat(rowAxis.getMember().size()).isEqualTo(1);
        assertThat(rowAxis.getMember()).contains(m3);
    }

    @Test
    public void testAddIndexRangeWithIndex() {
        // given
        DMember m1 = new DMember();
        m1.setIndex(5);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefDefaults.addIndexRange(dataDef);

        // then
        Field field = new Field();
        field.setName("indexRange");
        field.setValue("5-5");

        m1.getFields().add(field);

        assertThat(colAxis.getMember().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithIndexRange() {
        // given
        Field field = new Field();
        field.setName("indexRange");
        field.setValue("3-10");

        DMember m1 = new DMember();
        m1.getFields().add(field);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefDefaults.addIndexRange(dataDef);

        // then
        assertThat(m1.getFields().size()).isEqualTo(1);
        assertThat(m1.getFields()).contains(field);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithBreakAfter() {
        // given
        Field field = new Field();
        field.setName("breakAfter");
        field.setValue("xyz");

        DMember m1 = new DMember();
        m1.getFields().add(field);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefDefaults.addIndexRange(dataDef);

        // then
        assertThat(m1.getFields().size()).isEqualTo(1);
        assertThat(m1.getFields()).contains(field);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testSetDates() {
        // given
        DataDef dataDef = createTestDataDef();

        Date runDate = new Date();
        Date highDate = DateUtils.addYears(runDate, 50);

        given(configService.getRunDateTime()).willReturn(runDate);
        given(configService.getHighDate()).willReturn(highDate);

        // when
        dataDefDefaults.setDates(dataDef);

        // then
        assertThat(dataDef.getFromDate()).isEqualTo(runDate);
        assertThat(dataDef.getToDate()).isEqualTo(highDate);
    }

    private DAxis getAxis(final DataDef dataDef, final String axisName) {
        return dataDef.getAxis().stream()
                .filter(d -> d.getName().equals(axisName)).findFirst().get();
    }

    private DataDef createTestDataDef() {

        DMember m1 = new DMember();
        DMember m2 = new DMember();
        m2.setAxis("x");

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);
        colAxis.getMember().add(m2);

        DMember m3 = new DMember();
        m3.setOrder(10);

        DAxis rowAxis = new DAxis();
        rowAxis.setName("row");
        rowAxis.getMember().add(m3);

        DAxis factAxis = new DAxis();
        factAxis.setName("fact");

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);
        dataDef.getAxis().add(rowAxis);
        dataDef.getAxis().add(factAxis);

        return dataDef;
    }

}
