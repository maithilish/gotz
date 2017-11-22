package org.codetab.gotz.model.helper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.w3c.dom.Document;

import com.google.inject.Singleton;

@Singleton
public class LocatorFieldsHelper {

    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(LocatorFieldsHelper.class);

    /**
     * instance of BeanService.
     */
    @Inject
    private BeanService beanService;

    @Inject
    private ConfigService configService;

    @Inject
    private FieldsHelper fieldsHelper;

    @Inject
    private IOHelper ioHelper;

    private List<Fields> fieldsList;

    /**
     * private constructor.
     */
    @Inject
    private LocatorFieldsHelper() {
    }

    /**
     * initializes the helper by assigning step and class fields to state
     * variables.
     * @return true if able to set step and class fields else returns false
     * @throws ParserConfigurationException
     */
    public boolean init() {
        Validate.validState(beanService != null, "beanService is null");

        try {
            fieldsList = getFieldss();
        } catch (Exception e) {
            throw new CriticalException(e);
        }
        return true;
    }

    public Fields getFields(final String clazz, final String group)
            throws FieldsException {
        // return deep copy
        for (Fields fields : fieldsList) {
            if (fields.getClazz().equals(clazz)
                    && fields.getGroup().equals(group)) {
                return fieldsHelper.deepCopy(fields);
            }
        }
        // if not found, return empty fields
        Fields fields = new Fields();
        fields.setName("locator");
        fields.setGroup(group);
        fields.setClazz(clazz);
        return fields;
    }

    /**
     * adds label field to locator. Label is name:group pair. *
     * @param locator
     *            {@link Locator}
     * @throws FieldsException
     */
    public void addLabel(final Locator locator) throws FieldsException {
        Validate.notNull(locator, "locator must not be null");
        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());
        fieldsHelper.addElement("label", label, locator.getFields());
    }

    private List<Fields> getFieldss() throws ParserConfigurationException,
            FileNotFoundException, TransformerFactoryConfigurationError,
            TransformerException, ConfigNotFoundException, FieldsException {

        List<Fields> fieldsList = new ArrayList<>();

        List<Fields> xBeans = beanService.getBeans(Fields.class);
        for (Fields xBean : xBeans) {
            // merge global steps to tasks steps
            String defaultNs = XmlUtils.getDefaultNs(xBean.getNodes().get(0));
            Document doc = XmlUtils.createDocument(xBean.getNodes(), "fields",
                    null, defaultNs);
            Document effectiveDoc = mergeSteps(doc);

            // split on tasks to new Fieldss
            Fields holder = new Fields();
            holder.getNodes().add(effectiveDoc);
            List<Fields> newFieldss =
                    fieldsHelper.split("/:fields/:tasks", holder);

            // set new fields fields
            for (Fields fields : newFieldss) {
                fields.setName(xBean.getName());
                fields.setClazz(xBean.getClazz());
                fields.setGroup(getGroupFromNodes(fields));
            }

            fieldsList.addAll(newFieldss);
        }
        return fieldsList;
    }

    private String getGroupFromNodes(final Fields fields)
            throws FieldsException {
        String xpath = "/:fields/:tasks/@group";
        return fieldsHelper.getLastValue(xpath, fields);
    }

    private Document mergeSteps(final Document doc) throws FieldsException {
        String stepsXslFile = "";
        try {
            stepsXslFile = configService.getConfig("gotz.stepsXslFile");
            StreamSource xslSource = ioHelper.getStreamSource(stepsXslFile);
            DOMResult domResult = new DOMResult();

            Transformer tr =
                    TransformerFactory.newInstance().newTransformer(xslSource);
            tr.transform(new DOMSource(doc), domResult);

            Document tDoc = (Document) domResult.getNode();
            return tDoc;
        } catch (ConfigNotFoundException | FileNotFoundException
                | TransformerFactoryConfigurationError
                | TransformerException e) {
            throw new FieldsException(Util.buildString(
                    "unable to merge steps [", stepsXslFile, "]"), e);
        }
    }

}
