
package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Java class for dataDef complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="dataDef"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://codetab.org/xfield}xfield" minOccurs="0"/&gt;
 *         &lt;element ref="{http://codetab.org/gotz}axis" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="fromDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="toDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "dataDef", propOrder = {"xfield", "axis"})
public class DataDef extends Base implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://codetab.org/xfield")
    private XField xfield;
    @XmlElement
    private List<DAxis> axis;
    @XmlAttribute(name = "fromDate")
    @XmlJavaTypeAdapter(Adapter1.class)
    @XmlSchemaType(name = "dateTime")
    private Date fromDate;
    @XmlAttribute(name = "toDate")
    @XmlJavaTypeAdapter(Adapter1.class)
    @XmlSchemaType(name = "dateTime")
    private Date toDate;

    /**
     * Gets the value of the xfield property.
     *
     * @return possible object is {@link XField }
     *
     */
    public XField getXfield() {
        return xfield;
    }

    /**
     * Sets the value of the xfield property.
     *
     * @param value
     *            allowed object is {@link XField }
     *
     */
    public void setXfield(XField value) {
        this.xfield = value;
    }

    /**
     * Gets the value of the axis property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the axis property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAxis().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DAxis }
     *
     *
     */
    public List<DAxis> getAxis() {
        if (axis == null) {
            axis = new ArrayList<DAxis>();
        }
        return this.axis;
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
    public void setFromDate(Date value) {
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
    public void setToDate(Date value) {
        this.toDate = value;
    }

    @Override
    public boolean equals(final Object obj) {
        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId()).append("name", getName())
                .append("fromDate", fromDate).append("toDate", toDate)
                .append("axis", axis).toString();
    }

}
