package in.m.picks.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement(name = "afields")
@XmlAccessorType(XmlAccessType.NONE)
public class Afields implements Serializable {

	private static final long serialVersionUID = 1L;

	private String className;
	private List<Afield> afields;

	public Afields() {
		afields = new ArrayList<Afield>();
	}

	public Afields(Afields other) {
		// deep copy
		afields = new ArrayList<Afield>();
		for (Afield afield : other.getAfields()) {
			addAfield(new Afield(afield));
		}
	}

	@XmlAttribute
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@XmlElement(name = "afield")
	public final List<Afield> getAfields() {
		return afields;
	}

	public final void setAfields(List<Afield> afields) {
		this.afields = afields;
	}

	public final Afield getAfield(String name) {
		for (Afield afield : afields) {
			if (afield.getName().equals(name)) {
				return afield;
			}
		}
		return null;
	}

	// returns a deepcopy which is subset of this AFields
	public final Afields getAfieldsByGroup(String group) {
		Afields copy = new Afields();
		for (Afield afield : afields) {
			if (StringUtils.equals(afield.getGroup(), group)) {
				copy.addAfield(new Afield(afield));
			}
		}
		return copy;
	}

	// returns a deepcopy which is subset of this AFields
	public final Afields getAfieldsByName(String name) {
		Afields copy = new Afields();
		for (Afield afield : afields) {
			if (StringUtils.equals(afield.getName(), name)) {
				copy.addAfield(new Afield(afield));
			}
		}
		return copy;
	}

	public final void addAfield(Afield afield) {
		Afield existing = getAfield(afield.getName());
		if (existing != null) {
			afields.remove(existing);
		}
		afields.add(afield);
	}

	public final int size() {
		return afields.size();
	}

	@Override
	public String toString() {
		return "Afields [afields=" + afields + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((afields == null) ? 0 : afields.hashCode());
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
		Afields other = (Afields) obj;
		if (afields == null) {
			if (other.afields != null)
				return false;
		} else if (!afields.equals(other.afields))
			return false;
		return true;
	}

	public StringBuilder trace() {
		StringBuilder sb = new StringBuilder();
		for (Afield af : afields) {
			sb.append(af.trace());
			sb.append(System.lineSeparator());
		}
		return sb;
	}

}