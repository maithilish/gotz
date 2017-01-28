package in.m.picks.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.Range;

import in.m.picks.exception.AfieldNotFoundException;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.Util;

@SuppressWarnings("serial")
@XmlRootElement(name = "datadef")
@XmlAccessorType(XmlAccessType.NONE)
public class DataDef extends Afields {

	private Long id;

	private String name;
	private String parser;
	private String type;
	private Date fromDate;
	private Date toDate;

	private List<DAxis> axes;

	private transient Set<Set<DMember>> memberSets;

	public DataDef() {
		axes = new ArrayList<DAxis>();
	}

	public final Long getId() {
		return id;
	}

	public final void setId(Long id) {
		this.id = id;
	}

	@XmlAttribute
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public final String getParser() {
		return parser;
	}

	public final void setParser(String parser) {
		this.parser = parser;
	}

	@XmlAttribute
	public final String getType() {
		return type;
	}

	public final void setType(String type) {
		// default type is data
		if (type == null) {
			type = "data";
		}
		this.type = type;
	}

	@XmlAttribute
	public final Date getFromDate() {
		return fromDate;
	}

	public final void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	@XmlAttribute
	public final Date getToDate() {
		return toDate;
	}

	public final void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	@XmlElement(name = "axis")
	public final List<DAxis> getAxes() {
		return axes;
	}

	public final void setAxes(List<DAxis> axes) {
		this.axes = axes;
	}

	@XmlTransient
	public final Set<Set<DMember>> getMemberSets() {
		return memberSets;
	}

	public final void setMemberSets(Set<Set<DMember>> memberSets) {
		this.memberSets = memberSets;
	}

	public final void addAxis(DAxis axis) {
		axes.add(axis);
	}

	public void setDefaults() {
		for (DAxis axis : axes) {
			axis.setDefaults();
		}
	}

	// returns null if when no axis is found
	// returns Afield with zero Afield for axis without any Afields
	public Afields getAxisAfieldsByGroup(String axisName, String group) {
		for (DAxis axis : axes) {
			if (axis.name.equals(axisName)) {
				return axis.getAfieldsByGroup(group);
			}
		}
		return null;
	}

	public Afields getAxisAfieldsByName(String axisName, String name) {
		for (DAxis axis : axes) {
			if (axis.name.equals(axisName)) {
				return axis.getAfieldsByName(name);
			}
		}
		return null;
	}

	public List<Afields> getAllAfields() {
		List<Afields> allAfields = new ArrayList<Afields>();
		allAfields.add(this);
		for (DAxis axis : getAxes()) {
			allAfields.add(axis);
			for (DMember member : axis.getMembers()) {
				allAfields.add(member);
			}
			allAfields.add(axis.getFilter());
		}
		return allAfields;
	}

	public Map<String, Afields> getFilterMap() {
		// TODO test
		Map<String, Afields> filterMap = new HashMap<String, Afields>();
		for (DAxis axis : axes) {
			filterMap.put(axis.getName(), axis.getFilter());
		}
		return filterMap;
	}

	private void generateMemberSets() {
		int axesSize = getAxes().size();
		Set<?>[] memberSets = new HashSet<?>[axesSize];
		for (int i = 0; i < axesSize; i++) {
			Set<DMember> members = getAxes().get(i).getMembers();
			Set<DMember> exMembers = expandMembers(members);
			memberSets[i] = exMembers;
		}
		Set<Set<Object>> cartesianSet = Util.cartesianProduct(memberSets);
		Set<Set<DMember>> dataDefMemberSets = new HashSet<Set<DMember>>();
		for (Set<?> set : cartesianSet) {
			/*
			 * memberSet array contains only Set<Member> as it is populated by
			 * getMemberSet method Hence safe to ignore the warning
			 */
			@SuppressWarnings("unchecked")
			Set<DMember> memberSet = (Set<DMember>) set;
			dataDefMemberSets.add(memberSet);
		}
		setMemberSets(dataDefMemberSets);
	}

	private Set<DMember> expandMembers(Set<DMember> members) {
		Set<DMember> exMembers = new HashSet<DMember>();
		for (DMember member : members) {
			// expand index based members
			try {
				Range<Integer> indexRange = AccessUtil.getRange(member,
						"indexRange");
				Set<DMember> newSet = new HashSet<DMember>();
				for (int index = indexRange.getMinimum(); index <= indexRange
						.getMaximum(); index++) {
					DMember newMember = new DMember(member);
					newMember.setIndex(index);
					newMember.setOrder(member.getOrder() + index - 1);
					newSet.add(newMember);
				}
				exMembers.addAll(newSet);
			} catch (AfieldNotFoundException e) {
				exMembers.add(member);
			} catch (NumberFormatException e) {
			}
		}
		return exMembers;
	}

