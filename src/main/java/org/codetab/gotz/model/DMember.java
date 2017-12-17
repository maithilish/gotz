
package org.codetab.gotz.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://codetab.org/xfields}fields" minOccurs="0"/&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://codetab.org/gotz}nonEmptyString" /&gt;
 *       &lt;attribute name="axis" type="{http://codetab.org/gotz}nonEmptyString" /&gt;
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="match" type="{http://codetab.org/gotz}nonEmptyString" /&gt;
 *       &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "dMember", propOrder = {"fields", "id"})
public class DMember implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://codetab.org/xfields")
    private Fields fields;
    @XmlElement
    private Long id;
    @XmlAttribute(name = "name", required = true)
    private String name;
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
     * @return possible object is {@link Fields }
     *
     */
    public Fields getFields() {
        return fields;
    }

    /**
     * Sets the value of the fields property.
     *
     * @param value
     *            allowed object is {@link Fields }
     *
     */
    public void setFields(Fields value) {
        this.fields = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *            allowed object is {@link Long }
     *
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
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
