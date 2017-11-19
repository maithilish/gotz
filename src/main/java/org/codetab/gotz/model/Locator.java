
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
 * Java class for locator complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="locator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}base"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://codetab.org/gotz}fields"/&gt;
 *         &lt;element name="xField" type="{http://codetab.org/xfield}xField" minOccurs="0"/&gt;
 *         &lt;element name="documents" type="{http://codetab.org/gotz}document" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="url" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "locator", propOrder = {"fields", "xField", "documents"})
public class Locator extends Base implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElements({@XmlElement(name = "field", type = Field.class),
            @XmlElement(type = Fields.class)})
    private List<FieldsBase> fields;
    @XmlElement
    private XField xField;
    @XmlElement
    private List<Document> documents;
    @XmlAttribute(name = "url")
    private String url;
    @XmlAttribute(name = "group")
    private String group;

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
     * Gets the value of the xField property.
     *
     * @return possible object is {@link XField }
     *
     */
    public XField getXField() {
        return xField;
    }

    /**
     * Sets the value of the xField property.
     *
     * @param value
     *            allowed object is {@link XField }
     *
     */
    public void setXField(XField value) {
        this.xField = value;
    }

    /**
     * Gets the value of the documents property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the documents property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDocuments().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Document
     * }
     *
     *
     */
    public List<Document> getDocuments() {
        if (documents == null) {
            documents = new ArrayList<Document>();
        }
        return this.documents;
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
    public void setUrl(String value) {
        this.url = value;
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
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId()).append("name", getName())
                .append("group", group).append("url", url)
                .append("fields", fields).append("xField", xField)
                .append("documents", documents).toString();
    }

}
