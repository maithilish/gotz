package org.codetab.gotz.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.helper.DataDefDefaults;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.ColComparator;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.RowComparator;
import org.codetab.gotz.persistence.DataDefPersistence;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.validation.DataDefValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

@Singleton
public class DataDefService {

    private final Logger logger = LoggerFactory.getLogger(DataDefService.class);

    private List<DataDef> dataDefs;
    private Map<String, Set<Set<DMember>>> memberSetsMap = new HashMap<>();

    @Inject
    private BeanService beanService;
    @Inject
    private DataDefPersistence dataDefPersistence;
    @Inject
    private DataDefValidator validator;
    @Inject
    private DataDefDefaults dataDefDefaults;

    @Inject
    private DataDefService() {
    }

    public void init() {
        logger.info("initialize DataDefs singleton");

        List<DataDef> newDataDefs = beanService.getBeans(DataDef.class);
        setDefaults(newDataDefs);
        validateDataDefs(newDataDefs);

        dataDefs = dataDefPersistence.loadDataDefs();

        boolean updates =
                dataDefPersistence.markForUpdation(dataDefs, newDataDefs);

        if (updates) {
            for (DataDef dataDef : dataDefs) {
                dataDefPersistence.storeDataDef(dataDef);
            }
            dataDefs = dataDefPersistence.loadDataDefs();
        }

        traceDataDefs();
        try {
            traceDataStructure();
        } catch (ClassNotFoundException | IOException e) {
            logger.warn("{}", Util.getMessage(e));
        }
        logger.debug("initialized DataDefs singleton");
    }

    private void validateDataDefs(final List<DataDef> newDataDefs) {
        boolean valid = true;
        for (DataDef dataDef : newDataDefs) {
            validator.setDataDef(dataDef);
            if (!validator.validate()) {
                valid = false;
            }
        }
        if (!valid) {
            throw new CriticalException("invalid Datadefs");
        }
    }

    private void setDefaults(final List<DataDef> newDataDefs) {
        for (DataDef dataDef : newDataDefs) {
            dataDefDefaults.addFact(dataDef);
            dataDefDefaults.setOrder(dataDef);
            dataDefDefaults.setDates(dataDef);
            dataDefDefaults.addIndexRange(dataDef);
        }
    }

    public DataDef getDataDef(final String name)
            throws DataDefNotFoundException {
        try {
            return dataDefs.stream().filter(e -> e.getName().equals(name))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new DataDefNotFoundException(name);
        }
    }

    public List<DataDef> getDataDefs() {
        return dataDefs;
    }

    // transforms DAxis/DMember to Member/Axis
    public Data getDataTemplate(final String dataDefName)
            throws DataDefNotFoundException, ClassNotFoundException,
            IOException {
        DataDef dataDef = getDataDef(dataDefName);
        if (memberSetsMap.get(dataDefName) == null) {
            generateMemberSets(dataDef); // synchronized
        }
        Data data = new Data();
        data.setDataDef(dataDefName);
        for (Set<DMember> members : memberSetsMap.get(dataDefName)) {
            Member dataMember = new Member();
            dataMember.setName(""); // there is no name for member
            // add axis and its fields
            for (DMember dMember : members) {
                Axis axis = createAxis(dMember);
                dataMember.addAxis(axis);
                try {
                    // fields from DMember level are added in createAxis()
                    // fields from datadef level are added here
                    List<FieldsBase> fields = FieldsUtil.filterByValue(
                            dataDef.getFields(), "member", dMember.getName());
                    dataMember.getFields().addAll(fields);
                } catch (FieldNotFoundException e) {
                }
            }
            try {
                String group =
                        FieldsUtil.getValue(dataMember.getFields(), "group");
                dataMember.setGroup(group);
            } catch (FieldNotFoundException e) {
            }
            data.addMember(dataMember);
        }
        return data;
    }

    private Axis createAxis(final DMember dMember) {
        Axis axis = new Axis();
        AxisName axisName = AxisName.valueOf(dMember.getAxis().toUpperCase());
        axis.setName(axisName);
        axis.setOrder(dMember.getOrder());
        axis.setIndex(dMember.getIndex());
        axis.setMatch(dMember.getMatch());
        axis.setValue(dMember.getValue());
        // fields from DMember level
        axis.getFields().addAll(dMember.getFields());
        return axis;
    }

    private synchronized void generateMemberSets(final DataDef dataDef)
            throws ClassNotFoundException, IOException {
        int axesSize = dataDef.getAxis().size();
        Set<?>[] memberSets = new HashSet<?>[axesSize];
        for (int i = 0; i < axesSize; i++) {
            Set<DMember> members = dataDef.getAxis().get(i).getMember();
            memberSets[i] = members;
        }
        Set<Set<Object>> cartesianSet = Util.cartesianProduct(memberSets);
        Set<Set<DMember>> dataDefMemberSets = new HashSet<Set<DMember>>();
        for (Set<?> set : cartesianSet) {
            /*
             * memberSet array contains only Set<Member> as it is populated by
             * getMemberSet method Hence safe to ignore the warning
             */
            @SuppressWarnings("unchecked")
            Set<DMember> memberSet = (Set<DMember>) set;
            dataDefMemberSets.add(memberSet);
        }
        memberSetsMap.put(dataDef.getName(), dataDefMemberSets);
    }

    public Map<AxisName, List<FieldsBase>> getFilterMap(final String dataDef)
            throws DataDefNotFoundException, IllegalArgumentException {
        Map<AxisName, List<FieldsBase>> filterMap = new HashMap<>();
        List<DAxis> axes = getDataDef(dataDef).getAxis();
        for (DAxis axis : axes) {
            AxisName axisName = AxisName.valueOf(axis.getName().toUpperCase());
            DFilter filter = axis.getFilter();
            if (filter != null) {
                filterMap.put(axisName, filter.getFields());
            }
        }
        return filterMap;
    }

    public int getCount() {
        return dataDefs.size();
    }

    private void traceDataStructure()
            throws ClassNotFoundException, IOException {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("---- Trace data structure ----");
        logger.trace("");
        for (DataDef dataDef : dataDefs) {
            if (dataDef.getAxis().size() == 0) {
                continue;
            }
            try {
                String dataDefName = dataDef.getName();
                Data data = getDataTemplate(dataDefName);
                traceDataStructure(dataDefName, data);
            } catch (DataDefNotFoundException e) {
            }
        }
    }

    public void traceDataStructure(final String dataDefName, final Data data) {
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("DataDef [name=");
        sb.append(data.getDataDef());
        sb.append("] data structure");
        sb.append(line);
        sb.append(line);
        Collections.sort(data.getMembers(), new RowComparator());
        Collections.sort(data.getMembers(), new ColComparator());
        for (Member member : data.getMembers()) {
            sb.append("Member [");
            sb.append(member.getFields());
            sb.append(line);
            List<Axis> axes = new ArrayList<Axis>(member.getAxes());
            Collections.sort(axes);
            for (Axis axis : axes) {
                sb.append(axis.toString());
                sb.append(line);
            }
            sb.append(line);
        }
        Marker marker = MarkerUtil.getMarker(dataDefName);
        logger.trace(marker, "{}", sb);
    }

    public void traceDataDefs() {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("--- Trace DataDefs ----");
        for (DataDef dataDef : dataDefs) {
            Marker marker = MarkerUtil.getMarker(dataDef.getName());
            logger.trace(marker, "{}", dataDef);
        }
    }
}
