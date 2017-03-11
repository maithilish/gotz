//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2017.03.11 at 10:13:58 AM IST
//

package in.m.picks.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Java class for document complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this
 * class.
 *
 * <pre>
 * &lt;complexType name="document"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/picks}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="documentObject"
 *                     type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="fromDate"
 *                     type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="toDate"
 *                     type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="url" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "document", propOrder = {"documentObject"})
public final class Document extends Base implements Serializable {

    private static final long serialVersionUID = 1L;
    @ToStringExclude
    @XmlElement
    private Object documentObject;
    @XmlAttribute(name = "fromDate")
    @XmlJavaTypeAdapter(Adapter1.class)
    @XmlSchemaType(name = "dateTime")
    private Date fromDate;
    @XmlAttribute(name = "toDate")
    @XmlJavaTypeAdapter(Adapter1.class)
    @XmlSchemaType(name = "dateTime")
    private Date toDate;
    @XmlAttribute(name = "url")
    private String url;

    /**
     * Gets the value of the documentObject property.
     *
     * @return possible object is {@link Object }
     *
     */
    public Object getDocumentObject() {
        return documentObject;
    }

    /**
     * Sets the value of the documentObject property.
     *
     * @param value
     *            allowed object is {@link Object }
     *
     */
    public void setDocumentObject(final Object value) {
        this.documentObject = value;
    }

    /**
     * Gets the value of the fromDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFromDate(final Date value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the toDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setToDate(final Date value) {
        this.toDate = value;
    }

    /**
     * Gets the value of the url property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setUrl(final String value) {
        this.url = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see in.m.picks.model.Base#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see in.m.picks.model.Base#hashCode()
     */
    @Override
    public int hashCode() {
        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see in.m.picks.model.Base#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
