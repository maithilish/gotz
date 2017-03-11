package org.codetab.nscoop.dao.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.nscoop.dao.IDocumentDao;
import org.codetab.nscoop.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DocumentDao implements IDocumentDao {

    static final Logger LOGGER = LoggerFactory.getLogger(DocumentDao.class);

    private PersistenceManagerFactory pmf;

    public DocumentDao(final PersistenceManagerFactory pmf) {
        this.pmf = pmf;
        if (pmf == null) {
            LOGGER.error("loading JDO Dao failed as PersistenceManagerFactory is null");
        }
    }

    @Override
    public Document getDocument(final Long id) {
        PersistenceManager pm = getPM();
        try {
            Object result = pm.getObjectById(Document.class, id);
            // document without documentObject
            pm.getFetchPlan().addGroup("detachDocumentObject");
            return (Document) pm.detachCopy(result);
        } finally {
            pm.close();
        }
    }

    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        LOGGER.trace("returning PM : {}", pm);
        return pm;
    }

}
