package org.codetab.gotz.dao;

import org.codetab.gotz.model.Locator;

public interface ILocatorDao {

    Locator getLocator(String name, String group);

    void storeLocator(Locator locator);

    Locator getLocator(Long id);
}
