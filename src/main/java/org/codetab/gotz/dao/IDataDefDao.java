package org.codetab.gotz.dao;

import java.util.Date;
import java.util.List;

import org.codetab.gotz.model.DataDef;

public interface IDataDefDao {

    void storeDataDef(DataDef dataDef);

    List<DataDef> getDataDefs(Date date);

    DataDef getDataDef(String name, Date date);

    List<DataDef> getDataDefs(String name);
}
