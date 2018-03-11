package org.codetab.gotz.model.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BreakAfterHelper {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Data.class);

    @Inject
    private FieldsHelper fieldsHelper;

    private Map<AxisName, List<String>> breakAftersCache;

    /**
     * private constructor.
     */
    @Inject
    private BreakAfterHelper() {
        breakAftersCache = new HashMap<>();

    }

    public boolean hasFinished(final Axis axis, final int endIndex)
            throws NumberFormatException, FieldsException {
        boolean noField = true;
        try {
            List<String> breakAfters = null;
            AxisName axisName = axis.getName();
            if (breakAftersCache.containsKey(axisName)) {
                breakAfters = breakAftersCache.get(axisName);
            } else {
                // xpath - not abs path
                breakAfters = fieldsHelper.getValues("//xf:breakAfter/@value", //$NON-NLS-1$
                        false, axis.getFields());
                breakAftersCache.put(axisName, breakAfters);
            }
            noField = false;
            String value = axis.getValue();
            if (value == null) {
                String message = Messages.getString("BaseParser.48"); //$NON-NLS-1$
                throw new StepRunException(message);
            } else {
                for (String breakAfter : breakAfters) {
                    if (value.equals(breakAfter)) {
                        return true;
                    }
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
}