	public Data getDataTemplate() {
		if (memberSets == null) {
			synchronized (this) {
				generateMemberSets();
			}
		}
		Data data = new Data();
		data.setDataDef(getName());
		for (Set<DMember> members : getMemberSets()) {
			Member dataMember = new Member();
			for (DMember member : members) {
				Axis axis = new Axis();
				axis.setName(member.getAxis());
				axis.setOrder(member.getOrder());
				axis.setIndex(member.getIndex());
				axis.setMatch(member.getMatch());
				// TODO refactor group handling and test
				if (member.getGroup() != null) {
					dataMember.setGroup(member.getGroup());
				}
				dataMember.addAxis(axis);
			}
			data.addMember(dataMember);
		}
		return data;
	}

	// public DataDef deepClone() throws IOException, ClassNotFoundException {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// ObjectOutputStream oos = new ObjectOutputStream(baos);
	// oos.writeObject(this);
	//
	// ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	// ObjectInputStream ois = new ObjectInputStream(bais);
	// return (DataDef) ois.readObject();
	// }

	@Override
	public String toString() {
		return "DataDef [name=" + name + ", parser=" + parser + ", type=" + type
				+ ", fromDate=" + fromDate + ", toDate=" + toDate + ", axes=" + axes
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axes == null) ? 0 : axes.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parser == null) ? 0 : parser.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DataDef other = (DataDef) obj;
		if (axes == null) {
			if (other.axes != null)
				return false;
		} else if (!axes.equals(other.axes))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parser == null) {
			if (other.parser != null)
				return false;
		} else if (!parser.equals(other.parser))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}

@XmlRootElement(name = "axis")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"members","filter"})
class DAxis extends Afields implements Serializable {

	private static final long serialVersionUID = 1L;

	String name;
	private Set<DMember> members;
	private DFilter filter;

	public DAxis() {
		members = new HashSet<DMember>();
	}

	@XmlAttribute
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		// TODO requires test
		this.name = name;
	}

	public final Set<DMember> getMembers() {
		return members;
	}

	@XmlElement(name = "member")
	public final void setMembers(Set<DMember> members) {
		this.members = members;
	}

	@XmlElement(name = "filter")
	public final DFilter getFilter() {
		return filter;
	}

	public final void setFilter(DFilter filter) {
		filter.setAxis(name);
		this.filter = filter;
	}

	/*
	 * datadef.xml may not have some fields and they are set here
	 */
	public void setDefaults() {
		// implied fact member
		if (name.equals("fact")) {
			DMember fact = new DMember();
			fact.setAxis(this.name);
			fact.setName("fact");
			fact.setOrder(0);
			members.add(fact);
		}
		// set member's axis name and order
		int i = 0;
		for (DMember member : this.members) {
			member.setAxis(name);
			member.setOrder(i++);
		}
	}

	@Override
	public String toString() {
		return "DAxis [name=" + name + ", members=" + members + ", filter=" + filter
				+ "] " + super.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
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
		DAxis other = (DAxis) obj;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.NONE)
class DMember extends Afields implements Serializable {

	private static final long serialVersionUID = 1L;

	String name;
	String axis;
	String match;
	Integer index;
	Integer order;
	String group;

	public DMember() {

	}

	public DMember(DMember member) {
		// deep copy
		this.name = member.getName();
		this.axis = member.getAxis();
		this.match = member.getMatch();
		this.index = member.getIndex();
		this.order = member.getOrder();
		this.group = member.getGroup();
		for (Afield afield : member.getAfields()) {
			addAfield(new Afield(afield));
		}
	}

	@XmlAttribute
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public final String getAxis() {
		return axis;
	}

	public final void setAxis(String axis) {
		this.axis = axis;
	}

	@XmlAttribute
	public final String getMatch() {
		return match;
	}

	public final void setMatch(String match) {
		this.match = match;
	}

	@XmlAttribute
	public final Integer getIndex() {
		return index;
	}

	public final void setIndex(Integer index) {
		this.index = index;
	}

	@XmlAttribute
	public final Integer getOrder() {
		return order;
	}

	public final void setOrder(Integer order) {
		this.order = order;
	}

	@XmlAttribute
	public final String getGroup() {
		return group;
	}

	public final void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "DMember [name=" + name + ", axis=" + axis + ", match=" + match
				+ ", index=" + index + ", order=" + order + ", group=" + group + "] "
				+ super.toString();
	}

	public String toStringMedium() {
		return "DMember [name=" + name + ", axis=" + axis + ", match=" + match
				+ ", index=" + index + ", order=" + order + ", group=" + group + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((match == null) ? 0 : match.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
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
		DMember other = (DMember) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equals(other.axis))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
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
		return true;
	}

}

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "filter")
class DFilter extends Afields implements Serializable {

	private static final long serialVersionUID = 1L;

	private String axis;

	@XmlAttribute
	public final String getAxis() {
		return axis;
	}

	public final void setAxis(String axis) {
		this.axis = axis;
	}

	@Override
	public String toString() {
		return "DFilter [axis=" + axis + "] " + super.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
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
		DFilter other = (DFilter) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equals(other.axis))
			return false;
		return true;
	}

}
