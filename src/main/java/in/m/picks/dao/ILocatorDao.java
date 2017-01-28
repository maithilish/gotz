package in.m.picks.dao;

import in.m.picks.model.Locator;

public interface ILocatorDao {

	public Locator getLocator(String name, String group);

	public void storeLocator(Locator locator);

	public Locator getLocator(Long id);
}
