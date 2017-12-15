package org.codetab.gotz.dao.jdo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.IDataSetDao;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JDO DataSetDao implementation.
 * @author Maithilish
 *
 */
public final class DataSetDao implements IDataSetDao {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataSetDao.class);

    /**
     * persistence manager factory.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * DataDao constructor.
     * @param pmf
     *            persistence manager factory
     */
    public DataSetDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Store dataSet. If name and group are not unique then dataSets is not
     * persisted and exception is thrown. Otherwise, it compares existing
     * dataSets with the input dataSets and creates a list of items which don't
     * exists in store. Finally, the created list is bulk persisted.
     * @param dataSets
     *            list to store
     * @throws StepPersistenceException
     *             if name and group is not unique
     */
    @Override
    public void storeDataSets(final List<DataSet> dataSets) {
        Validate.notNull(dataSets, "dataSet must not be null");

        // TODO filter on col or row
        List<String> names = dataSets.stream().map(DataSet::getName).distinct()
                .collect(Collectors.toList());
        List<String> groups = dataSets.stream().map(DataSet::getGroup)
                .distinct().collect(Collectors.toList());

        if (names.size() != 1 || groups.size() != 1) {
            String message = Util.join(
                    "unable persist dataset as name or group are not unique",
                    names.toString(), groups.toString());
            throw new StepPersistenceException(message);
        }

        String name = names.get(0);
        String group = groups.get(0);

        List<DataSet> pDataSets = new ArrayList<>();

        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            String filter = "name == pname && group == pgroup";
            String paramDecla = "String pname,String pgroup";
            Extent<DataSet> extent = pm.getExtent(DataSet.class);
            Query<DataSet> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);
            query.setParameters(name, group);
            List<DataSet> oDataSets = query.executeList();
            oDataSets = (List<DataSet>) pm.detachCopyAll(oDataSets);

            for (DataSet dataSet : dataSets) {
                if (!oDataSets.contains(dataSet)) {
                    pDataSets.add(dataSet);
                }
            }

            for (DataSet dataSet : pDataSets) {
                pm.makePersistent(dataSet);
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    /**
     * <p>
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        return pm;
    }
}
