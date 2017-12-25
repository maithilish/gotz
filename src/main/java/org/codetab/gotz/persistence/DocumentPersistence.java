package org.codetab.gotz.persistence;

import javax.inject.Inject;

import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.IDaoFactory;
import org.codetab.gotz.dao.IDocumentDao;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;

/**
 * <p>
 * Document persistence methods.
 * @author Maithilish
 *
 */
public class DocumentPersistence {

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
        if (!configService.isPersist("gotz.useDatastore")) { //$NON-NLS-1$
            return null;
        }

        // get Document with documentObject
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDocumentDao dao = daoFactory.getDocumentDao();
            return dao.getDocument(id);
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("DocumentPersistence.1"), //$NON-NLS-1$
                            String.valueOf(id), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }

}
