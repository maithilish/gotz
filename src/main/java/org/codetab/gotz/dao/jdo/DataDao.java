package org.codetab.gotz.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataDao implements IDataDao {

    static final Logger LOGGER = LoggerFactory.getLogger(DataDao.class);

    private PersistenceManagerFactory pmf;

    public DataDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    @Override
    public void storeData(final Data data) {
        Validate.notNull(data, "data must not be null");
        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(data);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Override
    public Data getData(final Long documentId, final Long dataDefId) {
        PersistenceManager pm = getPM();
        List<Data> data = null;
        try {
            String filter = "documentId == rId && dataDefId == dId";
            String paramDecla = "Long rId,Long dId";
            Extent<Data> extent = pm.getExtent(Data.class);
            Query<Data> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);
            query.setParameters(documentId, dataDefId);
            data = query.executeList();
            pm.getFetchPlan().addGroup("detachMembers");
            data = (List<Data>) pm.detachCopyAll(data);
        } finally {
            pm.close();
        }
        switch (data.size()) {
        case 0:
            return null;
        case 1:
            return data.get(0);
        default:
            throw new IllegalStateException(
                    "found multiple data for [documentId][dataDefId] ["
                            + documentId + "][" + dataDefId + "]");
        }
    }

    @Override
    public Data getData(final Long id) {
        PersistenceManager pm = getPM();
        Data data = null;
        try {
            Object result = pm.getObjectById(Data.class, id);
            pm.getFetchPlan().addGroup("detachMembers");
            data = (Data) pm.detachCopy(result);
        } finally {
            pm.close();
        }
        return data;
    }

    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        LOGGER.trace("returning PM : {}", pm);
        return pm;
    }
}
