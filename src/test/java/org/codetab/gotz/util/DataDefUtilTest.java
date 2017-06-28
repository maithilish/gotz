package org.codetab.gotz.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DataDef;
import org.junit.Assert;
import org.junit.Test;

public class DataDefUtilTest {

    @Test
    public void testGetAxis() {
        DAxis col = new DAxis();
        col.setName("col");

        DAxis row = new DAxis();
        row.setName("row");

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(col);
        dataDef.getAxis().add(row);

        DataDefUtil.getAxis(dataDef, AxisName.COL);

        DataDefUtil.getAxis(dataDef, AxisName.ROW);
    }

    @Test
    public void testGetAxisFromEmptyAxis() {
        DataDef dataDef = new DataDef();
        DataDefUtil.getAxis(dataDef, AxisName.COL);

    }

    @Test
    public void testWellDefined()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        assertUtilityClassWellDefined(DataDefUtil.class);
    }

    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Assert.assertTrue("class must be final",
                Modifier.isFinal(clazz.getModifiers()));
        Assert.assertEquals("There must be only one constructor", 1,
                clazz.getDeclaredConstructors().length);
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible()
                || !Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                Assert.fail("there exists a non-static method:" + method);
            }
        }
    }
}
