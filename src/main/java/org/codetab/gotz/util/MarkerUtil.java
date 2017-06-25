package org.codetab.gotz.util;

import java.util.List;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class MarkerUtil {

    private static final Marker LOG_STATE = MarkerFactory.getMarker("LOG_STATE");
    private static final Marker NORMAL = MarkerFactory.getMarker("NORMAL");

    public static Marker getMarker(final List<FieldsBase> fields) {
        try {
            if (OFieldsUtil.isFieldTrue(fields, "logstate")) {
                return LOG_STATE;
            } else {
                return NORMAL;
            }
        } catch (FieldNotFoundException e) {
            return NORMAL;
        }
    }

    public static Marker getMarker(final String name, final String group) {
        String markerName = Util.buildString("LOG", "_", name.toUpperCase(), "_",
                group.toUpperCase());
        return MarkerFactory.getMarker(markerName);
    }

    public static Marker getMarker(final String name, final String group,
            final String dataDef) {
        String markerName = Util.buildString("LOG", "_", name.toUpperCase(), "_",
                group.toUpperCase());
        if (dataDef != null) {
            markerName = Util.buildString(markerName, "_", dataDef.toUpperCase());
        }
        return MarkerFactory.getMarker(markerName);
    }
}
