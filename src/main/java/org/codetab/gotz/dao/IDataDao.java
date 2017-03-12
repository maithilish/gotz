package org.codetab.gotz.dao;

import org.codetab.gotz.model.Data;

public interface IDataDao {

    void storeData(Data data);

    Data getData(Long documentId, Long dataDefId);

    Data getData(Long id);

}
