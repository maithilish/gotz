package org.codetab.gotz.model.helper;

import java.util.ArrayList;
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

/**
 * <p>
 * Provides fields for locator. It obtains list of fields from BeanService and
 * merges step fields with all datadef fields. For performance, it caches fields
 * group-wise in a hash map.
 *
 * <p>
 * The following XML fragment specifies common steps and group/datadef structure
 *
 * <pre>
 *  &lt;fields name="class" value="org.codetab.gotz.model.Locator"&gt;
 *        --- step fields common to all groups/datadefs ---
 *        &lt;fields name="group" value="steps"&gt;
 *            &lt;fields name="step" value="seeder"&gt;
 *                &lt;field name="class" value="LocatorSeeder" /&gt;
 *                &lt;field name="nextstep" value="loader" /&gt;
 *            &lt;/fields&gt;
 *        &lt;/fields&gt;
 *        --- groups ---
 *        &lt;fields name="group" value="quote"&gt;
 *            &lt;fields name="group" value="datadef"&gt;
 *                --- one or more datadefs ---
 *                &lt;field name="datadef" value="quotePage" /&gt;
 *                   --- zero or more steps ---
 *                &lt;/fields&gt;
 *            &lt;/fields&gt;
 * </pre>
 *
 * @author Maithilish
 *
 */
@Singleton
public class LocatorFieldsHelper {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorFieldsHelper.class);

    /**
     * Holds list of steps general to all groups. Steps defined at groups
     * datadef level are excluded.
     */
    private List<FieldsBase> stepFields;
    /**
     * Holds list of groups defined for locator class.
     */
    private List<FieldsBase> classFields;
    /**
     * Cache of group (key) list of group fields (value) pair. It holds
     * effective group fields i.e. Group fields with all steps.
     */
    private Map<String, List<FieldsBase>> locatorFieldsMap =
            new ConcurrentHashMap<>();

    /**
     * instance of BeanService.
     */
    @Inject
    private BeanService beanService;

    /**
     * private constructor.
     */
    @Inject
    private LocatorFieldsHelper() {
    }

    /**
     * initializes the helper by assigning step and class fields to state
     * variables.
     * @return true if able to set step and class fields else returns false
     */
    public boolean init() {
        setStepFields();
        setClassFields();
        return true;
    }

    /**
     * Returns deep copy of step fields defined. Only steps defined outside the
     * group which are general to all groups are included and steps defined at
     * datadef level are not included. As other threads may modify fields, deep
     * copy is returned.
     * @return list of step fields
     * @throws IllegalStateException
     *             if not initialized by calling init
     */
    public List<FieldsBase> getStepFields() {
        if (stepFields == null) {
            throw new IllegalStateException(
                    "LocatorFieldsHelper is not initialized");
        }
        return FieldsUtil.deepClone(stepFields);
    }

    /**
     * Returns fields of a group for locator class. Step fields are merged with
     * datadef fields in the group to obtain effective fields for the group. For
     * performance, it caches the group fields in a ConcurrentHashMap and
     * returns deep copy of the cached item. As other threads may modify the
     * fields, deep copy is essential. If, no matching group is found, then
     * empty list is returned.
     * @param group
     *            {@link String } group name
     * @return deep copy of effective list of group fields
     * @throws IllegalStateException
     *             if not initialized by calling init
     */
    public List<FieldsBase> getLocatorGroupFields(final String group) {
        if (stepFields == null || classFields == null) {
            throw new IllegalStateException(
                    "LocatorFieldsHelper is not initialized");
        }
        if (!locatorFieldsMap.containsKey(group)) {
            addGroupFieldsToMap(group);
        }
        List<FieldsBase> fields = locatorFieldsMap.get(group);
        if (fields == null) {
            return new ArrayList<>();
        } else {
            return FieldsUtil.deepClone(fields);
        }
    }

    /**
     * adds label field to locator. Label is name:group pair. *
     * @param locator
     *            {@link Locator}
     */
    public void addLabel(final Locator locator) {
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        FieldsBase field = FieldsUtil.createField("label", label);
        locator.getFields().add(field);
    }

    /**
     * obtains list of fields from BeanService and filters them by Locator
     * class.
     */
    private void setClassFields() {
        List<FieldsBase> fields = beanService.getBeans(FieldsBase.class);
        try {
            classFields = FieldsUtil.filterByValue(fields, "class",
                    Locator.class.getName());
        } catch (FieldNotFoundException e) {
            classFields = new ArrayList<>();
            LOGGER.warn("{}", e.getLocalizedMessage());
        }
    }

    /**
     * obtains list of fields from BeanService and filters them by Locator
     * class. From the filtered list it obtains steps that are common to all
     * groups/datadef
     */
    private void setStepFields() {
        List<FieldsBase> fields = beanService.getBeans(FieldsBase.class);
        try {
            List<FieldsBase> clzFields = FieldsUtil.filterByValue(fields,
                    "class", Locator.class.getName());
            List<FieldsBase> stepsGroup =
                    FieldsUtil.filterByGroup(clzFields, "steps");
            stepFields = FieldsUtil.filterByName(stepsGroup, "step");
        } catch (FieldNotFoundException e) {
            stepFields = new ArrayList<>();
            LOGGER.warn("{}", e.getLocalizedMessage());
        }
    }

    /**
     * adds effective fields (after merging steps with group) for a group to
     * cache.
     * @param locatorGroup
     *            {@link String}
     */
    private void addGroupFieldsToMap(final String locatorGroup) {
        List<FieldsBase> groupFields;
        try {
            groupFields = getGroupFieldsWithSteps(locatorGroup);
            locatorFieldsMap.put(locatorGroup, groupFields);
        } catch (FieldNotFoundException e) {
            LOGGER.warn("{}", e.getLocalizedMessage());
            LOGGER.warn("unable to construct group fields for group {}",
                    locatorGroup);
        }
    }

    /**
     * Propagates all common steps to each datadef. If step is not defined at
     * datadef level, then common step is added. In case, step is defined at
     * datadef level then it will have precedence and common step is ignored.
     * Returns effective fields for a locator group.
     * @param locatorGroup
     *            {@link String}
     * @return list merged fields
     * @throws FieldNotFoundException
     *             if either group or datadef fields are not defined.
     */
    private List<FieldsBase> getGroupFieldsWithSteps(final String locatorGroup)
            throws FieldNotFoundException {
        LOGGER.info("merge step fields with datadef fields");
        List<FieldsBase> groupFields = null;
        try {
            groupFields = FieldsUtil.filterByGroup(classFields, locatorGroup);
        } catch (FieldNotFoundException e) {
            LOGGER.warn(
                    "unable to find group {} in locator fields, check fields xml file",
                    locatorGroup);
            throw e;
        }
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
            return groupFields;
        } catch (FieldNotFoundException e) {
            LOGGER.warn(
                    "unable to find datadef for group {} in locator fields, check fields xml file",
                    locatorGroup);
            throw e;
        }
    }
}
