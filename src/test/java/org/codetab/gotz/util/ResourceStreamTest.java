package org.codetab.gotz.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codetab.gotz.di.DInjector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResourceStreamTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    ResourceStream resourceStream;

    @Before
    public void setUp() throws Exception {
        DInjector di = new DInjector().instance(DInjector.class);
        resourceStream = di.instance(ResourceStream.class);
    }

    @Test
    public void testGetInputStream() throws IOException {
        // given
        String file = "/gotz-default.xml";
        InputStream expected = ResourceStream.class.getResourceAsStream(file);

        // when
        InputStream actual = resourceStream.getInputStream(file);

        // then
        assertThat(IOUtils.contentEquals(actual, expected)).isTrue();
    }

    @Test
    public void testGetInputStreamShouldThrowException() throws IOException {
        // given
        exception.expect(FileNotFoundException.class);

        // when
        resourceStream.getInputStream("xyz");
    }

}
