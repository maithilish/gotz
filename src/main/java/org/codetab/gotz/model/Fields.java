
package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codetab.gotz.model.iterator.FieldsIterator;

/**
 * <p>
 * Java class for fields complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="fields"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://codetab.org/gotz}fieldsBase"&gt;
 *       &lt;group ref="{http://codetab.org/gotz}fields"/&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "fields", propOrder = {"fields"})
public class Fields extends FieldsBase implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElements({@XmlElement(name = "field", type = Field.class),
            @XmlElement(type = Fields.class)})
    private List<FieldsBase> fields;
    @XmlAttribute(name = "name")
    private String name;
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
        return indentToString("   ");
    }

    @Override
    public String indentToString(String indent) {
        String aindent = "   ";
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        builder.append(indent);
        builder.append("- fields: {name: ");
        builder.append(name);
        builder.append(", value: ");
        builder.append(value);
        builder.append("}");
        for (FieldsBase fb : fields) {
            builder.append(fb.indentToString(indent + aindent));
        }
        return builder.toString();
    }

    @Override
    public Iterator<FieldsBase> iterator() {
        return new FieldsIterator(fields);
    }

}
