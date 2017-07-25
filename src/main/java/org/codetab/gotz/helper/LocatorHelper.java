package org.codetab.gotz.helper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorHelper.class);

    @Inject
    private BeanService beanService;
    @Inject
    private ConfigService configService;

    public List<Locator> getLocatorsFromBeans() {
        List<Locator> locatorList = new ArrayList<>();
        LOGGER.info("initialize locators");
        List<Locators> list = beanService.getBeans(Locators.class);
        for (Locators locators : list) {
            trikleGroup(locators);
        }
        for (Locators locators : list) {
            locatorList.addAll(extractLocator(locators));
        }
        return locatorList;
    }

    /*
     * fork locators for load testing
     */
    public List<Locator> forkLocators(final List<Locator> locators) {
        List<Locator> forkedLocators = new ArrayList<>();
        try {
            int count =
                    Integer.parseInt(configService.getConfig("forklocator"));
            for (Locator locator : locators) {
                forkedLocators.add(locator);
                for (int i = 0; i < count; i++) {
                    Locator forkedLocator = SerializationUtils.clone(locator);
                    forkedLocator.setName(locator.getName() + i);
                    forkedLocators.add(forkedLocator);
                }
            }
        } catch (ConfigNotFoundException e) {
        }
        return forkedLocators;
    }

    private List<Locator> extractLocator(final Locators locatorsList) {
        List<Locator> locatorList = new ArrayList<>();
        for (Locators locs : locatorsList.getLocators()) {
            extractLocator(locs);
        }
        for (Locator locator : locatorsList.getLocator()) {
            locatorList.add(locator);
        }
        return locatorList;
    }

    private void trikleGroup(final Locators locators) {
        LOGGER.info("cascade locators group to all locator");
        for (Locators locs : locators.getLocators()) {
            if (locs.getGroup() == null) {
                locs.setGroup(locators.getGroup());
            }
            trikleGroup(locs);
        }
        for (Locator locator : locators.getLocator()) {
            if (locator.getGroup() == null) {
                locator.setGroup(locators.getGroup());
            }
        }
    }
}
