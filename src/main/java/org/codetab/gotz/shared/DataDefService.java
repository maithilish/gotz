package org.codetab.gotz.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.persistence.DataDefPersistence;
import org.codetab.gotz.util.MarkerUtil;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.validation.DataDefValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

@Singleton
public class DataDefService {

    private final Logger logger = LoggerFactory.getLogger(DataDefService.class);

    // stored datadefs
    private List<DataDef> dataDefs;

    private Map<String, Set<Set<DMember>>> memberSetsMap;
    private Map<String, Data> dataTemplateMap;

    @Inject
    private BeanService beanService;

    @Inject
    private DataDefPersistence dataDefPersistence;
    @Inject
    private DataDefValidator validator;
    @Inject
    private DataDefHelper dataDefHelper;

    @Inject
    private DataDefService() {
    }

    public void init() {
        logger.info("initialize DataDefs singleton");

        List<DataDef> newDataDefs = beanService.getBeans(DataDef.class);

        setDefaults(newDataDefs);

        memberSetsMap = new HashMap<>();
        dataTemplateMap = new HashMap<>();
        dataDefs = newDataDefs;
        validateDataDefs(newDataDefs);

        // clear maps populated for validation
        memberSetsMap = new HashMap<>();
        dataTemplateMap = new HashMap<>();

        dataDefs = dataDefPersistence.loadDataDefs();
        /*
         * store changed datadef and load latest datadef, persistence will alter
         * object so clone newDataDefs
         */
        storeDataDefs(cloneDataDefs(newDataDefs));

        // add new datadefs from file which are not persisted
        addTransientDataDefs(newDataDefs);

        traceDataDefs();

        traceDataStructure();

        logger.debug("initialized DataDefs singleton");
    }

    private List<DataDef> cloneDataDefs(final List<DataDef> newDataDefs) {
        List<DataDef> clone = new ArrayList<>();

        for (DataDef dataDef : newDataDefs) {
            clone.add(SerializationUtils.clone(dataDef));
        }
        return clone;
    }

    private void addTransientDataDefs(final List<DataDef> newDataDefs) {
        for (DataDef newDataDef : newDataDefs) {
            boolean exists = dataDefs.stream()
                    .anyMatch(df -> df.getName().equals(newDataDef.getName()));
            if (!exists) {
                dataDefs.add(newDataDef);
            }
        }
    }

    private void storeDataDefs(final List<DataDef> newDataDefs) {

        boolean updates =
                dataDefPersistence.markForUpdation(dataDefs, newDataDefs);

        if (updates) {
            for (DataDef dataDef : dataDefs) {
                dataDefPersistence.storeDataDef(dataDef);
            }
            dataDefs = dataDefPersistence.loadDataDefs();
        }
    }

    private void validateDataDefs(final List<DataDef> newDataDefs) {

        for (DataDef dataDef : newDataDefs) {
            try {
                validator.validate(dataDef);
                // getDataTemplate(dataDef.getName()); // only for validation
            } catch (InvalidDataDefException e) {
                String message =
                        Util.join("invalid datadef [", dataDef.getName(), "]");
                throw new CriticalException(message, e);
            }
        }
    }

    private void setDefaults(final List<DataDef> newDataDefs) {
        for (DataDef dataDef : newDataDefs) {
            try {
                dataDefHelper.addFact(dataDef);
                dataDefHelper.setOrder(dataDef);
                dataDefHelper.setDates(dataDef);
                dataDefHelper.addIndexRange(dataDef);
                dataDefHelper.addFields(dataDef);
            } catch (FieldsException e) {
                throw new CriticalException(
                        "datadef [" + dataDef.getName() + "] set defaults", e);
            }
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
        Data data = null;
        if (dataTemplateMap.containsKey(dataDefName)) {
            data = dataTemplateMap.get(dataDefName);
        } else {
            // create and cache the memberSets and data
            DataDef dataDef = getDataDef(dataDefName);
            Set<Set<DMember>> memberSets = null;
            if (memberSetsMap.containsKey(dataDefName)) {
                memberSets = memberSetsMap.get(dataDefName);
            } else {
                memberSets = dataDefHelper.generateMemberSets(dataDef); // synchronized
                memberSetsMap.put(dataDefName, memberSets);
            }
            data = dataDefHelper.createDataTemplate(dataDef, memberSets);
            dataTemplateMap.put(dataDefName, data);
        }
        return SerializationUtils.clone(data);
    }

    public Map<AxisName, Fields> getFilterMap(final String dataDef)
            throws DataDefNotFoundException {
        Map<AxisName, Fields> filterMap = new HashMap<>();
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

    private void traceDataStructure() {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("---- Trace data structure ----");
        logger.trace("");
        for (DataDef dataDef : dataDefs) {
            if (dataDef.getAxis().size() == 0) {
                String message = Util.join("datadef [", dataDef.getName(),
                        "] no axis defined");
                throw new CriticalException(message);
            }

            try {
                String dataDefName = dataDef.getName();
                Data data = getDataTemplate(dataDefName);
                String trace =
                        dataDefHelper.getDataStructureTrace(dataDefName, data);
                Marker marker = MarkerUtil.getMarker(dataDefName);
                logger.trace(marker, "{}", trace);
            } catch (ClassNotFoundException | DataDefNotFoundException
                    | IOException e) {
                String message = Util.join("datadef [", dataDef.getName(), "]");
                throw new CriticalException(message, e);
            }

        }
    }

    public void traceDataStructure(final String dataDefName, final Data data) {
        String trace = dataDefHelper.getDataStructureTrace(dataDefName, data);
        Marker marker = MarkerUtil.getMarker(dataDefName);
        logger.trace(marker, "{}", trace);
    }

}
