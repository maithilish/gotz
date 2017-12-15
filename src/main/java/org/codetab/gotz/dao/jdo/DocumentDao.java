package org.codetab.gotz.dao.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JDO DocumentDao implementation.
 * @author Maithilish
 *
 */
public final class DocumentDao implements IDocumentDao {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentDao.class);

    /**
     * JDO PMF.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     * @param pmf
     *            JDO PMF
     */
    public DocumentDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Get document (detached copy) from id. It fetches document with
     * documentObject (i.e. actual contents).
     * @param id
     * @return document
     */
    @Override
    public Document getDocument(final long id) {
        Document document = null;
        PersistenceManager pm = getPM();
        try {
            Object result = pm.getObjectById(Document.class, id);
            // document with documentObject
            pm.getFetchPlan().addGroup("detachDocumentObject");
            document = (Document) pm.detachCopy(result);
        } finally {
            pm.close();
        }
        return document;
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
