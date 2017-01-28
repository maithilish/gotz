package in.m.picks.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "afieldslist")
@XmlAccessorType(XmlAccessType.NONE)
public class AfieldsList implements Serializable {

	private static final long serialVersionUID = 1L;

	List<Afields> afields;
	
	@XmlElement(name="afields")
	public List<Afields> getAfields() {
		return afields;
	}

	public void setAfields(List<Afields> afields) {
		this.afields = afields;
	}

	
	@Override
	public String toString() {
		return "AfieldsList [afields=" + afields + "]";
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
		AfieldsList other = (AfieldsList) obj;
		if (afields == null) {
			if (other.afields != null)
				return false;
		} else if (!afields.equals(other.afields))
			return false;
		return true;
	}
		
}
