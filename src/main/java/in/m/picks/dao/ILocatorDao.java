package in.m.picks.dao;

import in.m.picks.model.Locator;

public interface ILocatorDao {

    Locator getLocator(String name, String group);

    void storeLocator(Locator locator);

    Locator getLocator(Long id);
}
