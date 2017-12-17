package org.codetab.gotz.dao.jdo;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDataDao;
import org.codetab.gotz.dao.IDataDefDao;
import org.codetab.gotz.dao.IDataSetDao;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ILocatorDao;
import org.codetab.gotz.messages.Messages;

/**
 * <p>
 * Concrete implementation of IDaoFactory which creates family of DAO for JDO -
 * javax.jdo specification.
 * @author Maithilish
 *
 */
public class JdoDaoFactory implements IDaoFactory {

    /**
     * JDO PMF.
     */
    private PMF pmf;

    /**
     * <p>
     * Constructor.
     */
    @Inject
    public JdoDaoFactory() {
    }

    /**
     * <p>
     * Set JDO PMF. If pmf.getFactory() returns null, then initialize PMF and
     * then set it to field.
     * @param pmf
     *            JDO PMF
     */
    @Inject
    public void setPmf(final PMF pmf) {
        Validate.notNull(pmf, Messages.getString("JdoDaoFactory.0")); //$NON-NLS-1$

        if (pmf.getFactory() == null) {
            pmf.init();
        }
        this.pmf = pmf;
    }

    /**
     * Get DocumentDao for JDO.
     * @return documentDao
     */
    @Override
    public IDocumentDao getDocumentDao() {
        return new DocumentDao(pmf.getFactory());
    }

    /**
     * Get DataDefDao for JDO.
     * @return dataDefDao
     */
    @Override
    public IDataDefDao getDataDefDao() {
        return new DataDefDao(pmf.getFactory());
    }

    /**
     * Get DataDao for JDO.
     * @return dataDao
     */
    @Override
    public IDataDao getDataDao() {
        return new DataDao(pmf.getFactory());
    }

    /**
     * Get DataSetDao for JDO.
     * @return dataSetDao
     */
    @Override
    public IDataSetDao getDataSetDao() {
        return new DataSetDao(pmf.getFactory());
    }

    /**
     * Get LocatorDao for JDO.
     * @return locatorDao
     */
    @Override
    public ILocatorDao getLocatorDao() {
        return new LocatorDao(pmf.getFactory());
    }

}
