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
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.w3c.dom.Document;

import com.google.inject.Singleton;

@Singleton
public class LocatorFieldsHelper {

    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(LocatorXFieldHelper.class);

    /**
     * instance of BeanService.
     */
    @Inject
    private BeanService beanService;

    @Inject
    private ConfigService configService;

    @Inject
    private FieldsHelper xFieldHelper;

    @Inject
    private IOHelper ioHelper;

    private List<XField> xFields;

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
            xFields = getXFields();
        } catch (Exception e) {
            throw new CriticalException(e);
        }
        return true;
    }

    public XField getXField(final String clazz, final String group)
            throws FieldsException {
        // return deep copy
        for (XField xField : xFields) {
            if (xField.getClazz().equals(clazz)
                    && xField.getGroup().equals(group)) {
                return xFieldHelper.deepCopy(xField);
            }
        }
        // if not found, return empty xfield
        XField xField = new XField();
        xField.setName("locator");
        xField.setGroup(group);
        xField.setClazz(clazz);
        return xField;
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
        xFieldHelper.addElement("label", label, locator.getXField());
    }

    private List<XField> getXFields() throws ParserConfigurationException,
            FileNotFoundException, TransformerFactoryConfigurationError,
            TransformerException, ConfigNotFoundException, FieldsException {

        List<XField> xFieldList = new ArrayList<>();

        List<XField> xBeans = beanService.getBeans(XField.class);
        for (XField xBean : xBeans) {
            // merge global steps to tasks steps
            String defaultNs = XmlUtils.getDefaultNs(xBean.getNodes().get(0));
            Document doc = XmlUtils.createDocument(xBean.getNodes(), "xfield",
                    null, defaultNs);
            Document effectiveDoc = mergeSteps(doc);

            // split on tasks to new XFields
            XField holder = new XField();
            holder.getNodes().add(effectiveDoc);
            List<XField> newXFields =
                    xFieldHelper.split("/:xfield/:tasks", holder);

            // set new xfield fields
            for (XField xField : newXFields) {
                xField.setName(xBean.getName());
                xField.setClazz(xBean.getClazz());
                xField.setGroup(getGroupFromNodes(xField));
            }

            xFieldList.addAll(newXFields);
        }
        return xFieldList;
    }

    private String getGroupFromNodes(final XField xField)
            throws FieldsException {
        String xpath = "/:xfield/:tasks/@group";
        return xFieldHelper.getLastValue(xpath, xField);
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
