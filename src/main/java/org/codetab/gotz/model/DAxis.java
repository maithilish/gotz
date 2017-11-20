
package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Java class for dAxis complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="dAxis"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://codetab.org/xfield}xfield" minOccurs="0"/&gt;
 *         &lt;element ref="{http://codetab.org/gotz}member" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://codetab.org/gotz}filter" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "dAxis", propOrder = {"xfield", "member", "filter"})
public class DAxis extends Base implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://codetab.org/xfield")
    private XField xfield;
    @XmlElement
    private Set<DMember> member = new HashSet<DMember>();
    @XmlElement
    private DFilter filter;

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
     * Gets the value of the member property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the member property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getMember().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DMember }
     *
     *
     */
    public Set<DMember> getMember() {
        if (member == null) {
            member = new HashSet<DMember>();
        }
        return this.member;
    }

    /**
     * Gets the value of the filter property.
     *
     * @return possible object is {@link DFilter }
     *
     */
    public DFilter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param value
     *            allowed object is {@link DFilter }
     *
     */
    public void setFilter(DFilter value) {
        this.filter = value;
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
