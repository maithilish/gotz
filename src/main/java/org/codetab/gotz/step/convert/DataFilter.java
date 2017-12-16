package org.codetab.gotz.step.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseConverter;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Filters data.
 * @author Maithilish
 *
 */
public final class DataFilter extends BaseConverter {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataFilter.class);

    /**
     * to trace changes
     */
    private Set<String> filteredSet = new HashSet<>();

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

        LOGGER.info(getLabeled("apply filters"));

        int membersCountBefore = getData().getMembers().size();

        List<Member> forRemovalMembers = new ArrayList<Member>();
        Map<AxisName, Fields> filterMap = null;
        try {
            filterMap = dataDefService.getFilterMap(getData().getDataDef());
        } catch (DataDefNotFoundException e) {
            String message = "unable to filter";
            throw new StepRunException(message, e);
        }
        for (Member member : getData().getMembers()) {
            for (Axis axis : member.getAxes()) {
                if (requireFilter(axis, filterMap)) {
                    forRemovalMembers.add(member);
                    break;
                }
            }
        }

        LOGGER.trace(getMarker(), "filtered members");

        for (Member member : forRemovalMembers) {
            getData().getMembers().remove(member);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(getMarker(), "{}", member);
                filteredSet.add(getValuesAsString(member));
            }
        }
        if (getData().getMembers().size() == 0) {
            setConsistent(false);
            String message =
                    "output is empty, check datadef query/indexRange/breakAfter/filter";
            throw new StepRunException(message);
        }

        int memberCountAfter = getData().getMembers().size();
        traceChanges(membersCountBefore, memberCountAfter);

        setConvertedData(getData());
        setConsistent(true);
        setStepState(StepState.PROCESS);
        return true;
    }

    private void traceChanges(final int membersCountBefore,
            final int memberCountAfter) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        LOGGER.trace(getMarker(), "filter summary");
        LOGGER.trace(getMarker(),
                "data members count : before [{}] and after [{}]",
                membersCountBefore, memberCountAfter);
        LOGGER.trace(getMarker(), "items filtered");
        for (String item : filteredSet) {
            LOGGER.trace(getMarker(), "  {}", item);
        }
    }

    private String getValuesAsString(final Member member) {
        StringBuilder sb = new StringBuilder();
        AxisName[] axisNames = {AxisName.COL, AxisName.ROW, AxisName.FACT};
        for (AxisName axisName : axisNames) {
            String value = member.getAxis(axisName).getValue();
            sb.append("[");
            sb.append(value);
            sb.append("]");
        }
        return sb.toString();
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
            final Map<AxisName, Fields> filterMap) {
        Fields filters = filterMap.get(axis.getName());
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
     * @param fields
     *            filter fields from datadef
     * @param filterGroup
     *            if "match" then axis.getMatch() is compared with field value
     *            if "value" then axis.getValue() is compared with field value
     * @return true if axis candidate for filter.
     */
    private boolean requireFilter(final Axis axis, final Fields fields,
            final String filterType) {
        String value = "";
        if (filterType.equals("match")) {
            value = axis.getMatch();
        }
        if (filterType.equals("value")) {
            value = axis.getValue();
        }
        if (value == null) {
            return false;
        }
        try {
            String xpath = Util.join("/xf:filters[@type='", filterType,
                    "']/xf:filter/@pattern");
            // include blanks also in patterns
            List<String> patterns = fieldsHelper.getValues(xpath, true, fields);
            for (String pattern : patterns) {
                if (value.equals(pattern)) {
                    return true;
                }
                try {
                    if (Pattern.matches(pattern, value)) {
                        return true;
                    }
                } catch (PatternSyntaxException e) {
                    String message = Util.join("unable to filter", pattern);
                    throw new StepRunException(message, e);
                }
            }
        } catch (FieldsNotFoundException e) {
        }
        return false;
    }
}
