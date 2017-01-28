package in.m.picks.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datadefs")
@XmlAccessorType(XmlAccessType.NONE)
public class DataDefs implements Serializable {

	private static final long serialVersionUID = 1L;

	List<DataDef> dataDefs;

	@XmlElement(name = "datadef")
	public List<DataDef> getDataDefs() {
		return dataDefs;
	}

	public void setDataDefs(List<DataDef> dataDefs) {
		this.dataDefs = dataDefs;
	}

	@Override
	public String toString() {
		return "DataDefs [dataDefs=" + dataDefs + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataDefs == null) ? 0 : dataDefs.hashCode());
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
		DataDefs other = (DataDefs) obj;
		if (dataDefs == null) {
			if (other.dataDefs != null)
				return false;
		} else if (!dataDefs.equals(other.dataDefs))
			return false;
		return true;
	}	
}
