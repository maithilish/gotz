package org.codetab.gotz.util;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class MarkerUtil {

    private MarkerUtil() {
    }

    public static Marker getMarker(final String name, final String group) {
        String markerName = Util.buildString("LOG", "_", name.toUpperCase(),
                "_", group.toUpperCase());
        return MarkerFactory.getMarker(markerName);
    }

    public static Marker getMarker(final String name, final String group,
            final String dataDefName) {
        String markerName = Util.buildString("LOG", "_", name.toUpperCase(),
                "_", group.toUpperCase());
        if (dataDefName != null) {
            markerName = Util.buildString(markerName, "_",
                    dataDefName.toUpperCase());
        }
        return MarkerFactory.getMarker(markerName);
    }

    public static Marker getMarker(final String dataDefName) {
        String markerName =
                Util.buildString("LOG", "_", dataDefName.toUpperCase());
        return MarkerFactory.getMarker(markerName);
    }
}
