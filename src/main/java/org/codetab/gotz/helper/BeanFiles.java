package org.codetab.gotz.helper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.validation.XMLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BeanFiles {

    private final Logger logger = LoggerFactory.getLogger(BeanFiles.class);

    @Inject
    private XMLValidator xmlValidator;
    @Inject
    private IXoc xoc;

    private String beanFile;
    private String schemaFile;
    private String baseName;

    @Inject
    private BeanFiles() {
    }

    public void setFiles(String beanFile, String schemaFile) {
        this.beanFile = beanFile;
        this.schemaFile = schemaFile;
    }

    public boolean validateBeanFile()
            throws JAXBException, IOException, SAXException, ConfigNotFoundException {
        logger.info("initialize Bean file");

        logger.info("using Bean configuartion file [{}]", beanFile);

        return xmlValidator.validate(beanFile, schemaFile);
    }

    public List<Bean> getBeanFiles() throws JAXBException, IOException, SAXException {
        baseName = FilenameUtils.getFullPath(beanFile);
        List<Bean> list = unmarshalBeanFile(beanFile, Bean.class);
        logger.info("configure Bean files...");
        for (Bean bean : list) {
            String fileName = baseName.concat(bean.getXmlFile());
            bean.setXmlFile(fileName);
            if (StringUtils.isEmpty(bean.getSchemaFile())) {
                bean.setSchemaFile(schemaFile);
            }
            logger.debug("{}", bean.toString());
        }
        return list;
    }

    public boolean validateBeanFiles(List<Bean> beanFiles)
            throws JAXBException, SAXException, IOException {
        logger.info("validate Bean files...");
        for (Bean bean : beanFiles) {
            xmlValidator.validate(bean.getXmlFile(), bean.getSchemaFile());
        }
        return true;
    }

    public <T> List<T> unmarshalBeanFile(String beanFile, Class<T> clz)
            throws JAXBException, IOException {
        return xoc.unmarshall(beanFile, clz);
    }
}
