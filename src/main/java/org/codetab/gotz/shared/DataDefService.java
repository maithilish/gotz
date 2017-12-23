package org.codetab.gotz.shared;

import java.io.IOException;
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
import org.codetab.gotz.messages.Messages;
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
        logger.info(Messages.getString("DataDefService.0")); //$NON-NLS-1$

        // get new datadefs and set defaults
        List<DataDef> newDataDefs = beanService.getBeans(DataDef.class);
        setDefaults(newDataDefs);

        // validate new datadefs
        dataDefs = newDataDefs;
        validateDataDefs(newDataDefs);

        // load existing dataDefs, mark and store changed dataDefs, if there are
        // updates then reload latest datadef.
        dataDefs = dataDefPersistence.loadDataDefs();
        storeAndReloadDataDefs(newDataDefs);

        // when persist is false (globally or for a datadef) then reload will
        // not return any or that datadef then add those dateDef
        addTransientDataDefs(newDataDefs);

        // generate data templates from dataDefs
        createAndCacheDataTemplates();

        // trace
        traceDataDefs();
        traceDataStructure();

        logger.debug(Messages.getString("DataDefService.1")); //$NON-NLS-1$
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
                        Messages.getString("DataDefService.4") //$NON-NLS-1$
                                + dataDef.getName()
                                + Messages.getString("DataDefService.5"), //$NON-NLS-1$
                        e);
            }
        }
    }

    private void validateDataDefs(final List<DataDef> newDataDefs) {

        for (DataDef dataDef : newDataDefs) {
            try {
                validator.validate(dataDef);
            } catch (InvalidDataDefException e) {
                String message =
                        Util.join(Messages.getString("DataDefService.2"), //$NON-NLS-1$
                                dataDef.getName(), "]"); //$NON-NLS-1$
                throw new CriticalException(message, e);
            }
        }
    }

    private void storeAndReloadDataDefs(final List<DataDef> newDataDefs) {

        boolean updates =
                dataDefPersistence.markForUpdation(dataDefs, newDataDefs);

        if (updates) {
            for (DataDef dataDef : dataDefs) {
                // persist alters the dataDef, so persist the clone
                DataDef clone = dataDefHelper.cloneDataDef(dataDef);
                dataDefPersistence.storeDataDef(clone);
            }
            dataDefs = dataDefPersistence.loadDataDefs();
        }
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

    private void createAndCacheDataTemplates() {
        dataTemplateMap = new HashMap<>();

        for (DataDef dataDef : dataDefs) {
            // create and cache the memberSets and data
            String dataDefName = dataDef.getName();
            Set<Set<DMember>> memberSets = null;

            try {
                memberSets = dataDefHelper.generateMemberSets(dataDef); // synchronized
            } catch (ClassNotFoundException | IOException e) {
                String message =
                        Util.join(Messages.getString("DataDefService.10"), //$NON-NLS-1$
                                dataDef.getName(), "]"); //$NON-NLS-1$
                throw new CriticalException(message, e);
            }
            Data data = dataDefHelper.createDataTemplate(dataDef, memberSets);
            dataTemplateMap.put(dataDefName, data);
        }
    }

    private void traceDataDefs() {
        if (!logger.isTraceEnabled()) {
            return;
        }

        for (DataDef dataDef : dataDefs) {
            Marker marker = MarkerUtil.getMarker(dataDef.getName());
            logger.trace(marker, Messages.getString("DataDefService.6")); //$NON-NLS-1$
            logger.trace(marker, "{}", dataDef); //$NON-NLS-1$
        }
    }

    private void traceDataStructure() {
        if (!logger.isTraceEnabled()) {
            return;
        }

        for (DataDef dataDef : dataDefs) {
            if (dataDef.getAxis().size() == 0) {
                String message =
                        Util.join(Messages.getString("DataDefService.8"), //$NON-NLS-1$
                                dataDef.getName(),
                                Messages.getString("DataDefService.9")); //$NON-NLS-1$
                throw new CriticalException(message);
            }

            try {
                String dataDefName = dataDef.getName();
                Data data = getDataTemplate(dataDefName);
                traceDataStructure(dataDefName, data);
            } catch (NoSuchElementException e) {
                String message =
                        Util.join(Messages.getString("DataDefService.10"), //$NON-NLS-1$
                                dataDef.getName(), "]"); //$NON-NLS-1$
                throw new CriticalException(message, e);
            }
        }
    }

    // accessor methods
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
    public Data getDataTemplate(final String dataDefName) {
        Data data = null;
        if (dataTemplateMap.containsKey(dataDefName)) {
            data = dataTemplateMap.get(dataDefName);
        } else {
            throw new NoSuchElementException(
                    Util.join(Messages.getString("DataDefService.3"), //$NON-NLS-1$
                            dataDefName, "]")); //$NON-NLS-1$
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

    public void traceDataStructure(final String dataDefName, final Data data) {
        String trace = dataDefHelper.getDataStructureTrace(dataDefName, data);
        Marker marker = MarkerUtil.getMarker(dataDefName);
        logger.trace(marker, Messages.getString("DataDefService.12")); //$NON-NLS-1$
        logger.trace(marker, ""); //$NON-NLS-1$
        logger.trace(marker, "{}", trace); //$NON-NLS-1$
    }
}
