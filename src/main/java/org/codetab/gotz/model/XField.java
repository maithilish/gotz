
package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.codetab.gotz.util.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * Java class for xField complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="xField"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xField", namespace = "http://codetab.org/xfield",
        propOrder = {"nodes"})
public class XField implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlAnyElement(lax = true)
    private List<Node> nodes;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "class")
    private String clazz;
    @XmlAttribute(name = "group")
    private String group;

    /**
     * Gets the value of the nodes property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the nodes property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getNodes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Element }
     * {@link Object }
     *
     *
     */
    public List<Node> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<Node>();
        }
        return this.nodes;
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
     * Gets the value of the clazz property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the group property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setGroup(String value) {
        this.group = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("xfield : {name: ");
        builder.append(name);
        builder.append(", group: ");
        builder.append(group);
        builder.append(", clazz: ");
        builder.append(clazz);
        builder.append("}");
        builder.append(System.lineSeparator());
        try {
            builder.append(XmlUtils.toXML(getNodes(), true, 4));
        } catch (TransformerException e) {
            builder.append(e.getLocalizedMessage());
        }
        return builder.toString();
    }

    /**
     * two nodes with same contents are not equal, so nodes are converted to xml
     * and compared.
     */
    @Override
    public boolean equals(final Object obj) {
        String[] excludes =
                {"id", "nodes", "dnDetachedState", "dnFlags", "dnStateManager"};
        boolean isEqual = EqualsBuilder.reflectionEquals(this, obj, excludes);

        try {
            String lXml = XmlUtils.toXML(getNodes());
            String rXml = (obj == null) ? null
                    : XmlUtils.toXML(((XField) obj).getNodes());
            if (!lXml.equals(rXml)) {
                isEqual = false;
            }
        } catch (TransformerException e) {
            isEqual = false;
        }
        return isEqual;
    }

    /**
     * two nodes with same contents returns hash, so nodes converted to xml and
     * hash is calculated.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        // get nodes as string and uses its hash
        try {
            result = prime * result + XmlUtils.toXML(getNodes()).hashCode();
        } catch (Exception e) {
        }
        return result;
    }

}
