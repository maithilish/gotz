package in.m.picks.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "afield")
@XmlAccessorType(XmlAccessType.NONE)
public class Afield implements Serializable {

	private static final long serialVersionUID = 1L;

	private String group;
	private String name;
	private String value;
	private Boolean cascade;

	public Afield() {
	}

	public Afield(Afield afield) {
		this.group = afield.getGroup();
		this.name = afield.getName();
		this.value = afield.getValue();
		this.cascade = afield.isCascade();
	}

	public Afield(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@XmlAttribute
	public final String getGroup() {
		return group;
	}

	public final void setGroup(String group) {
		this.group = group;
	}

	@XmlAttribute
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(required = true)
	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	@XmlAttribute(required=false)
	public final Boolean isCascade() {
		return cascade;
	}

	public final void setCascade(Boolean cascade) {
		this.cascade = cascade;
	}

	@Override
	public String toString() {
		return "Afield [group=" + group + ", name=" + name + ", value=" + value
				+ ", cascade=" + cascade + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cascade == null) ? 0 : cascade.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Afield other = (Afield) obj;
		if (cascade == null) {
			if (other.cascade != null)
				return false;
		} else if (!cascade.equals(other.cascade))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public StringBuilder trace() {
		StringBuilder sb = new StringBuilder();
		sb.append("Afield [group=");
		sb.append(group);
		sb.append(", name=");
		sb.append(name);
		sb.append(", value=");
		sb.append(value);
		sb.append(", cascade=");
		sb.append(cascade);
		sb.append("]");
		return sb;
	}
}