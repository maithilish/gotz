package org.codetab.gotz.model.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.helper.IXoc;
import org.codetab.gotz.messages.Messages;
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
        Validate.notNull(beanFileName, Messages.getString("BeanHelper.0")); //$NON-NLS-1$
        Validate.notNull(schemaFileName, Messages.getString("BeanHelper.1")); //$NON-NLS-1$

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
        logger.info(Messages.getString("BeanHelper.2")); //$NON-NLS-1$
        logger.info(Messages.getString("BeanHelper.3"), beanFile); //$NON-NLS-1$

        Validate.validState(beanFile != null,
                Messages.getString("BeanHelper.4")); //$NON-NLS-1$
        Validate.validState(schemaFile != null,
                Messages.getString("BeanHelper.5")); //$NON-NLS-1$

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
        Validate.notNull(beanFiles, Messages.getString("BeanHelper.6")); //$NON-NLS-1$

        logger.info(Messages.getString("BeanHelper.7")); //$NON-NLS-1$

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

        Validate.validState(beanFile != null,
                Messages.getString("BeanHelper.8")); //$NON-NLS-1$
        Validate.validState(schemaFile != null,
                Messages.getString("BeanHelper.9")); //$NON-NLS-1$

        baseName = FilenameUtils.getFullPath(beanFile);
        String packageName = Bean.class.getPackage().getName();
        List<Object> list = unmarshalBeanFile(beanFile, packageName);
        logger.info(Messages.getString("BeanHelper.10")); //$NON-NLS-1$

        List<Bean> beans = new ArrayList<>();
        for (Object o : list) {
            Bean bean = (Bean) o;
            String fileName = baseName.concat(bean.getXmlFile());
            bean.setXmlFile(fileName);
            if (StringUtils.isEmpty(bean.getSchemaFile())) {
                bean.setSchemaFile(schemaFile);
            }
            beans.add(bean);
            logger.debug("{}", bean.toString()); //$NON-NLS-1$
        }
        return beans;
    }

    public List<Object> unmarshalBeanFile(final String fileName,
            final String packageName) throws JAXBException, IOException {
        Validate.notNull(fileName, Messages.getString("BeanHelper.12")); //$NON-NLS-1$
        Validate.notNull(packageName, "packageName must not be null");

        return xoc.unmarshall(fileName, packageName);
    }
}
