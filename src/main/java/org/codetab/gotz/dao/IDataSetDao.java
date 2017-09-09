package org.codetab.gotz.dao;

import java.util.List;

import org.codetab.gotz.model.DataSet;

/**
 * <p>
 * DataSetDao interface.
 * @author Maithilish
 *
 */
public interface IDataSetDao {
    /**
     * <p>
     * Store dataSet.
     * @param dataSet
     *            to store
     */
    void storeDataSets(List<DataSet> dataSets);

}
