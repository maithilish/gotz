package org.codetab.gotz.itest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.codetab.gotz.di.DInjector;
import org.junit.Test;

public class Ex4IT extends ITestBase {

    private String exName = "ex-4";

    @Test
    public void jsoupTest() throws FileNotFoundException, IOException {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        String exDir = "/defs/examples/jsoup/" + exName;

        String beanFile = exDir + "/bean.xml";
        List<String> actual = runGotz(beanFile, dInjector);

        String expectedFile = exDir + "/expected.txt";
        List<String> expected = getExpectedList(expectedFile, dInjector);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void htmlunitTest() throws FileNotFoundException, IOException {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        String exDir = "/defs/examples/htmlunit/" + exName;

        String beanFile = exDir + "/bean.xml";
        List<String> actual = runGotz(beanFile, dInjector);

        String expectedFile = exDir + "/expected.txt";
        List<String> expected = getExpectedList(expectedFile, dInjector);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
