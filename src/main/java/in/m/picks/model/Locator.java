package in.m.picks.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "locator")
@XmlAccessorType(XmlAccessType.NONE)
public final class Locator extends Afields {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String group;
	private String url;
	private List<Document> documents;

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
	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	@XmlAttribute
	public final String getGroup() {
		return group;
	}

	public final void setGroup(String group) {
		this.group = group;
	}

	public final List<Document> getDocuments() {
		return documents;
	}

	public final void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public final void addDocument(Document document) {
		if (documents == null) {
			documents = new ArrayList<Document>();
		}
		documents.add(document);
	}

	@Override
	public String toString() {
		return "Locator [id=" + id + ", name=" + name + ", group=" + group + "]";
	}

	public String toStringMedium() {
		return "Locator [name=" + name + ", group=" + group + ", url=" + url + "]";
	}

}
