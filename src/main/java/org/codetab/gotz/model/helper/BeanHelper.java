package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.IXoc;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.validation.XMLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BeanHelper {

    private final Logger logger = LoggerFactory.getLogger(BeanHelper.class);

    @Inject
    private XMLValidator xmlValidator;
    @Inject
    private IXoc xoc;

    private String beanFile;
    private String schemaFile;
    private String baseName;

    @Inject
    private BeanHelper() {
    }

    public void setFiles(final String beanFileName,
            final String schemaFileName) {
        this.beanFile = beanFileName;
        this.schemaFile = schemaFileName;
    }

    public boolean validateBeanFile() throws JAXBException, IOException,
            SAXException, ConfigNotFoundException {
        logger.info("initialize Bean file");

        logger.info("using Bean configuartion file [{}]", beanFile);

        return xmlValidator.validate(beanFile, schemaFile);
    }

    public List<Bean> getBeanFiles()
            throws JAXBException, IOException, SAXException {
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

    public boolean validateBeanFiles(final List<Bean> beanFiles)
            throws JAXBException, SAXException, IOException {
        logger.info("validate Bean files...");
        for (Bean bean : beanFiles) {
            xmlValidator.validate(bean.getXmlFile(), bean.getSchemaFile());
        }
        return true;
    }

    public <T> List<T> unmarshalBeanFile(final String fileName,
            final Class<T> clz) throws JAXBException, IOException {
        return xoc.unmarshall(fileName, clz);
    }
}