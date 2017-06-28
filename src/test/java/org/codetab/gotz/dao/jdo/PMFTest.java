package org.codetab.gotz.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.ResourceStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class PMFTest {

    @Mock
    private ConfigService configService;
    @Spy
    private ResourceStream resourceStream;
    @Spy
    private Properties jdoProperties;

    @InjectMocks
    private PMF pmf;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        // given
        DInjector dInjector = new DInjector().instance(DInjector.class);

        // when
        PMF instanceA = dInjector.instance(PMF.class);
        PMF instanceB = dInjector.instance(PMF.class);

        // then
        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testInit() throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ConfigNotFoundException {

        given(configService.getConfig("gotz.datastore.configFile"))
                .willReturn("/jdoconfig.properties");

        pmf.init();

        InOrder inOrder = inOrder(configService, resourceStream, jdoProperties);
        inOrder.verify(configService).getConfig("gotz.datastore.configFile");
        inOrder.verify(resourceStream).getInputStream("/jdoconfig.properties");
        inOrder.verify(jdoProperties).load(any(InputStream.class));

        assertThat(pmf.getFactory()).isNotNull();
    }

    @Test
    public void testInitMultiple() throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ConfigNotFoundException {

        given(configService.getConfig("gotz.datastore.configFile"))
                .willReturn("/jdoconfig.properties");

        pmf.init();

        InOrder inOrder = inOrder(configService, resourceStream, jdoProperties);
        inOrder.verify(configService).getConfig("gotz.datastore.configFile");
        inOrder.verify(resourceStream).getInputStream("/jdoconfig.properties");
        inOrder.verify(jdoProperties).load(any(InputStream.class));

        assertThat(pmf.getFactory()).isNotNull();

        verifyNoMoreInteractions(configService, resourceStream);

        pmf.init();

        verifyNoMoreInteractions(configService, resourceStream);
    }

    @Test
    public void testInitThrowConfigNotFoundException()
            throws ConfigNotFoundException {
        given(configService.getConfig("gotz.datastore.configFile"))
                .willThrow(ConfigNotFoundException.class);

        exceptionRule.expect(CriticalException.class);
        pmf.init();
    }

    @Test
    public void testInitThrowFileNotFoundException()
            throws FileNotFoundException {
        given(resourceStream.getInputStream("/jdoconfig.properties"))
                .willThrow(FileNotFoundException.class);

        exceptionRule.expect(CriticalException.class);
        pmf.init();
    }

}
