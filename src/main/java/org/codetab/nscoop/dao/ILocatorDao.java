package org.codetab.nscoop.dao;

import org.codetab.nscoop.model.Locator;

public interface ILocatorDao {

    Locator getLocator(String name, String group);

    void storeLocator(Locator locator);

    Locator getLocator(Long id);
}
