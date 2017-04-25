package org.codetab.gotz.dao.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DocumentDao implements IDocumentDao {

    static final Logger LOGGER = LoggerFactory.getLogger(DocumentDao.class);

    private PersistenceManagerFactory pmf;

    public DocumentDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    @Override
    public Document getDocument(final Long id) {
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

    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        LOGGER.trace("returning PM : {}", pm);
        return pm;
    }

}
