package in.m.picks.model;

import java.util.Date;

public class Document {

	private Long id;
	private Date fromDate;
	private Date toDate;
	private String url;
	private Object documentObject;

	public final Long getId() {
		return id;
	}

	public final void setId(Long id) {
		this.id = id;
	}

	public final Date getFromDate() {
		return fromDate;
	}

	public final void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public final Date getToDate() {
		return toDate;
	}

	public final void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public Object getDocumentObject() {
		return documentObject;
	}

	public void setDocumentObject(Object documentObject) {
		this.documentObject = documentObject;
	}

	public final boolean isDocumentLoaded() {
		if (documentObject == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		return "Document [id=" + id + ", fromDate=" + fromDate + ", toDate=" + toDate
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Document other = (Document) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
