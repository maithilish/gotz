package org.codetab.nscoop.dao;

import java.util.Date;
import java.util.List;

import org.codetab.nscoop.model.DataDef;

public interface IDataDefDao {

    void storeDataDef(DataDef dataDef);

    List<DataDef> getDataDefs(Date date);

}
