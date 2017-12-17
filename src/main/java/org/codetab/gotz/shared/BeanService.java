package org.codetab.gotz.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.helper.BeanHelper;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

@Singleton
public class BeanService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BeanService.class);

    @Inject
    private BeanHelper beanHelper;

    private List<Object> beans;

    @Inject
    private BeanService() {
    }

    public void init(final String beanFile, final String schemaFile) {
        LOGGER.info(Messages.getString("BeanService.0"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            beanHelper.setFiles(beanFile, schemaFile);
            beanHelper.validateBeanFile();
            List<Bean> files = beanHelper.getBeanFiles();
            beanHelper.validateBeanFiles(files);
            beans = unmarshallBeanFiles(files);
        } catch (ConfigNotFoundException | JAXBException | SAXException
                | IOException | ClassNotFoundException e) {
            throw new CriticalException(Messages.getString("BeanService.2"), //$NON-NLS-1$
                    e);
        }
        LOGGER.debug(Messages.getString("BeanService.3"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * returns deep clone of beans of a class as persistence messes the beans
     */
    public <T> List<T> getBeans(final Class<T> ofClass) {
        List<T> list = new ArrayList<T>();
        for (Object bean : beans) {
            if (ofClass.isInstance(bean)) {
                try {
                    T deepClone = Util.deepClone(ofClass, ofClass.cast(bean));
                    list.add(deepClone);
                } catch (ClassNotFoundException | IOException e) {
                    LOGGER.error(Messages.getString("BeanService.5"), //$NON-NLS-1$
                            e.getMessage());
                    LOGGER.trace("", e); //$NON-NLS-1$
                }
            }
        }
        return list;
    }

    public <T> long getCount(final Class<T> ofClass) {
        return beans.stream().filter(ofClass::isInstance).count();
    }

    private List<Object> unmarshallBeanFiles(final List<Bean> beanList)
            throws ClassNotFoundException, JAXBException, IOException,
            SAXException {
        LOGGER.info(Messages.getString("BeanService.7")); //$NON-NLS-1$
        List<Object> list = new ArrayList<>();
        for (Bean bean : beanList) {
            Class<?> ofClass = Class.forName(bean.getClassName());
            List<?> objs =
                    beanHelper.unmarshalBeanFile(bean.getXmlFile(), ofClass);
            list.addAll(objs);
        }
        return list;
    }

    // private void debugState(final Wrapper wrapper) throws JAXBException {
    // ObjectFactory of = new ObjectFactory();
    // JAXBElement<Wrapper> we = of.createWrapper(wrapper);
    // MDC.put("entitytype", "defs");
    // xoc.marshall(we, wrapper);
    // MDC.remove("entitytype");
    // }
}
