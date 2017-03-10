package in.m.picks.dao;

import java.util.Date;
import java.util.List;

import in.m.picks.model.DataDef;

public interface IDataDefDao {

    void storeDataDef(DataDef dataDef);

    List<DataDef> getDataDefs(Date date);

}
