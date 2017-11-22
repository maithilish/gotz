
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
 * Java class for dFilter complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="dFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fields" type="{http://codetab.org/xfields}fields" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="axis" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "dFilter", propOrder = {"fields"})
public class DFilter extends Base implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement
    private Fields fields;
    @XmlAttribute(name = "axis")
    private String axis;

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
