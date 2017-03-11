package in.m.picks.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.Field;
import in.m.picks.model.FieldsBase;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.Filter;
import in.m.picks.step.IStep;
import in.m.picks.util.FieldsIterator;
import in.m.picks.util.FieldsUtil;

public final class DataFilter extends Filter {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFilter.class);

    @Override
    public IStep instance() {
        return new DataFilter();
    }

    @Override
    public void filter() throws Exception {
        List<Member> forRemovalMembers = new ArrayList<Member>();
        Map<AxisName, List<FieldsBase>> filterMap = DataDefService.INSTANCE
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
        DataDefService.INSTANCE.traceDataStructure(getData());
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
