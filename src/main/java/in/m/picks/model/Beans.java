package in.m.picks.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "beans")
@XmlAccessorType(XmlAccessType.NONE)
public class Beans implements Serializable {

	private static final long serialVersionUID = 1L;

	List<Bean> beans;

	@XmlElement(name = "bean")
	public List<Bean> getBeans() {
		return beans;
	}

	public void setBeans(List<Bean> beans) {
		this.beans = beans;
	}

	@Override
	public String toString() {
		return "Beans [beans=" + beans + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beans == null) ? 0 : beans.hashCode());
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
		Beans other = (Beans) obj;
		if (beans == null) {
			if (other.beans != null)
				return false;
		} else if (!beans.equals(other.beans))
			return false;
		return true;
	}	
		
}
