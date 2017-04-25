package org.codetab.gotz.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.step.Filter;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.util.FieldsIterator;
import org.codetab.gotz.util.FieldsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataFilter extends Filter {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFilter.class);

    @Override
    public IStep instance() {
        return new DataFilter();
    }

    @Override
    public void filter() throws Exception {
        List<Member> forRemovalMembers = new ArrayList<Member>();
        Map<AxisName, List<FieldsBase>> filterMap = DataDefService.instance()
                .getFilterMap(getData().getDataDef());
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
        DataDefService.instance().traceDataStructure(getData());
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

    private boolean requireFilter(final Axis axis, final List<FieldsBase> filters,
            final String filterGroup) {
        try {
            List<FieldsBase> fil = FieldsUtil.getGroupFields(filters, filterGroup);
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
