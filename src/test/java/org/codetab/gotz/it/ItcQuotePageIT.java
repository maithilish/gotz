package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.ListAppender;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Test;

public class ItcQuotePageIT {

    @Test
    public void itcQuotePageJSoupTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/quotepage/bean.xml");

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

        List<Object> actual = listAppender.getList();

        ConfigService configService = dInjector.instance(ConfigService.class);
        String prefix = "ITC |quote |" + configService.getRunDateTime() + " |";
        List<String> expected = getJSoupExpected(prefix);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void itcQuotePageHtmlUnitTest() {

        System.setProperty("gotz.beanFile",
                "/itest/itc/quotepage/htmlunit/bean.xml");

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

        List<Object> actual = listAppender.getList();

        ConfigService configService = dInjector.instance(ConfigService.class);
        String prefix = "ITC |quote |" + configService.getRunDateTime() + " |";
        List<String> expected = getHtmlUnitExpected(prefix);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    private List<String> getHtmlUnitExpected(final String prefix) {
        List<String> expected = new ArrayList<>();
        expected.add(prefix + "EPS (TTM) |* -");
        addExpected(prefix, expected);
        return expected;
    }

    private List<String> getJSoupExpected(final String prefix) {
        List<String> expected = new ArrayList<>();
        expected.add(prefix + "EPS (TTM) |-");
        addExpected(prefix, expected);
        return expected;
    }

    private void addExpected(final String prefix, final List<String> expected) {
        expected.add(prefix + "DIV YIELD.(%) |2.70%");
        expected.add(prefix + "INDUSTRY P/E |37.06");
        expected.add(prefix + "P/C |-");
        expected.add(prefix + "FACE VALUE (Rs) |1.00");
        expected.add(prefix + "MARKET CAP (Rs Cr) |382,642.57");
        expected.add(prefix + "INDUSTRY P/E |37.06");
        expected.add(prefix + "PRICE/BOOK |11.29");
        expected.add(prefix + "BOOK VALUE (Rs) |27.89");
        expected.add(prefix + "DIV (%) |850.00%");
    }
}
