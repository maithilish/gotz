package in.m.picks.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bean")
@XmlAccessorType(XmlAccessType.NONE)
public class Bean {

	private String name;
	private String className;
	private String xmlFile;
	private String schemaFile;

	public Bean() {
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@XmlAttribute
	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	@XmlAttribute
	public String getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	@Override
	public String toString() {
		return "Bean [name=" + name + ", className=" + className + ", xmlFile="
				+ xmlFile + ", schemaFile=" + schemaFile + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((schemaFile == null) ? 0 : schemaFile.hashCode());
		result = prime * result + ((xmlFile == null) ? 0 : xmlFile.hashCode());
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
		Bean other = (Bean) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (schemaFile == null) {
			if (other.schemaFile != null)
				return false;
		} else if (!schemaFile.equals(other.schemaFile))
			return false;
		if (xmlFile == null) {
			if (other.xmlFile != null)
				return false;
		} else if (!xmlFile.equals(other.xmlFile))
			return false;
		return true;
	}

}
