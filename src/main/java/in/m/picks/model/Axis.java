package in.m.picks.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import in.m.picks.util.FieldsUtil;

public class Axis implements Comparable<Axis>, Serializable {

	private static final long serialVersionUID = 1L;

	private AxisName name;
	private String value;
	private String match;
	private Integer index;
	private Integer order;
	private List<FieldsBase> fields;

	public Axis() {
	}

	public AxisName getName() {
		return name;
	}

	public void setName(AxisName name) {
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
		if (fields == null) {
			fields = new ArrayList<FieldsBase>();
		}
		return this.fields;
	}

	public int compareTo(Axis other) {
		// TODO write test
		// String name is converted to Enum AxisName and compared
		AxisName a1 = name;
		AxisName a2 = other.name;
		return a1.compareTo(a2);
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Axis [name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append(", match=");
		builder.append(match);
		builder.append(", index=");
		builder.append(index);
		builder.append(", order=");
		builder.append(order);
		builder.append(",");
		builder.append(FieldsUtil.getFormattedFields(fields));
		builder.append("]");
		return builder.toString();
	}

}
