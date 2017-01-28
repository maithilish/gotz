package in.m.picks.shared;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import in.m.picks.model.Bean;
import in.m.picks.model.Wrapper;
import in.m.picks.util.Util;
import in.m.picks.validation.XMLValidator;

public enum BeanService {

	INSTANCE;

	final Logger logger = LoggerFactory.getLogger(BeanService.class);

	private List<Bean> beanFiles;
	protected List<Object> beans;

	private BeanService() {
		logger.info("Initializing Beans Singleton");
		beans = new ArrayList<Object>();
		try {
			setBeanFiles();
			unmarshallBeanFiles();
		} catch (JAXBException | ClassNotFoundException | SAXException
				| IOException e) {
			logger.trace("Stacktrace ", e);
			logger.error("{}. Picks terminated.", e.getLocalizedMessage());
			MonitorService.INSTANCE.triggerFatal("Beans Initialization failure");
		}
		logger.debug("Initialized Beans Singleton");
	}

	/*
	 * returns deep clone of beans of a class as persistence messes the beans
	 */
	public <T> List<T> getBeans(Class<T> ofClass) {
		List<T> list = new ArrayList<T>();
		for (Object bean : beans) {
			if (bean.getClass() == ofClass) {
				try {
					T deepClone = Util.deepClone(ofClass, ofClass.cast(bean));
					list.add(deepClone);
				} catch (ClassNotFoundException | IOException e) {
					logger.error("Unable to get deep Clone of bean. {}",
							e.getMessage());
					logger.trace("", e);
				}
			}
		}
		return list;
	}

	public <T> int getCount(Class<T> ofClass) {
		int count = 0;
		for (Object bean : beans) {
			if (bean.getClass() == ofClass) {
				count++;
			}
		}
		return count;
	}

	private void setBeanFiles() throws JAXBException, SAXException, IOException {

		String beanFile = ConfigService.INSTANCE.getConfig("picks.beanFile");
		String schemaFile = ConfigService.INSTANCE.getConfig("picks.schemaFile");
		String baseName = FilenameUtils.getFullPath(beanFile);

		logger.info("Initializing Bean file");
		logger.info("Using Bean configuartion file [{}]", beanFile);

		logger.info("Validating Bean file...");
		XMLValidator validator = new XMLValidator();
		validator.setXmlFile(beanFile);
		validator.setSchemaFile(schemaFile);
		validator.validate();

		beanFiles = unmarshall(beanFile, Bean.class);
		logger.info("Configuring Bean files...");
		for (Bean bean : beanFiles) {
			String fileName = baseName.concat(bean.getXmlFile());
			bean.setXmlFile(fileName);
			if (StringUtils.isEmpty(bean.getSchemaFile())) {
				bean.setSchemaFile(schemaFile);
			}
			logger.info("{}", bean.toString());
			validator.setXmlFile(bean.getXmlFile());
			validator.setSchemaFile(bean.getSchemaFile());
			validator.validate();
		}
	}

	private void unmarshallBeanFiles()
			throws ClassNotFoundException, JAXBException, FileNotFoundException {
		for (Bean bean : beanFiles) {
			Class<?> ofClass = Class.forName(bean.getClassName());
			logger.info("Unmarshall : [{}] to type [{}]", bean.getXmlFile(),
					ofClass);
			List<?> list = unmarshall(bean.getXmlFile(), ofClass);
			logger.info("Objects created [{}]", list.size());
			beans.addAll(list);
		}
	}

	private <T> List<T> unmarshall(String fileName, Class<T> ofClass)
			throws JAXBException, FileNotFoundException {
		JAXBContext jc = JAXBContext.newInstance(Wrapper.class, ofClass);
		Unmarshaller um = jc.createUnmarshaller();
		logger.debug("Unmarshing [{}]", fileName);
		List<T> beans = Util.unmarshal(um, ofClass, fileName);
		return beans;
	}

}
