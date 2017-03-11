package org.codetab.nscoop.dao;

import org.codetab.nscoop.model.Data;

public interface IDataDao {

    void storeData(Data data);

    Data getData(Long documentId, Long dataDefId);

    Data getData(Long id);

}
