//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.27 at 04:33:27 PM IST 
//

package in.m.picks.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "wrapper", propOrder = { "any" })
public class Wrapper implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
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
