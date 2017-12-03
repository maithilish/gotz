package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Document persistence methods.
 * @author Maithilish
 *
 */
public class DocumentPersistence {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentPersistence.class);

    /**
     * Config service.
     */
    @Inject
    private ConfigService configService;
    /**
     * DaoFactory provider.
     */
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    /**
     * <p>
     * Loads document by id.
     * @param id
     *            document id
     * @return document or null if not found
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Document loadDocument(final long id) {
        // get Document with documentObject
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDocumentDao dao = daoFactory.getDocumentDao();
            return dao.getDocument(id);
        } catch (RuntimeException e) {
            LOGGER.error("{}", e.getMessage());
            LOGGER.trace("", e);
            String message = Util.join("unable to load Document[id=",
                    String.valueOf(id), "]");
            throw new StepPersistenceException(message, e);
        }
    }

}
