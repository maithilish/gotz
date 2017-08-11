package org.codetab.gotz.dao;

import javax.inject.Inject;

import org.codetab.gotz.di.DInjector;

/**
 * DAO Factory. Applies Abstract Factory Design Pattern to provide an interface
 * to for creating families of DAO such as DocumentDao, LocatorDao etc., without
 * specifying their concrete classes.
 * @author m
 *
 */
public class DaoFactory {

    /**
     * DI.
     */
    @Inject
    private DInjector dInjector;

    /**
     * DaoFactory constructor.
     * <p>
     *
     */
    @Inject
    public DaoFactory() {
    }

    /**
     * <p>
     * Get instance of DaoFactory of ORM type. At present, only JDO is
     * implemented.
     * @param orm
     *            type JDO or JPA
     * @return DaoFactory of JDO
     * @throws UnsupportedOperationException
     *             if ORM type is JPA
     */
    public DaoFactory getDaoFactory(final ORM orm) {
        DaoFactory instance = null;
        switch (orm) {
        case JDO:
            instance = dInjector
                    .instance(org.codetab.gotz.dao.jdo.JdoDaoFactory.class);
            break;
        case JPA:
            throw new UnsupportedOperationException("JPA not yet supported");
        default:
            instance = dInjector
                    .instance(org.codetab.gotz.dao.jdo.JdoDaoFactory.class);
            break;
        }
        return instance;
    }

    /**
     * <p>
     * Subclass should override this.
     * @return locatorDao
     */
    public ILocatorDao getLocatorDao() {
        throw new UnsupportedOperationException(
                "subclass should override this method");
    }

    /**
     * <p>
     * Subclass should override this.
     * @return documentDao
     */
    public IDocumentDao getDocumentDao() {
        throw new UnsupportedOperationException(
                "subclass should override this method");
    }

    /**
     * <p>
     * Subclass should override this.
     * @return dataDefDao
     */
    public IDataDefDao getDataDefDao() {
        throw new UnsupportedOperationException(
                "subclass should override this method");
    }

    /**
     * <p>
     * Subclass should override this.
     * @return dataDao
     */
    public IDataDao getDataDao() {
        throw new UnsupportedOperationException(
                "subclass should override this method");
    }
}
