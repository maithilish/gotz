package in.m.picks.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Member extends Base {

	private static final long serialVersionUID = 1L;

	private String group;
	private Set<Axis> axes;
	private List<FieldsBase> fields;

	public Member() {
		axes = new HashSet<Axis>();
	}

	public final String getGroup() {
		return group;
	}

	public final void setGroup(String group) {
		this.group = group;
	}

	public final Set<Axis> getAxes() {
		return axes;
	}

	public final void setAxes(Set<Axis> axes) {
		this.axes = axes;
	}

	public List<FieldsBase> getFields() {
		return fields;
	}

	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
	}

	public final Axis getAxis(String axisName) {
		for (Axis axis : axes) {
			if (axis.getName().equalsIgnoreCase(axisName)) {
				return axis;
			}
		}
		return null;
	}

	public final Map<String, Axis> getAxisMap() {
		Map<String, Axis> axisMap = new HashMap<>();
		for (Axis axis : axes) {
			axisMap.put(axis.getName(), axis);
		}
		return axisMap;
	}

	public void addAxis(Axis axis) {
		axes.add(axis);
	}

	public String getValue(String axisName) {
		Axis axis = getAxis(axisName);
		return axis.getValue();
	}

	public StringBuilder traceMember() {
		StringBuilder sb = new StringBuilder();
		sb.append(toString());
		for (Axis axis : axes) {
			sb.append(axis);
		}
		return sb;
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
