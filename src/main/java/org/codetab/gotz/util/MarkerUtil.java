package org.codetab.gotz.util;

import org.apache.commons.lang3.Validate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>
 * Provides log marker for trace log.
 * @author Maithilish
 *
 */
public final class MarkerUtil {

    /**
     * <p>
     * Private constructor.
     */
    private MarkerUtil() {
    }

    /**
     * <p>
     * Get marker for name-group.
     * @param name
     *            name, not null
     * @param group
     *            group, not null
     * @return marker
     */
    public static Marker getMarker(final String name, final String group) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

        String markerName = Util.join("LOG", "_", name.toUpperCase(), "_",
                group.toUpperCase());
        return MarkerFactory.getMarker(markerName);
    }

    /**
     * <p>
     * Get marker for name-group-datadef.
     * @param name
     *            name, not null
     * @param group
     *            group, not null
     * @param dataDefName
     *            datadef name
     * @return marker
     */
    public static Marker getMarker(final String name, final String group,
            final String dataDefName) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

        String markerName = Util.join("LOG", "_", name.toUpperCase(), "_",
                group.toUpperCase());
        if (dataDefName != null) {
            markerName = Util.join(markerName, "_", dataDefName.toUpperCase());
        }
        return MarkerFactory.getMarker(markerName);
    }

    /**
     * <p>
     * Get marker for datadef.
     * @param dataDefName
     *            datadef name, not null
     * @return marker
     */
    public static Marker getMarker(final String dataDefName) {

        Validate.notNull(dataDefName, "dataDefName must not be null");

        String markerName = Util.join("LOG", "_", dataDefName.toUpperCase());
        return MarkerFactory.getMarker(markerName);
    }
}
