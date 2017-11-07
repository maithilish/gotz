package org.codetab.gotz.model.helper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.codetab.gotz.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.inject.Singleton;

@Singleton
public class LocatorXFieldHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorXFieldHelper.class);

    /**
     * instance of BeanService.
     */
    @Inject
    private BeanService beanService;

    @Inject
    private ConfigService configService;

    @Inject
    private XFieldHelper xFieldHelper;

    @Inject
    private IOHelper ioHelper;

    private List<XField> xFields;

    /**
     * private constructor.
     */
    @Inject
    private LocatorXFieldHelper() {
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
            throws TransformerException, ParserConfigurationException {
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
     */
    public void addLabel(final Locator locator) {
        Validate.notNull(locator, "locator must not be null");

        String label =
                Util.buildString(locator.getName(), ":", locator.getGroup());

        Optional<Node> node =
                xFieldHelper.getLast(locator.getXField().getNodes());

        if (node.isPresent()) {
            xFieldHelper.addElement("label", label, node.get());
        } else {
            LOGGER.error("xfield is empty, unable to add locator label {}",
                    label);
        }
    }

    private List<XField> getXFields()
            throws ParserConfigurationException, FileNotFoundException,
            TransformerFactoryConfigurationError, TransformerException,
            ConfigNotFoundException, XPathExpressionException {
        List<XField> xBeans = beanService.getBeans(XField.class);

        List<XField> xFieldList = new ArrayList<>();

        for (XField xBean : xBeans) {
            Document doc = XmlUtils.createDocument(xBean.getNodes(), "xfield");
            Document effectiveDoc = mergeSteps(doc);
            XField xField = new XField();
            xField.setName(xBean.getName());
            xField.setClazz(xBean.getClazz());
            xField.setGroup(getGroup(effectiveDoc));
            xField.getNodes().add(effectiveDoc);
            xFieldList.add(xField);
        }
        return xFieldList;
    }

    private String getGroup(final Node node) throws XPathExpressionException {
        String xpath = "/:xfield/:tasks/@group";
        return xFieldHelper.getValue(xpath, node);
    }

    private Document mergeSteps(final Document doc)
            throws FileNotFoundException, TransformerFactoryConfigurationError,
            TransformerException, ConfigNotFoundException {
        String stepsXslFile = configService.getConfig("gotz.stepsXslFile");
        StreamSource xslSource = ioHelper.getStreamSource(stepsXslFile);
        Transformer tr =
                TransformerFactory.newInstance().newTransformer(xslSource);

        DOMResult domResult = new DOMResult();
        tr.transform(new DOMSource(doc), domResult);

        Document transformedDoc = (Document) domResult.getNode();

        System.out.println("---" + doc.getNamespaceURI()
                + transformedDoc.getNamespaceURI());

        return transformedDoc;
    }

}
