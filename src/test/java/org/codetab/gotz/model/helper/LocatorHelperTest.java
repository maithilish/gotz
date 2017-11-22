package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * tests for LocatorHelper.
 *
 * @author Maithilish
 *
 */
public class LocatorHelperTest {

    @Mock
    private BeanService beanService;
    @Mock
    private ConfigService configService;

    @InjectMocks
    private LocatorHelper locatorHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessTrikleGroup() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 =
                locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorHelper.getLocatorsFromBeans();

        // then
        assertThat(locator1.getGroup()).isEqualTo("g1");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("g1");
    }

    @Test
    public void testProcessTrikleGroupLocatorGroupNotNull() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 =
                locators.get(0).getLocators().get(0).getLocator().get(0);

        locator1.setGroup("gx");

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorHelper.getLocatorsFromBeans();

        // then
        assertThat(locator1.getGroup()).isEqualTo("gx");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("g1");
    }

    @Test
    public void testProcessTrikleGroupChildLocatorsGroupNotNull() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 =
                locators.get(0).getLocators().get(0).getLocator().get(0);

        // set group of child locators
        Locators locs = locators.get(0).getLocators().get(0);
        locs.setGroup("gx");

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorHelper.getLocatorsFromBeans();

        // then
        assertThat(locator1.getGroup()).isEqualTo("g1");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("gx");
    }

    @Test
    public void testProcessExtractLocators() throws IllegalAccessException {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 =
                locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        List<Locator> actual = locatorHelper.getLocatorsFromBeans();

        // then
        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual).contains(locator1, locator2, locator3);
    }

    @Test
    public void testGetLocatorsFromBeansIllegalState()
            throws IllegalAccessException {
        FieldUtils.writeDeclaredField(locatorHelper, "beanService", null, true);
        try {
            locatorHelper.getLocatorsFromBeans();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("beanService is null");
        }
    }

    @Test
    public void testForkLocators() throws ConfigNotFoundException {
        Locator locator1 = new Locator();
        locator1.setName("x");
        locator1.setGroup("g");
        locator1.setUrl("u");

        Locator locator2 = new Locator();
        locator2.setName("x0");
        locator2.setGroup("g");
        locator2.setUrl("u");

        Locator locator3 = new Locator();
        locator3.setName("x1");
        locator3.setGroup("g");
        locator3.setUrl("u");

        List<Locator> locators = new ArrayList<>();
        locators.add(locator1);

        given(configService.getConfig("gotz.fork.locator")).willReturn("2");

        List<Locator> actual = locatorHelper.forkLocators(locators);

        // then
        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual).contains(locator1, locator2, locator3);
    }

    @Test
    public void testForkLocatorsConfigNotFound()
            throws ConfigNotFoundException {
        Locator locator1 = new Locator();
        locator1.setName("x");
        locator1.setGroup("g");
        locator1.setUrl("u");

        List<Locator> locators = new ArrayList<>();
        locators.add(locator1);

        given(configService.getConfig("gotz.fork.locator"))
                .willThrow(ConfigNotFoundException.class);

        List<Locator> actual = locatorHelper.forkLocators(locators);

        // then
        assertThat(actual.size()).isEqualTo(0);
    }

    @Test
    public void testForkLocatorsIllegalState() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(locatorHelper, "configService", null,
                true);
        try {
            locatorHelper.forkLocators(new ArrayList<>());
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("configService is null");
        }
    }

    @Test
    public void testForkLocatorsNullParams() {
        try {
            locatorHelper.forkLocators(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("locators must not be null");
        }
    }

    private List<Locators> createTestObjects() {
        // Field field = new Field();
        // field.setName("f");

        Locator locator1 = new Locator();
        locator1.setName("n1");
        locator1.setFields(new Fields());
        // locator1.getFields().add(field);

        Locator locator2 = new Locator();
        locator2.setName("n2");
        locator1.setFields(new Fields());
        // locator2.getFields().add(field);

        Locators locators1 = new Locators();
        locators1.setGroup("g1");
        locators1.getLocator().add(locator1);
        locators1.getLocator().add(locator2);

        Locator locator3 = new Locator();
        locator3.setName("n3");
        // locator3.getFields().add(field);
        locator3.setFields(new Fields());

        Locators locators2 = new Locators();
        locators2.getLocator().add(locator3);

        locators1.getLocators().add(locators2);

        List<Locators> locators = new ArrayList<>();
        locators.add(locators1);

        return locators;
    }

}
