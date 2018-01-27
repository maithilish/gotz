package org.codetab.gotz.model.helper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides list of locator. It obtains locators from BeanService and propagate
 * group to all locators. It also fork locator for load testing.
 *
 * @author Maithilish
 *
 */
public class LocatorHelper {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorHelper.class);

    /**
     * BeanService singleton.
     */
    @Inject
    private BeanService beanService;

    /**
     * ConfigService singleton.
     */
    @Inject
    private ConfigService configService;

    /**
     * LocatorFieldsHelper
     */
    @Inject
    private LocatorFieldsHelper locatorFieldsHelper;

    /**
     * private constructor.
     */
    @Inject
    private LocatorHelper() {
    }

    /**
     * obtains locators from BeanService, trickle groups and creates list of
     * locators.
     * @return list of locators
     */
    public List<Locator> getLocatorsFromBeans() {
        LOGGER.info(Messages.getString("LocatorHelper.0")); //$NON-NLS-1$

        Validate.validState(beanService != null,
                Messages.getString("LocatorHelper.1")); //$NON-NLS-1$

        List<Locator> locatorList = new ArrayList<>();
        List<Locators> list = beanService.getBeans(Locators.class);
        for (Locators locators : list) {
            trikleGroup(locators);
        }
        for (Locators locators : list) {
            locatorList.addAll(extractLocator(locators));
        }
        return locatorList;
    }

    /**
     * If gotz.fork.locator config is set, returns list of original locator plus
     * forked locators else returns empty list.
     * @param locators
     *            - original list of locator
     * @return list of locators - original plus forked
     */
    public List<Locator> forkLocators(final List<Locator> locators) {
        Validate.notNull(locators, Messages.getString("LocatorHelper.2")); //$NON-NLS-1$
        Validate.validState(configService != null,
                Messages.getString("LocatorHelper.3")); //$NON-NLS-1$

        List<Locator> forkedLocators = new ArrayList<>();
        try {
            int count = Integer
                    .parseInt(configService.getConfig("gotz.fork.locator")); //$NON-NLS-1$
            if (count > 0) {
                for (Locator locator : locators) {
                    forkedLocators.add(locator);
                    for (int i = 0; i < count; i++) {
                        Locator forkedLocator =
                                SerializationUtils.clone(locator);
                        forkedLocator.setName(locator.getName() + i);
                        forkedLocators.add(forkedLocator);
                    }
                }
            }
        } catch (ConfigNotFoundException e) {
        }
        return forkedLocators;
    }

    /**
     * locators contains list of locator and also locators. This will
     * recursively extract all locator into a flat list.
     * @param locatorsList
     *            - list of locators
     * @return list of locator
     */
    private List<Locator> extractLocator(final Locators locatorsList) {
        LOGGER.info(Messages.getString("LocatorHelper.5")); //$NON-NLS-1$

        List<Locator> locatorList = new ArrayList<>();
        // for (Locators locs : locatorsList.getLocators()) {
        // locatorList.addAll(extractLocator(locs));
        // }
        for (Locator locator : locatorsList.getLocator()) {
            locatorList.add(locator);
        }
        return locatorList;
    }

    /**
     * locators contains list of locator and also locators. This will
     * recursively trickle group to all locator.
     *
     * @param locators
     *            - list of locators
     */
    private void trikleGroup(final Locators locators) {
        LOGGER.info(Messages.getString("LocatorHelper.6")); //$NON-NLS-1$

        // for (Locators locs : locators.getLocators()) {
        // if (locs.getGroup() == null) {
        // locs.setGroup(locators.getGroup());
        // }
        // trikleGroup(locs);
        // }
        for (Locator locator : locators.getLocator()) {
            if (locator.getGroup() == null) {
                locator.setGroup(locators.getGroup());
            }
        }
    }

    public Labels createLabels(final Locator locator) {
        return new Labels(locator.getName(), locator.getGroup());
    }

    public Locator createLocator(final Member member, final String locatorName,
            final String label)
            throws FieldsException, InvalidDataDefException {
        Locator locator = new Locator();
        locator.setName(locatorName);
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.join("[", label, "] ", //$NON-NLS-1$
                    Messages.getString("LocatorCreator.1"));
            throw new InvalidDataDefException(message);
        } else {
            locator.setGroup(member.getGroup());
            Fields fields = locatorFieldsHelper.getFields(
                    locator.getClass().getName(), locator.getGroup());
            locator.setFields(fields);
            if (member.getFields() != null) {
                locator.getFields().getNodes()
                        .addAll(member.getFields().getNodes());
            }
        }
        return locator;
    }
}
