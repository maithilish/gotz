package in.m.picks.dao;

import in.m.picks.model.Data;

public interface IDataDao {

	public void storeData(Data data);

	public Data getData(Long documentId, Long dataDefId);

	public Data getData(Long id);

}
