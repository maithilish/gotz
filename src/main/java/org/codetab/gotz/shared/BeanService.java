package org.codetab.gotz.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.helper.BeanFiles;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

@Singleton
public class BeanService {

    private final Logger logger = LoggerFactory.getLogger(BeanService.class);

    @Inject
    private BeanFiles beanFiles;

    private List<Object> beans;

    @Inject
    private BeanService() {
    }

    public void init(String beanFile, String schemaFile) {
        logger.info("initialize singleton {}", "BeanService");
        try {
            beanFiles.setFiles(beanFile, schemaFile);
            beanFiles.validateBeanFile();
            List<Bean> files = beanFiles.getBeanFiles();
            beanFiles.validateBeanFiles(files);
            beans = unmarshallBeanFiles(files);
        } catch (ConfigNotFoundException | JAXBException | SAXException
                | IOException | ClassNotFoundException e) {
            logger.trace("{}", e);
            logger.error("{}", e.getLocalizedMessage());
            throw new CriticalException("initialization failure : BeanService",
                    e);
        }
        logger.debug("initialized singleton {}", "BeanService");
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
                    logger.error("unable to get deep Clone of bean. {}",
                            e.getMessage());
                    logger.trace("", e);
                }
            }
        }
        return list;
    }

    public <T> long getCount(final Class<T> ofClass) {
        return beans.stream().filter(ofClass::isInstance).count();
    }

    private List<Object> unmarshallBeanFiles(List<Bean> beans)
            throws ClassNotFoundException, JAXBException, IOException,
            SAXException {
        logger.info("unmarshall bean files...");
        List<Object> list = new ArrayList<>();
        for (Bean bean : beans) {
            Class<?> ofClass = Class.forName(bean.getClassName());
            List<?> objs =
                    beanFiles.unmarshalBeanFile(bean.getXmlFile(), ofClass);
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
