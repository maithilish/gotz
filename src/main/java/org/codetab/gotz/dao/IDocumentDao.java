package org.codetab.gotz.dao;

import org.codetab.gotz.model.Document;

/**
 * <p>
 * DocumentDao interface.
 * @author Maithilish
 *
 */
public interface IDocumentDao {

    /**
     * <p>
     * Get Document by id.
     * @param id
     *            document id
     * @return document
     */
    Document getDocument(Long id);

}
