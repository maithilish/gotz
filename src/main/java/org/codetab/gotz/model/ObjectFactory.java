
package org.codetab.gotz.model;

import java.beans.Beans;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.codetab.gotz.model package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Fields_QNAME =
            new QName("http://codetab.org/xfields", "fields");
    private final static QName _Wrapper_QNAME =
            new QName("http://codetab.org/gotz", "wrapper");
    private final static QName _Locator_QNAME =
            new QName("http://codetab.org/gotz", "locator");
    private final static QName _Locators_QNAME =
            new QName("http://codetab.org/gotz", "locators");
    private final static QName _Document_QNAME =
            new QName("http://codetab.org/gotz", "document");
    private final static QName _Bean_QNAME =
            new QName("http://codetab.org/gotz", "bean");
    private final static QName _Beans_QNAME =
            new QName("http://codetab.org/gotz", "beans");
    private final static QName _Datadef_QNAME =
            new QName("http://codetab.org/gotz", "datadef");
    private final static QName _Axis_QNAME =
            new QName("http://codetab.org/gotz", "axis");
    private final static QName _Member_QNAME =
            new QName("http://codetab.org/gotz", "member");
    private final static QName _Filter_QNAME =
            new QName("http://codetab.org/gotz", "filter");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.codetab.gotz.model
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Fields }
     *
     */
    public Fields createFields() {
        return new Fields();
    }

    /**
     * Create an instance of {@link Wrapper }
     *
     */
    public Wrapper createWrapper() {
        return new Wrapper();
    }

    /**
     * Create an instance of {@link Locator }
     *
     */
    public Locator createLocator() {
        return new Locator();
    }

    /**
     * Create an instance of {@link Locators }
     *
     */
    public Locators createLocators() {
        return new Locators();
    }

    /**
     * Create an instance of {@link Document }
     *
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link Bean }
     *
     */
    public Bean createBean() {
        return new Bean();
    }

    /**
     * Create an instance of {@link Beans }
     *
     */
    public Beans createBeans() {
        return new Beans();
    }

    /**
     * Create an instance of {@link DataDef }
     *
     */
    public DataDef createDataDef() {
        return new DataDef();
    }

    /**
     * Create an instance of {@link DAxis }
     *
     */
    public DAxis createDAxis() {
        return new DAxis();
    }

    /**
     * Create an instance of {@link DMember }
     *
     */
    public DMember createDMember() {
        return new DMember();
    }

    /**
     * Create an instance of {@link DFilter }
     *
     */
    public DFilter createDFilter() {
        return new DFilter();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Fields
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/xfields", name = "fields")
    public JAXBElement<Fields> createFields(Fields value) {
        return new JAXBElement<Fields>(_Fields_QNAME, Fields.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Wrapper
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "wrapper")
    public JAXBElement<Wrapper> createWrapper(Wrapper value) {
        return new JAXBElement<Wrapper>(_Wrapper_QNAME, Wrapper.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Locator
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "locator")
    public JAXBElement<Locator> createLocator(Locator value) {
        return new JAXBElement<Locator>(_Locator_QNAME, Locator.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Locators
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "locators")
    public JAXBElement<Locators> createLocators(Locators value) {
        return new JAXBElement<Locators>(_Locators_QNAME, Locators.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Document
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "document")
    public JAXBElement<Document> createDocument(Document value) {
        return new JAXBElement<Document>(_Document_QNAME, Document.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Bean
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "bean")
    public JAXBElement<Bean> createBean(Bean value) {
        return new JAXBElement<Bean>(_Bean_QNAME, Bean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Beans
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "beans")
    public JAXBElement<Beans> createBeans(Beans value) {
        return new JAXBElement<Beans>(_Beans_QNAME, Beans.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataDef
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "datadef")
    public JAXBElement<DataDef> createDatadef(DataDef value) {
        return new JAXBElement<DataDef>(_Datadef_QNAME, DataDef.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DAxis
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "axis")
    public JAXBElement<DAxis> createAxis(DAxis value) {
        return new JAXBElement<DAxis>(_Axis_QNAME, DAxis.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DMember
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "member")
    public JAXBElement<DMember> createMember(DMember value) {
        return new JAXBElement<DMember>(_Member_QNAME, DMember.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DFilter
     * }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://codetab.org/gotz", name = "filter")
    public JAXBElement<DFilter> createFilter(DFilter value) {
        return new JAXBElement<DFilter>(_Filter_QNAME, DFilter.class, null,
                value);
    }

}
