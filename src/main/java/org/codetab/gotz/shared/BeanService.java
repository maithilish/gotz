package org.codetab.gotz.shared;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.model.ObjectFactory;
import org.codetab.gotz.model.Wrapper;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.validation.XMLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xml.sax.SAXException;

public enum BeanService {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(BeanService.class);

    private List<Bean> beanFiles;
    private List<Object> beans;

    BeanService() {
        String className = this.getClass().getName();
        logger.info("initialize singleton {}", className);
        beans = new ArrayList<Object>();
        try {
            validateBeanFile();
            setBeanFiles();
            validateBeanFiles();
            unmarshallBeanFiles();
        } catch (JAXBException | ClassNotFoundException | SAXException | IOException e) {
            logger.trace("{}", e);
            logger.error("{}", e.getLocalizedMessage());
            MonitorService.INSTANCE.triggerFatal("initialization failure " + className);
        }
        logger.debug("initialized singleton {}", className);
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
                    logger.error("Unable to get deep Clone of bean. {}", e.getMessage());
                    logger.trace("", e);
                }
            }
        }
        return list;
    }

    public <T> int getCount(final Class<T> ofClass) {
        int count = 0;
        for (Object bean : beans) {
            if (bean.getClass() == ofClass) {
                count++;
            }
        }
        return count;
    }

    private void validateBeanFile() throws JAXBException, SAXException, IOException {
        String beanFile = ConfigService.INSTANCE.getConfig("gotz.beanFile");
        String schemaFile = ConfigService.INSTANCE.getConfig("gotz.schemaFile");
        validateSchema(beanFile, schemaFile);
    }

    private void setBeanFiles() throws JAXBException, SAXException, IOException {
        String beanFile = ConfigService.INSTANCE.getConfig("gotz.beanFile");
        String schemaFile = ConfigService.INSTANCE.getConfig("gotz.schemaFile");
        String baseName = FilenameUtils.getFullPath(beanFile);

        logger.info("initialize Bean file");
        logger.info("using Bean configuartion file [{}]", beanFile);

        beanFiles = unmarshall(beanFile, Bean.class);
        logger.info("configure Bean files...");
        for (Bean bean : beanFiles) {
            String fileName = baseName.concat(bean.getXmlFile());
            bean.setXmlFile(fileName);
            if (StringUtils.isEmpty(bean.getSchemaFile())) {
                bean.setSchemaFile(schemaFile);
            }
            logger.debug("{}", bean.toString());
        }
    }

    private void validateBeanFiles() throws JAXBException, SAXException, IOException {
        logger.info("validate Bean files...");
        for (Bean bean : beanFiles) {
            validateSchema(bean.getXmlFile(), bean.getSchemaFile());
        }
    }

    private void validateSchema(final String xmlFile, final String schemaFile)
            throws JAXBException, IOException, SAXException {
        XMLValidator validator = new XMLValidator();
        validator.setXmlFile(xmlFile);
        validator.setSchemaFile(schemaFile);
        validator.validate();
    }

    private void unmarshallBeanFiles()
            throws ClassNotFoundException, JAXBException, FileNotFoundException {
        logger.info("unmarshall bean files...");
        for (Bean bean : beanFiles) {
            Class<?> ofClass = Class.forName(bean.getClassName());
            List<?> list = unmarshall(bean.getXmlFile(), ofClass);
            beans.addAll(list);
        }
    }

    private <T> List<T> unmarshall(final String fileName, final Class<T> ofClass)
            throws JAXBException, FileNotFoundException {
        String packageName = ofClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller um = jc.createUnmarshaller();
        logger.debug("unmarshall : [{}] to type [{}]", fileName, ofClass);
        StreamSource xmlSource = new StreamSource(Util.getResourceAsStream(fileName));
        Wrapper wrapper = um.unmarshal(xmlSource, Wrapper.class).getValue();
        debugState(wrapper);
        List<T> list = new ArrayList<T>();
        for (Object e : wrapper.getAny()) {
            @SuppressWarnings("unchecked")
            T t = (T) JAXBIntrospector.getValue(e);
            list.add(t);
        }
        logger.debug("model objects created [{}]", list.size());
        return list;
    }

    private void debugState(final Wrapper wrapper) throws JAXBException {
        ObjectFactory of = new ObjectFactory();
        JAXBElement<Wrapper> we = of.createWrapper(wrapper);
        MDC.put("entitytype", "defs");
        marshall(we, wrapper);
        MDC.remove("entitytype");
    }

    public void marshall(final JAXBElement<?> e, final Object o) throws JAXBException {
        String packageName = o.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller jm = jc.createMarshaller();
        jm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter result = new StringWriter();
        jm.marshal(e, result);
        logger.debug(result.toString());
    }

}
