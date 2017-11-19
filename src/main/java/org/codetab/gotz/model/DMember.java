
package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Java class for dMember complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="dMember"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://codetab.org/gotz}fields"/&gt;
 *         &lt;element ref="{http://codetab.org/xfield}xfield" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="axis" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="match" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "dMember", propOrder = {"fields", "xfield"})
public class DMember extends Base implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElements({@XmlElement(name = "field", type = Field.class),
            @XmlElement(type = Fields.class)})
    private List<FieldsBase> fields;
    @XmlElement(namespace = "http://codetab.org/xfield")
    private XField xfield;
    @XmlAttribute(name = "axis")
    private String axis;
    @XmlAttribute(name = "index")
    private Integer index;
    @XmlAttribute(name = "match")
    private String match;
    @XmlAttribute(name = "order")
    private Integer order;
    @XmlAttribute(name = "value")
    private String value;

    /**
     * Gets the value of the fields property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the fields property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFields().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Field }
     * {@link Fields }
     *
     *
     */
    public List<FieldsBase> getFields() {
        if (fields == null) {
            fields = new ArrayList<FieldsBase>();
        }
        return this.fields;
    }

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
     * @return possible object is {@link String }
     *
     */
    public String getAxis() {
        return axis;
    }

    /**
     * Sets the value of the axis property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setAxis(String value) {
        this.axis = value;
    }

    /**
     * Gets the value of the index property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     *
     * @param value
     *            allowed object is {@link Integer }
     *
     */
    public void setIndex(Integer value) {
        this.index = value;
    }

    /**
     * Gets the value of the match property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the value of the match property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setMatch(String value) {
        this.match = value;
    }

    /**
     * Gets the value of the order property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     *
     * @param value
     *            allowed object is {@link Integer }
     *
     */
    public void setOrder(Integer value) {
        this.order = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

}
