package org.codetab.gotz.helper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class FieldsHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FieldsHelper.class);

    private List<FieldsBase> stepFields;
    private List<FieldsBase> classFields;
    private Map<String, List<FieldsBase>> locatorFieldsMap =
            new ConcurrentHashMap<>();

    @Inject
    private BeanService beanService;

    public boolean init() {
        if (stepFields == null) {
            setStepFields();
        }
        if (classFields == null) {
            setClassFields();
        }
        return true;
    }

    /*
     * return deep copy or new fields as other threads may modify fields
     */
    public List<FieldsBase> getStepFields() {
        return FieldsUtil.deepClone(stepFields);
    }

    /*
     * return deep copy or new fields as other threads may modify fields
     */
    public List<FieldsBase> getLocatorGroupFields(final String group) {
        if (!locatorFieldsMap.containsKey(group)) {
            addGroupFieldsToMap(group);
        }
        return FieldsUtil.deepClone(locatorFieldsMap.get(group));
    }

    public void addLabel(final Locator locator) {
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = FieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    private void setClassFields() {
        List<FieldsBase> fields = beanService.getBeans(FieldsBase.class);
        try {
            classFields = FieldsUtil.filterByValue(fields, "class",
                    Locator.class.getName());
        } catch (FieldNotFoundException e) {
        }
    }

    private void setStepFields() {
        List<FieldsBase> fields = beanService.getBeans(FieldsBase.class);
        try {
            List<FieldsBase> clzFields = FieldsUtil.filterByValue(fields,
                    "class", Locator.class.getName());
            List<FieldsBase> stepsGroup =
                    FieldsUtil.filterByGroup(clzFields, "steps");
            stepFields = FieldsUtil.filterByName(stepsGroup, "step");
        } catch (FieldNotFoundException e) {
        }
    }

    private void addGroupFieldsToMap(final String locatorGroup) {
        if (locatorFieldsMap.containsKey(locatorGroup)) {
            return;
        }
        try {
            List<FieldsBase> groupFields =
                    FieldsUtil.filterByGroup(classFields, locatorGroup);
            addStepsToGroupFields(groupFields);
            locatorFieldsMap.put(locatorGroup, groupFields);
        } catch (FieldNotFoundException e) {
        }
    }

    private void addStepsToGroupFields(final List<FieldsBase> groupFields) {
        LOGGER.info("merge step fields with datadef fields");
        try {
            List<Fields> dataDefGroup =
                    FieldsUtil.filterByGroupAsFields(groupFields, "datadef");
            for (Fields dataDefFields : dataDefGroup) {
                for (FieldsBase step : stepFields) {
                    // if step is not defined for datadef, then add it
                    if (!FieldsUtil.contains(dataDefFields, step.getName(),
                            step.getValue())) {
                        dataDefFields.getFields()
                                .add(FieldsUtil.deepClone(step));
                    }
                }
            }
        } catch (FieldNotFoundException e) {
            LOGGER.warn(
                    "unable to find datadef fields in class fields, check fields xml file");
        }
    }
}
