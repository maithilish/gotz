package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.DataDefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines to handle documents.
 * @author Maithilish
 *
 */
public class DataHelper {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Data.class);

    /**
     * DataDefService singleton.
     */
    @Inject
    private DataDefService dataDefService;

    @Inject
    private FieldsHelper fieldsHelper;

    /**
     * private constructor.
     */
    @Inject
    private DataHelper() {
    }

    public Data getDataTemplate(final String dataDefName, final long documentId,
            final String label) throws DataDefNotFoundException,
            ClassNotFoundException, IOException {
        Data data = dataDefService.getDataTemplate(dataDefName);
        data.setName(label);
        data.setDataDefId(dataDefService.getDataDef(dataDefName).getId());
        data.setDocumentId(documentId);
        return data;
    }

    /**
     * Deep copy member, but for performance fields are not cloned instead
     * reference is passed to copy.
     * @param member
     * @return
     */
    public Member createMember(final Member member) {
        Member cMember = new Member();
        cMember.setName(member.getName());
        cMember.setGroup(member.getGroup());
        for (Axis axis : member.getAxes()) {
            Axis cAxis = new Axis();
            cAxis.setName(axis.getName());
            cAxis.setMatch(axis.getMatch());
            cAxis.setOrder(Integer.valueOf(axis.getOrder()));
            cAxis.setIndex(Integer.valueOf(axis.getIndex()));
            cAxis.setValue(axis.getValue());
            cAxis.setFields(axis.getFields());
            cMember.addAxis(cAxis);
        }
        cMember.setFields(member.getFields());
        return cMember;
    }

    public boolean hasFinished(final Axis axis, final int endIndex)
            throws NumberFormatException, FieldsException {
        boolean noField = true;
        try {
            // xpath - not abs path
            String breakAfter = fieldsHelper
                    .getLastValue("//xf:breakAfter/@value", axis.getFields()); //$NON-NLS-1$
            noField = false;
            String value = axis.getValue();
            if (value == null) {
                String message = Messages.getString("BaseParser.48"); //$NON-NLS-1$
                throw new StepRunException(message);
            } else {
                if (value.equals(breakAfter)) {
                    return true;
                }
            }
        } catch (FieldsNotFoundException e) {
        }

        if (endIndex >= 0) {
            noField = false;
            if (axis.getIndex() + 1 > endIndex) {
                return true;
            }
        }

        if (noField) {
            String message = Messages.getString("BaseParser.49"); //$NON-NLS-1$
            throw new FieldsException(message);
        }
        return false;
    }

    public Integer[] nextMemberIndexes(final Member member,
            final AxisName axisName) {
        Integer[] indexes = getMemberIndexes(member);
        indexes[axisName.ordinal()] = indexes[axisName.ordinal()] + 1;
        return indexes;
    }

    public boolean alreadyProcessed(final Set<Integer[]> memberIndexSet,
            final Integer[] memberIndexes) {
        for (Integer[] indexes : memberIndexSet) {
            boolean processed = true;
            for (int i = 0; i < AxisName.values().length; i++) {
                int index = indexes[i];
                int memberIndex = memberIndexes[i];
                if (index != memberIndex) {
                    processed = false;
                }
            }
            if (processed) {
                return processed;
            }
        }
        return false;
    }

    public Integer[] getMemberIndexes(final Member member) {
        Integer[] memberIndexes = new Integer[AxisName.values().length];
        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            int index = 0;
            try {
                axis = member.getAxis(axisName);
                index = new Integer(axis.getIndex());
            } catch (NoSuchElementException e) {
            }
            memberIndexes[axisName.ordinal()] = index;
        }
        return memberIndexes;
    }

}
