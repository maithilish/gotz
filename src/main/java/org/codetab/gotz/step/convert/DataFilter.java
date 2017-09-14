package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.iterator.FieldsIterator;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseFilter;
import org.codetab.gotz.util.FieldsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Filters data.
 * @author Maithilish
 *
 */
public final class DataFilter extends BaseFilter {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataFilter.class);

    @Override
    public IStep instance() {
        return this;
    }

    /**
     * Filters data. Obtains filter fields from DataDefService and creates a
     * list of members for removal and removes matching members from data.
     */
    @Override
    public boolean process() {
        Validate.validState(getData() != null, "data must not be null");

        List<Member> forRemovalMembers = new ArrayList<Member>();
        Map<AxisName, List<FieldsBase>> filterMap = null;
        try {
            filterMap = dataDefService.getFilterMap(getData().getDataDef());
        } catch (DataDefNotFoundException e) {
            String givenUpMessage = "unable to filter";
            LOGGER.error("{} {}", givenUpMessage, e.getLocalizedMessage());
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
            throw new StepRunException(givenUpMessage, e);
        }
        for (Member member : getData().getMembers()) {
            for (Axis axis : member.getAxes()) {
                if (requireFilter(axis, filterMap)) {
                    forRemovalMembers.add(member);
                    break;
                }
            }
        }
        for (Member member : forRemovalMembers) {
            getData().getMembers().remove(member);
        }
        dataDefService.traceDataStructure(getData().getDataDef(), getData());
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    /**
     * <p>
     * Tells whether axis a candidate for filter. Axis can be filter based on
     * match field or value field - see
     * {@link DataFilter#requireFilter(Axis, List, String)}. Return true if
     * require filter based on Axis match field else checks require filter based
     * on Axis value field otherwise returns false.
     * @param axis
     *            to check
     * @param filterMap
     *            map of filter fields
     * @return true if axis candidate for filter otherwise false
     */
    private boolean requireFilter(final Axis axis,
            final Map<AxisName, List<FieldsBase>> filterMap) {
        List<FieldsBase> filters = filterMap.get(axis.getName());
        if (filters == null) {
            return false;
        }
        if (requireFilter(axis, filters, "match")) {
            return true;
        }
        if (requireFilter(axis, filters, "value")) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Checks axis against filter fields and group and return true if axis
     * candidate for filter.
     * @param axis
     *            to filter
     * @param filters
     *            filter fields from datadef
     * @param filterGroup
     *            if "match" then axis.getMatch() is compared with field value
     *            if "value" then axis.getValue() is compared with field value
     * @return true if axis candidate for filter.
     */
    private boolean requireFilter(final Axis axis,
            final List<FieldsBase> filters, final String filterGroup) {
        try {
            List<FieldsBase> fil =
                    FieldsUtil.filterByGroup(filters, filterGroup);
            FieldsIterator ite = new FieldsIterator(fil);
            while (ite.hasNext()) {
                FieldsBase field = ite.next();
                if (field instanceof Field) {
                    String value = "";
                    if (filterGroup.equals("match")) {
                        value = axis.getMatch();
                    }
                    if (filterGroup.equals("value")) {
                        value = axis.getValue();
                    }
                    if (value == null) {
                        return false;
                    }
                    if (value.equals(field.getValue())) {
                        return true;
                    }
                }
            }
        } catch (FieldNotFoundException e) {
        }
        return false;
    }
}
