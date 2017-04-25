package org.codetab.gotz.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

public interface IDaoUtil {

    Properties getDbConfig() throws IOException;

    void executeQuery(PersistenceManagerFactory pmf, String query) throws SQLException;

    void dropConstraint(PersistenceManagerFactory pmf, String table, String constraint)
            throws SQLException;

    void deleteSchemaForClasses(HashSet<String> schemaClasses);

    void createSchemaForClasses(HashSet<String> schemaClasses);

    void clearCache();

    <T> List<T> getObjects(Class<T> ofClass, List<String> detachGroups);

}
