package org.codetab.nscoop.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codetab.nscoop.dao.IDataDefDao;
import org.codetab.nscoop.dao.DaoFactory.ORM;
import org.codetab.nscoop.dao.jdo.DaoFactory;
import org.codetab.nscoop.exception.DataDefNotFoundException;
import org.codetab.nscoop.exception.FieldNotFoundException;
import org.codetab.nscoop.model.Axis;
import org.codetab.nscoop.model.AxisName;
import org.codetab.nscoop.model.ColComparator;
import org.codetab.nscoop.model.DAxis;
import org.codetab.nscoop.model.DFilter;
import org.codetab.nscoop.model.DMember;
import org.codetab.nscoop.model.Data;
import org.codetab.nscoop.model.DataDef;
import org.codetab.nscoop.model.Field;
import org.codetab.nscoop.model.FieldsBase;
import org.codetab.nscoop.model.Member;
import org.codetab.nscoop.model.RowComparator;
import org.codetab.nscoop.util.FieldsUtil;
import org.codetab.nscoop.util.Util;
import org.codetab.nscoop.validation.DataDefValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DataDefService {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(DataDefService.class);

    private Map<String, DataDef> dataDefsMap;

    private Map<String, Set<Set<DMember>>> memberSetsMap = new HashMap<>();

    DataDefService() {
        logger.info("initialize DataDefs singleton");
        validateDataDefs();
        storeDataDefs();
        setDataDefsMap();
        traceDataDefs();
        try {
            traceDataStructure();
        } catch (ClassNotFoundException | IOException e) {
            logger.warn("{}", Util.getMessage(e));
        }
        logger.debug("initialized DataDefs singleton");
    }

    private void validateDataDefs() {
        DataDefValidator validator = new DataDefValidator();
        List<DataDef> dataDefs = getDataDefsFromBeans();
        boolean valid = true;
        for (DataDef dataDef : dataDefs) {
            validator.setDataDef(dataDef);
            if (!validator.validate()) {
                valid = false;
            }
        }
        if (!valid) {
            MonitorService.INSTANCE.triggerFatal("invalid Datadefs");
        }
    }

    private void storeDataDefs() {
        List<DataDef> newDataDefs = getDataDefsFromBeans();
        List<DataDef> oldDataDefs = loadDataDefsFromStore();

        for (DataDef newDataDef : newDataDefs) {
            String message;
            String name = newDataDef.getName();
            DataDef oldDataDef = getDataDef(oldDataDefs, name);
            String saveMode = getSaveMode(oldDataDef, newDataDef);
            switch (saveMode) {
            case "insert":
                message = "not in store, insert";
                storeDataDef(newDataDef);
                break;
            case "update":
                message = "changed, insert new version";
                resetHighDate(oldDataDef); // reset to run date
                storeDataDef(oldDataDef); // and update
                storeDataDef(newDataDef); // insert new changes
                break;
            default:
                message = "no changes";
                break;
            }
            logger.info("DataDef [{}] {}", name, message);
        }

    }

    private String getSaveMode(final DataDef oldDataDef, final DataDef newDataDef) {
        String saveMode = null;
        if (oldDataDef == null) {
            saveMode = "insert";
        } else {
            if (oldDataDef.equals(newDataDef)) {
                saveMode = ""; // default no change
            } else {
                saveMode = "update";
            }
        }
        return saveMode;
    }

    private DataDef getDataDef(final List<DataDef> dataDefs, final String name) {
        for (DataDef dataDef : dataDefs) {
            if (dataDef.getName().equals(name)) {
                return dataDef;
            }
        }
        return null;
    }

    private void setDataDefsMap() {
        // List<DataDef> newDataDefs = getDataDefsFromBeans();
        List<DataDef> storedDataDefs = loadDataDefsFromStore();
        // copyFields(newDataDefs, storedDataDefs);

        dataDefsMap = new HashMap<String, DataDef>();
        for (DataDef dataDef : storedDataDefs) {
            dataDefsMap.put(dataDef.getName(), dataDef);
        }
    }

    private List<DataDef> getDataDefsFromBeans() {
        List<DataDef> dataDefs = BeanService.INSTANCE.getBeans(DataDef.class);
        for (DataDef dataDef : dataDefs) {
            setDefaults(dataDef);
            setDates(dataDef);
            setDefaultIndexRange(dataDef);
        }
        return dataDefs;
    }

    // private void copyFields(List<DataDef> srcDataDefs, List<DataDef>
    // destDataDefs) {
    //
    // for (DataDef sDataDef : srcDataDefs) {
    // for (DataDef dDataDef : destDataDefs) {
    // if (sDataDef.getName().equals(dDataDef.getName())) {
    // dDataDef.getFields().addAll(sDataDef.getFields());
    // }
    // }
    // }
    // }

    private void setDefaults(final DataDef dataDef) {
        for (DAxis axis : dataDef.getAxis()) {
            if (axis.getName().equals("fact")) {
                DMember fact = new DMember();
                fact.setAxis(axis.getName());
                fact.setName("fact");
                fact.setOrder(0);
                fact.setValue(null);
                axis.getMember().add(fact);
            }
            // set member's axis name and order
            int i = 0;
            for (DMember member : axis.getMember()) {
                member.setAxis(axis.getName());
                if (member.getOrder() == null) {
                    member.setOrder(i++);
                }
            }
        }
    }

    private void setDefaultIndexRange(final DataDef dataDef) {
        for (DAxis dAxis : dataDef.getAxis()) {
            for (DMember dMember : dAxis.getMember()) {
                if (!FieldsUtil.isAnyFieldDefined(dMember.getFields(), "indexRange",
                        "breakAfter")) {
                    Field field = new Field();
                    field.setName("indexRange");
                    Integer index = dMember.getIndex();
                    if (index == null) {
                        field.setValue("1-1");
                    } else {
                        field.setValue(index + "-" + index);
                    }
                    dMember.getFields().add(field);
                }
            }
        }
    }

    private void setDates(final DataDef dataDef) {
        dataDef.setFromDate(ConfigService.INSTANCE.getRunDateTime());
        dataDef.setToDate(ConfigService.INSTANCE.getHighDate());
    }

    private void resetHighDate(final DataDef dataDef) {
        dataDef.setToDate(ConfigService.INSTANCE.getRunDateTime());
    }

    private void storeDataDef(final DataDef dataDef) {
        try {
            ORM orm = DaoFactory
                    .getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
            IDataDefDao dao = DaoFactory.getDaoFactory(orm).getDataDefDao();
            String name = dataDef.getName();
            logger.debug("store DataDef");
            dao.storeDataDef(dataDef);
            if (dataDef.getId() != null) {
                logger.debug("stored DataDef [{}] [{}]", dataDef.getId(), name);
            }
        } catch (RuntimeException e) {
            logger.error("{}", e.getMessage());
            logger.trace("", e);
            throw e;
        }
    }

    private List<DataDef> loadDataDefsFromStore() {
        try {
            ORM orm = DaoFactory
                    .getOrmType(ConfigService.INSTANCE.getConfig("picks.orm"));
            IDataDefDao dao = DaoFactory.getDaoFactory(orm).getDataDefDao();
            List<DataDef> dataDefs = dao
                    .getDataDefs(ConfigService.INSTANCE.getRunDateTime());
            logger.debug("DataDef loaded : [{}]", dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            logger.error("{}", e.getMessage());
            logger.trace("", e);
            throw e;
        }
    }

    public DataDef getDataDef(final String name) throws DataDefNotFoundException {
        DataDef dataDef = dataDefsMap.get(name);
        if (dataDef == null) {
            throw new DataDefNotFoundException(name);
        }
        return dataDef;
    }

    public Data getDataTemplate(final String dataDefName)
            throws DataDefNotFoundException, ClassNotFoundException, IOException {
        DataDef dataDef = getDataDef(dataDefName);
        return getDataTemplate(dataDef);
    }

    // transforms DAxis/DMember to Member/Axis
    public Data getDataTemplate(final DataDef dataDef)
            throws ClassNotFoundException, IOException, IllegalArgumentException {
        if (memberSetsMap == null || memberSetsMap.get(dataDef.getName()) == null) {
            synchronized (this) {
                generateMemberSets(dataDef);
            }
        }
        Data data = new Data();
        data.setDataDef(dataDef.getName());
        for (Set<DMember> members : memberSetsMap.get(dataDef.getName())) {
            Member dataMember = new Member();
            dataMember.setName(""); // there is no name for member
            // add axis and its fields
            for (DMember dMember : members) {
                Axis axis = createAxis(dMember);
                dataMember.addAxis(axis);
                try {
                    // fields from DMember level are added in createAxis()
                    // fields from datadef level are added here
                    FieldsBase fields = FieldsUtil.getFieldsByValue(dataDef.getFields(),
                            "member", dMember.getName());
                    dataMember.getFields().add(fields);
                } catch (FieldNotFoundException e) {
                }
            }
            try {
                String group = FieldsUtil.getValue(dataMember.getFields(), "group");
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

    private void generateMemberSets(final DataDef dataDef)
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
        return dataDefsMap.size();
    }

    private void traceDataStructure() throws ClassNotFoundException, IOException {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("---- Trace data structure ----");
        logger.trace("");
        for (String dataDefName : dataDefsMap.keySet()) {
            try {
                traceDataStructure(getDataTemplate(dataDefName));
            } catch (DataDefNotFoundException e) {
            }
        }
    }

    public void traceDataStructure(final Data data) {
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
            sb.append(FieldsUtil.getFormattedFields(member.getFields()));
            sb.append(line);
            List<Axis> axes = new ArrayList<Axis>(member.getAxes());
            Collections.sort(axes);
            for (Axis axis : axes) {
                sb.append(axis.toString());
                sb.append(line);
            }
            sb.append(line);
        }
        logger.trace("{}", sb);
    }

    public void traceDataDefs() {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("--- Trace DataDefs ----");
        for (DataDef dataDef : dataDefsMap.values()) {
            StringBuilder sb = formattedDataDef(dataDef);
            logger.trace("{}", sb);
        }
    }

    private StringBuilder formattedDataDef(final DataDef dataDef) {
        String line = System.lineSeparator();
        String json = Util.getIndentedJson(dataDef, true);
        StringBuilder sb = new StringBuilder();
        sb.append("DataDef [name=");
        sb.append(dataDef.getName());
        sb.append("]");
        sb.append(line);
        sb.append(json);
        return sb;
    }
}
