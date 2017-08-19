package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.IXoc;
import org.codetab.gotz.model.Bean;
import org.codetab.gotz.validation.XMLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * Bean Helper.
 * @author Maithilish
 *
 */
public class BeanHelper {

    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(BeanHelper.class);

    /**
     * XML validator.
     */
    @Inject
    private XMLValidator xmlValidator;
    /**
     * XML Object converter.
     */
    @Inject
    private IXoc xoc;

    /**
     * bean file name.
     */
    private String beanFile;
    /**
     * schema file name.
     */
    private String schemaFile;
    /**
     * path base name.
     */
    private String baseName;

    /**
     * <p>
     * Private constructor.
     */
    @Inject
    private BeanHelper() {
    }

    /**
     * <p>
     * Set bean and schema file.
     * @param beanFileName
     *            bean file name
     * @param schemaFileName
     *            schema file name
     */
    public void setFiles(final String beanFileName,
            final String schemaFileName) {
        Validate.notNull(beanFileName, "beanFileName must not be null");
        Validate.notNull(schemaFileName, "schemaFileName must not be null");

        this.beanFile = beanFileName;
        this.schemaFile = schemaFileName;
    }

    /**
     * <p>
     * Validate bean file with schema.
     * @return true if bean file is valid
     * @throws JAXBException
     *             parse error
     * @throws SAXException
     *             parse error
     * @throws IOException
     *             IO error
     * @throws ConfigNotFoundException
     *             config error
     */
    public boolean validateBeanFile() throws JAXBException, IOException,
            SAXException, ConfigNotFoundException {
        logger.info("initialize Bean file");
        logger.info("using Bean configuartion file [{}]", beanFile);

        Validate.validState(beanFile != null, "beanFile is null");
        Validate.validState(schemaFile != null, "schemaFile is null");

        return xmlValidator.validate(beanFile, schemaFile);
    }

    /**
     * <p>
     * Validate XML files specified by list of beans.
     * @param beanFiles
     *            list of bean containing XML and schema file names.
     * @return true if all XML files are valid
     * @throws JAXBException
     *             parse error
     * @throws SAXException
     *             parse error
     * @throws IOException
     *             IO error
     */
    public boolean validateBeanFiles(final List<Bean> beanFiles)
            throws JAXBException, SAXException, IOException {
        Validate.notNull(beanFiles, "beanFiles must not be null");

        logger.info("validate Bean files...");

        for (Bean bean : beanFiles) {
            xmlValidator.validate(bean.getXmlFile(), bean.getSchemaFile());
        }
        return true;
    }

    /**
     * <p>
     * Unmarshal beanFile and create a list of bean specifying XML and schema
     * files.
     * @return list of bean
     * @throws JAXBException
     *             parse error
     * @throws SAXException
     *             parse error
     * @throws IOException
     *             IO error
     */
    public List<Bean> getBeanFiles()
            throws JAXBException, IOException, SAXException {

        Validate.validState(beanFile != null, "beanFile is null");
        Validate.validState(schemaFile != null, "schemaFile is null");

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

    /**
     * <p>
     * Unmarshal XML to objects of a class. <T> class type
     * @param <T>
     *            class type
     * @param fileName
     *            XML file
     * @param clz
     *            class of object
     * @return list of objects
     * @throws JAXBException
     *             parse error
     * @throws IOException
     *             IO error
     */
    public <T> List<T> unmarshalBeanFile(final String fileName,
            final Class<T> clz) throws JAXBException, IOException {

        Validate.notNull(fileName, "fileName must not be null");
        Validate.notNull(clz, "clz must not be null");

        return xoc.unmarshall(fileName, clz);
    }
}
