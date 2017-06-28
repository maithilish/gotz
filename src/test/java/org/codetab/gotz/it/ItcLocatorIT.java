package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.ListAppender;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.AppenderService;
import org.junit.Test;

public class ItcLocatorIT {

    @Test
    public void itcPlTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/locator/bean.xml");

        DInjector dInjector = new DInjector().instance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        AppenderService appenderService =
                dInjector.instance(AppenderService.class);
        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<String> actual = new ArrayList<>();
        List<Object> locators = listAppender.getList();
        for (Object obj : locators) {
            Locator locator = (Locator) obj;
            actual.add(locator.getUrl());
        }

        List<String> expected = Arrays.asList(
                "http://www.moneycontrol.com/financials/itc/balance-sheet/ITC#ITC",
                "http://www.moneycontrol.com/financials/itc/profit-loss/ITC#ITC");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
