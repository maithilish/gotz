package in.m.picks.dao;

import in.m.picks.model.Data;

public interface IDataDao {

    void storeData(Data data);

    Data getData(Long documentId, Long dataDefId);

    Data getData(Long id);

}
