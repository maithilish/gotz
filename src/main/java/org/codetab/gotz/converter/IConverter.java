package org.codetab.gotz.converter;

import org.codetab.gotz.model.XField;

/**
 * <p>
 * Converter interface.
 * @author Maithilish
 *
 * @param <T>
 *            input type
 * @param <U>
 *            output type
 */
public interface IConverter<T, U> {

    /**
     * <p>
     * Convert input type to output.
     * @param input
     *            to convert
     * @return converted output
     * @throws Exception
     *             convert error
     */
    U convert(T input) throws Exception;

    /**
     * <p>
     * Set fields.
     * @param fields
     *            list of fields
     */
    void setXField(XField xField);
}
