package in.m.picks.dao.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.dao.IDocumentDao;
import in.m.picks.model.Document;

public class DocumentDao implements IDocumentDao {

	final static Logger logger = LoggerFactory.getLogger(DocumentDao.class);

	private PersistenceManagerFactory pmf;

	public DocumentDao(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
		if (pmf == null)
			logger.error(
					"loading JDO Dao failed as PersistenceManagerFactory is null");
	}

	@Override
	public Document getDocument(Long id) {
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
		logger.trace("returning PM : {}", pm);
		return pm;
	}

}
