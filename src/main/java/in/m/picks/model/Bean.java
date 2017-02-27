//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.27 at 04:33:27 PM IST 
//

package in.m.picks.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "bean")
public class Bean implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlAttribute(name = "name", required = true)
	protected String name;
	@XmlAttribute(name = "className", required = true)
	protected String className;
	@XmlAttribute(name = "xmlFile", required = true)
	protected String xmlFile;
	@XmlAttribute(name = "schemaFile")
	protected String schemaFile;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String value) {
		this.className = value;
	}

	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String value) {
		this.xmlFile = value;
	}

	public String getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(String value) {
		this.schemaFile = value;
	}

	@Override
	public boolean equals(Object obj) {
		String[] excludes = { "id", "dnDetachedState", "dnFlags", "dnStateManager" };
		return EqualsBuilder.reflectionEquals(this, obj, excludes);
	}

	@Override
	public int hashCode() {
		String[] excludes = { "id", "dnDetachedState", "dnFlags", "dnStateManager" };
		return HashCodeBuilder.reflectionHashCode(this, excludes);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

}
