package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DataDefPersistence;
import org.codetab.gotz.testutil.XOBuilder;
import org.codetab.gotz.validation.DataDefValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataDefServiceTest {

    @Mock
    private BeanService beanService;
    @Mock
    private ConfigService configService;
    @Mock
    private DataDefPersistence dataDefPersistence;
    @Mock
    private DataDefValidator validator;
    @Mock
    private DataDefHelper dataDefHelper;
    @Mock
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private DataDefService dataDefService;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        // given
        DInjector dInjector = new DInjector().instance(DInjector.class);

        // when
        DataDefService instanceA = dInjector.instance(DataDefService.class);
        DataDefService instanceB = dInjector.instance(DataDefService.class);

        // then
        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testInitNoUpdates()
            throws IllegalAccessException, FieldsException,
            InvalidDataDefException, ClassNotFoundException, IOException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);
        DataDef dataDef2 = dataDefs.get(1);
        Set<Set<DMember>> set1 = new HashSet<>();
        Set<Set<DMember>> set2 = new HashSet<>();

        List<DataDef> newDataDefs = createSimpleDataDefs();
        DataDef newDataDef1 = newDataDefs.get(0);
        DataDef newDataDef2 = newDataDefs.get(1);

        // update false
        given(dataDefPersistence.markForUpdation(dataDefs, newDataDefs))
                .willReturn(false);

        given(beanService.getBeans(DataDef.class)).willReturn(newDataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);
        given(dataDefHelper.generateMemberSets(dataDef1)).willReturn(set1);
        given(dataDefHelper.generateMemberSets(dataDef2)).willReturn(set2);

        // when
        dataDefService.init();

        // then
        InOrder inOrder = inOrder(dataDefPersistence, beanService,
                dataDefHelper, validator);

        // get beans
        inOrder.verify(beanService).getBeans(DataDef.class);

        // set defaults
        inOrder.verify(dataDefHelper).addFact(newDataDef1);
        inOrder.verify(dataDefHelper).setOrder(newDataDef1);
        inOrder.verify(dataDefHelper).setDates(newDataDef1);
        inOrder.verify(dataDefHelper).addIndexRange(newDataDef1);
        inOrder.verify(dataDefHelper).addFields(newDataDef1);

        inOrder.verify(dataDefHelper).addFact(newDataDef2);
        inOrder.verify(dataDefHelper).setOrder(newDataDef2);
        inOrder.verify(dataDefHelper).setDates(newDataDef2);
        inOrder.verify(dataDefHelper).addIndexRange(newDataDef2);
        inOrder.verify(dataDefHelper).addFields(newDataDef2);

        // validate
        inOrder.verify(validator).validate(newDataDef1);
        inOrder.verify(validator).validate(newDataDef2);

        // no updates
        inOrder.verify(dataDefPersistence).loadDataDefs();
        inOrder.verify(dataDefPersistence).markForUpdation(dataDefs,
                newDataDefs);

        // data templates
        inOrder.verify(dataDefHelper).generateMemberSets(dataDef1);
        inOrder.verify(dataDefHelper).createDataTemplate(dataDef1, set1);
        inOrder.verify(dataDefHelper).generateMemberSets(dataDef2);
        inOrder.verify(dataDefHelper).createDataTemplate(dataDef2, set2);

        inOrder.verify(dataDefHelper).getDataStructureTrace(eq("x"),
                nullable(Data.class));
        inOrder.verify(dataDefHelper).getDataStructureTrace(eq("y"),
                nullable(Data.class));

        verifyNoMoreInteractions(dataDefPersistence, beanService, dataDefHelper,
                validator);
    }

    @Test
    public void testInitWithUpdates()
            throws IllegalAccessException, FieldsException,
            InvalidDataDefException, ClassNotFoundException, IOException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);
        DataDef dataDef2 = dataDefs.get(1);
        Set<Set<DMember>> set1 = new HashSet<>();
        Set<Set<DMember>> set2 = new HashSet<>();

        List<DataDef> newDataDefs = createSimpleDataDefs();
        DataDef newDataDef1 = newDataDefs.get(0);
        DataDef newDataDef2 = newDataDefs.get(1);

        List<DataDef> clones = createSimpleDataDefs();
        DataDef clone1 = clones.get(0);
        DataDef clone2 = clones.get(1);

        // update true
        given(dataDefPersistence.markForUpdation(dataDefs, newDataDefs))
                .willReturn(true);

        given(beanService.getBeans(DataDef.class)).willReturn(newDataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);
        given(dataDefHelper.generateMemberSets(dataDef1)).willReturn(set1);
        given(dataDefHelper.generateMemberSets(dataDef2)).willReturn(set2);
        given(dataDefHelper.cloneDataDef(newDataDef1)).willReturn(clone1);
        given(dataDefHelper.cloneDataDef(newDataDef2)).willReturn(clone2);

        // when
        dataDefService.init();

        // then
        InOrder inOrder = inOrder(dataDefPersistence, beanService,
                dataDefHelper, validator);

        // get beans
        inOrder.verify(beanService).getBeans(DataDef.class);

        // set defaults
        inOrder.verify(dataDefHelper).addFact(newDataDef1);
        inOrder.verify(dataDefHelper).setOrder(newDataDef1);
        inOrder.verify(dataDefHelper).setDates(newDataDef1);
        inOrder.verify(dataDefHelper).addIndexRange(newDataDef1);
        inOrder.verify(dataDefHelper).addFields(newDataDef1);

        inOrder.verify(dataDefHelper).addFact(newDataDef2);
        inOrder.verify(dataDefHelper).setOrder(newDataDef2);
        inOrder.verify(dataDefHelper).setDates(newDataDef2);
        inOrder.verify(dataDefHelper).addIndexRange(newDataDef2);
        inOrder.verify(dataDefHelper).addFields(newDataDef2);

        // validate
        inOrder.verify(validator).validate(newDataDef1);
        inOrder.verify(validator).validate(newDataDef2);

        // with updates
        inOrder.verify(dataDefPersistence).loadDataDefs();
        inOrder.verify(dataDefPersistence).markForUpdation(dataDefs,
                newDataDefs);
        inOrder.verify(dataDefHelper).cloneDataDef(newDataDef1);
        inOrder.verify(dataDefPersistence).storeDataDef(clone1);
        inOrder.verify(dataDefHelper).cloneDataDef(newDataDef2);
        inOrder.verify(dataDefPersistence).storeDataDef(clone2);
        inOrder.verify(dataDefPersistence).loadDataDefs();

        // data templates
        inOrder.verify(dataDefHelper).generateMemberSets(dataDef1);
        inOrder.verify(dataDefHelper).createDataTemplate(dataDef1, set1);
        inOrder.verify(dataDefHelper).generateMemberSets(dataDef2);
        inOrder.verify(dataDefHelper).createDataTemplate(dataDef2, set2);

        inOrder.verify(dataDefHelper).getDataStructureTrace(eq("x"),
                nullable(Data.class));
        inOrder.verify(dataDefHelper).getDataStructureTrace(eq("y"),
                nullable(Data.class));

        verifyNoMoreInteractions(dataDefPersistence, beanService, dataDefHelper,
                validator);
    }

    @Test
    public void testInitaddTransientDataDefs() throws DataDefNotFoundException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);
        dataDefs.remove(1); // dataDef2

        List<DataDef> newDataDefs = createSimpleDataDefs();
        DataDef newDataDef2 = newDataDefs.get(1); // transient i.e. new one

        given(beanService.getBeans(DataDef.class)).willReturn(newDataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        // when
        dataDefService.init();

        assertThat(dataDefService.getDataDef("x")).isSameAs(dataDef1);
        assertThat(dataDefService.getDataDef("y")).isSameAs(newDataDef2);
    }

    @Test
    public void testInitCheckDataDefs() throws DataDefNotFoundException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);
        DataDef dataDef2 = dataDefs.get(1);

        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        // when
        dataDefService.init();
        List<DataDef> actual = dataDefService.getDataDefs();

        assertThat(actual).containsExactly(dataDef1, dataDef2);
    }

    @Test
    public void testInitSetDefaultShouldThrowException()
            throws FieldsException {
        List<DataDef> newDataDefs = createSimpleDataDefs();
        DataDef newDataDef1 = newDataDefs.get(0);

        given(beanService.getBeans(DataDef.class)).willReturn(newDataDefs);
        doThrow(FieldsException.class).when(dataDefHelper)
                .addIndexRange(newDataDef1);

        testRule.expect(CriticalException.class);
        dataDefService.init();
    }

    @Test
    public void testInitValidationShouldThrowException()
            throws InvalidDataDefException {
        List<DataDef> newDataDefs = createSimpleDataDefs();
        DataDef newDataDef1 = newDataDefs.get(0);

        given(beanService.getBeans(DataDef.class)).willReturn(newDataDefs);
        doThrow(InvalidDataDefException.class).when(validator)
                .validate(newDataDef1);

        testRule.expect(CriticalException.class);
        dataDefService.init();
    }

    @Test
    public void testInitCreateAndCacheTemplateShouldThrowException()
            throws ClassNotFoundException, IOException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);

        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        doThrow(IOException.class).when(dataDefHelper)
                .generateMemberSets(dataDef1);

        try {
            dataDefService.init();
            fail("should throw CriticalException");
        } catch (CriticalException e) {
        }

        doThrow(ClassNotFoundException.class).when(dataDefHelper)
                .generateMemberSets(dataDef1);

        try {
            dataDefService.init();
            fail("should throw CriticalException");
        } catch (CriticalException e) {
        }
    }

    @Test
    public void testInitTraceDataStructureShouldThrowException()
            throws ClassNotFoundException, IOException, IllegalAccessException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef1 = dataDefs.get(0);
        dataDef1.getAxis().clear();

        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        try {
            dataDefService.init();
            fail("should throw CriticalException");
        } catch (CriticalException e) {
        }

        // for test coverage
        dataDefs = createSimpleDataDefs();
        dataDef1 = dataDefs.get(0);
        Set<Set<DMember>> set1 = new HashSet<>();
        Data data = new Data();

        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);
        given(dataDefHelper.generateMemberSets(dataDef1)).willReturn(set1);
        given(dataDefHelper.createDataTemplate(dataDef1, set1))
                .willReturn(data);
        given(dataDefHelper.getDataStructureTrace("x", data))
                .willThrow(NoSuchElementException.class); // just a hack

        try {
            dataDefService.init();
            fail("should throw CriticalException");
        } catch (CriticalException e) {
        }
    }

    @Test
    public void testGetDataDef()
            throws IllegalAccessException, DataDefNotFoundException {
        List<DataDef> dataDefs = createSimpleDataDefs();
        DataDef dataDef = dataDefs.get(0);

        FieldUtils.writeField(dataDefService, "dataDefs", dataDefs, true);

        DataDef actual = dataDefService.getDataDef("x");

        assertThat(actual).isSameAs(dataDef);
    }

    @Test
    public void testGetDataDefShouldThrowException()
            throws IllegalAccessException, DataDefNotFoundException {
        List<DataDef> dataDefs = createSimpleDataDefs();

        FieldUtils.writeField(dataDefService, "dataDefs", dataDefs, true);

        testRule.expect(DataDefNotFoundException.class);
        dataDefService.getDataDef("z");
    }

    @Test
    public void testGetDataTemplate() throws IllegalAccessException {
        Data data = new Data();
        data.setDataDef("x");

        Map<String, Data> map = new HashMap<>();
        map.put("x", data);

        FieldUtils.writeField(dataDefService, "dataTemplateMap", map, true);

        Data actual = dataDefService.getDataTemplate("x");

        assertThat(actual).isEqualTo(data);
        assertThat(actual).isNotSameAs(data); // clone
    }

    @Test
    public void testGetDataTemplateShouldThrowException()
            throws IllegalAccessException {
        Data data = new Data();
        data.setDataDef("x");

        Map<String, Data> map = new HashMap<>();
        map.put("x", data);

        FieldUtils.writeField(dataDefService, "dataTemplateMap", map, true);

        testRule.expect(NoSuchElementException.class);
        dataDefService.getDataTemplate("z");
    }

    @Test
    public void testGetFilterMap() throws DataDefNotFoundException {
        List<DataDef> dataDefs = createTestDataDefs();
        List<Fields> expectedFilters = createTestFilters();

        // for coverage - axis with null filter
        DAxis axis = new DAxis();
        axis.setName("row");
        dataDefs.get(0).getAxis().add(axis);

        given(beanService.getBeans(DataDef.class)).willReturn(dataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        dataDefService.init();

        Map<AxisName, Fields> filterMap = dataDefService.getFilterMap("x");

        // fields toString is used for comparison as XML format may differ
        assertThat(filterMap.get(AxisName.COL).toString())
                .isEqualToNormalizingWhitespace(
                        expectedFilters.get(0).toString());
        assertThat(filterMap.get(AxisName.ROW)).isNull();
        assertThat(filterMap.get(AxisName.FACT)).isNull();

        filterMap = dataDefService.getFilterMap("y");

        assertThat(filterMap.get(AxisName.COL)).isNull();
        assertThat(filterMap.get(AxisName.ROW).toString())
                .isEqualToNormalizingWhitespace(
                        expectedFilters.get(1).toString());
        assertThat(filterMap.get(AxisName.FACT)).isNull();
    }

    @Test
    public void testGetFilterMapShouldThrowException()
            throws DataDefNotFoundException {
        List<DataDef> dataDefs = createTestDataDefs();

        given(beanService.getBeans(DataDef.class)).willReturn(dataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);

        dataDefService.init();

        testRule.expect(DataDefNotFoundException.class);
        dataDefService.getFilterMap("unknown");
    }

    @Test
    public void testGetCount() {
        // given
        List<DataDef> dataDefs = createSimpleDataDefs();
        given(dataDefPersistence.loadDataDefs()).willReturn(dataDefs);
        dataDefService.init();

        // when
        int count = dataDefService.getCount();

        // then
        assertThat(count).isEqualTo(2);
    }

    private List<DataDef> createSimpleDataDefs() {
        DataDef dataDef1 = new DataDef();
        dataDef1.setName("x");
        dataDef1.getAxis().add(new DAxis());
        DataDef dataDef2 = new DataDef();
        dataDef2.setName("y");
        dataDef2.getAxis().add(new DAxis());

        List<DataDef> dataDefs = new ArrayList<>();
        dataDefs.add(dataDef1);
        dataDefs.add(dataDef2);
        return dataDefs;
    }

    private List<DataDef> createTestDataDefs() {

        // @formatter:off
        List<DataDef> dataDefs = new XOBuilder<DataDef>()
        .add("<dataDef name='x'>")
        .add(" <axis name='col'>")
        .add("  <filter axis='col'>")
        .add("   <xf:fields>")
        .add("    <xf:filters type='value'>")
        .add("      <xf:filter name='fx1' pattern='filter fx1' />")
        .add("      <xf:filter name='fx2' pattern='filter fx2' />")
        .add("    </xf:filters>")
        .add("   </xf:fields>")
        .add("  </filter>")
        .add(" </axis>")
        .add("</dataDef>")
        .add("<dataDef name='y'>")
        .add(" <axis name='row'>")
        .add("  <filter axis='row'>")
        .add("   <xf:fields>")
        .add("    <xf:filters type='match'>")
        .add("      <xf:filter name='fy1' pattern='filter fy1' />")
        .add("    </xf:filters>")
        .add("   </xf:fields>")
        .add("  </filter>")
        .add(" </axis>")
        .add("</dataDef>")
        .build(DataDef.class);
        // @formatter:on

        return dataDefs;
    }

    private List<Fields> createTestFilters() {

        //@formatter:off
        List<Fields> filter = new XOBuilder<Fields>()
        .add("<xf:fields>")
        .add("  <xf:filters type='value' >")
        .add("     <xf:filter name='fx1' pattern='filter fx1' />")
        .add("     <xf:filter name='fx2' pattern='filter fx2' />")
        .add("  </xf:filters>")
        .add("</xf:fields>")
        .add("<xf:fields>")
        .add("  <xf:filters type='match' >")
        .add("     <xf:filter name='fy1' pattern='filter fy1' />")
        .add("  </xf:filters>")
        .add("</xf:fields>")
        .build(Fields.class);
        //@formatter:on

        return filter;
    }

}
