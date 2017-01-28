package in.m.picks.model;

import java.util.HashSet;
import java.util.Set;

public class Member extends Afields {

	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String group;
	private Set<Axis> axes;

	public Member() {
		axes = new HashSet<Axis>();
	}

	// make deep copy
	public Member(Member member) {
		this.id = member.getId();
		this.name = member.getName();
		this.group = member.getGroup();

		axes = new HashSet<Axis>();
		for (Axis axis : member.getAxes()) {
			addAxis(new Axis(axis));
		}
		for (Afield afield : member.getAfields()) {
			addAfield(new Afield(afield));
		}
	}

	public final long getId() {
		return id;
	}

	public final void setId(long id) {
		this.id = id;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
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

	public final Axis getAxis(String axisName) {
		for (Axis axis : axes) {
			if (axis.getName().equalsIgnoreCase(axisName)) {
				return axis;
			}
		}
		return null;
	}

	public void addAxis(Axis axis) {
		axes.add(axis);
	}

	public String getValue(String axisName) {
		Axis axis = getAxis(axisName);
		return axis.getValue();
	}

	@Override
	public String toString() {
		return "Member [id=" + id + ", name=" + name + "] " + super.toString();
	}

	public StringBuilder traceMember() {
		StringBuilder sb = new StringBuilder();
		sb.append(toString());
		for (Axis axis : axes) {
			sb.append(axis);
		}
		return sb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((axes == null) ? 0 : axes.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Member other = (Member) obj;
		if (axes == null) {
			if (other.axes != null)
				return false;
		} else if (!axes.equals(other.axes))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
