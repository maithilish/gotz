package in.m.picks.model;

import java.util.ArrayList;
import java.util.List;

public class Data extends Base {

	private static final long serialVersionUID = 1L;

	private String dataDef;
	private long dataDefId;
	private long documentId;
	private List<Member> members;

	public Data() {
		members = new ArrayList<Member>();
	}

	public final String getDataDef() {
		return dataDef;
	}

	public final void setDataDef(String dataDef) {
		this.dataDef = dataDef;
	}

	public final long getDataDefId() {
		return dataDefId;
	}

	public final void setDataDefId(long dataDefId) {
		this.dataDefId = dataDefId;
	}

	public long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	public final List<Member> getMembers() {
		return members;
	}

	public final void setMembers(List<Member> members) {
		this.members = members;
	}

	public void addMember(Member member) {
		members.add(member);
	}

	public String toStringIds() {
		return "Data [id=" + getId() + ", dataDefId=" + dataDefId
				+ ", documentId=" + documentId + "]";
	}
}
