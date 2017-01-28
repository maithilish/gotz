package in.m.picks.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "locators")
@XmlAccessorType(XmlAccessType.NONE)
public class Locators implements Serializable {

	private static final long serialVersionUID = 1L;

	List<Locator> locators;

	@XmlElement(name = "locator")
	public List<Locator> getLocators() {
		return locators;
	}

	public void setLocators(List<Locator> locators) {
		this.locators = locators;
	}

	@Override
	public String toString() {
		return "Locators [locators=" + locators + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locators == null) ? 0 : locators.hashCode());
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
		Locators other = (Locators) obj;
		if (locators == null) {
			if (other.locators != null)
				return false;
		} else if (!locators.equals(other.locators))
			return false;
		return true;
	}	
		
}

