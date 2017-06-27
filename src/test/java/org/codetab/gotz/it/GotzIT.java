package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.ListAppender;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Test;

public class GotzIT {

    @Test
    public void simpleTest() {

        DInjector dInjector = new DInjector().instance(DInjector.class);

        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        AppenderService appenderService = dInjector.instance(AppenderService.class);

        System.setProperty("gotz.beanFile", "/itest/simple/bean.xml");

        gotzEngine.start();

        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<String> actual = listAppender.getList();
        List<String> expected = TestUtil.readFileAsList("/itest/simple/expected.txt");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void bsTest() {

        DInjector dInjector = new DInjector().instance(DInjector.class);

        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        AppenderService appenderService = dInjector.instance(AppenderService.class);

        System.setProperty("gotz.beanFile", "/itest/mc/bs/bean.xml");

        gotzEngine.start();

        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<String> actual = listAppender.getList();
        List<String> expected = TestUtil.readFileAsList("/itest/mc/bs/expected.txt");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
