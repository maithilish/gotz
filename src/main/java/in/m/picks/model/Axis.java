package in.m.picks.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Axis implements Comparable<Axis>, Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	private String match;
	private Integer index;
	private Integer order;
	private List<FieldsBase> fields;

	public Axis() {
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getMatch() {
		return match;
	}

	public final void setMatch(String match) {
		this.match = match;
	}

	public final Integer getIndex() {
		return index;
	}

	public final void setIndex(Integer index) {
		this.index = index;
	}

	public final Integer getOrder() {
		return order;
	}

	public final void setOrder(Integer order) {
		this.order = order;
	}

	public List<FieldsBase> getFields() {
		return fields;
	}

	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
	}

	public int compareTo(Axis other) {
		// TODO write test
		// String name is converted to Enum AxisName and compared
		AxisName a1 = AxisName.valueOf(name.toUpperCase());
		AxisName a2 = AxisName.valueOf(other.name.toUpperCase());
		return a1.compareTo(a2);
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

}
