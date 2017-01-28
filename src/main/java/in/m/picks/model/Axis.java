package in.m.picks.model;

public class Axis extends Afields implements Comparable<Axis> {

	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	private String match;
	private Integer index;
	private Integer order;

	public Axis() {
	}

	// make deep copy
	public Axis(Axis axis) {
		this.name = axis.getName();
		this.value = axis.getValue();
		this.match = axis.getMatch();
		this.index = axis.getIndex();
		this.order = axis.getOrder();
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

	@Override
	public String toString() {
		return "Axis [name=" + name + ", value=" + value + ", match=" + match
				+ ", index=" + index + ", order=" + order + "]";
	}

	public int compareTo(Axis other) {
		// TODO write test
		// String name is converted to Enum AxisName and compared
		AxisName a1 = AxisName.valueOf(name.toUpperCase());
		AxisName a2 = AxisName.valueOf(other.name.toUpperCase());
		return a1.compareTo(a2);
	}

	// TODO write test for hash and equal as axis is added to a Set

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((match == null) ? 0 : match.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Axis other = (Axis) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (match == null) {
			if (other.match != null)
				return false;
		} else if (!match.equals(other.match))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (order == null) {
			if (other.order != null)
				return false;
		} else if (!order.equals(other.order))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
