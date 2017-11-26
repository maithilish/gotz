package org.codetab.gotz.step.load.encoder;

import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;

/**
 * <p>
 * Data Encoder interface.
 * @author Maithilish
 *
 * @param <T>
 *            input type
 * @param <U>
 *            output type
 */
public interface IEncoder<U> {

    /**
     * <p>
     * Encode Data.
     * @param data
     *            to encode
     * @return encoded output
     * @throws Exception
     *             encode error
     */
    U encode(Data data) throws Exception;

    /**
     * <p>
     * Set fields.
     * @param fields
     *            list of fields
     */
    void setFields(Fields fields);
}
