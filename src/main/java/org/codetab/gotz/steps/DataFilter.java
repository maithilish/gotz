package org.codetab.gotz.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.stepbase.BaseFilter;
import org.codetab.gotz.util.FieldsIterator;
import org.codetab.gotz.util.OFieldsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataFilter extends BaseFilter {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFilter.class);

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean process() {
        List<Member> forRemovalMembers = new ArrayList<Member>();
        Map<AxisName, List<FieldsBase>> filterMap = null;
        try {
            filterMap = dataDefService.getFilterMap(getData().getDataDef());
        } catch (IllegalArgumentException | DataDefNotFoundException e) {
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
        dataDefService.traceDataStructure(getData());
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

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

    private boolean requireFilter(final Axis axis,
            final List<FieldsBase> filters, final String filterGroup) {
        try {
            List<FieldsBase> fil =
                    OFieldsUtil.getGroupFields(filters, filterGroup);
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
