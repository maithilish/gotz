package org.codetab.gotz.model;

import java.util.ArrayList;
import java.util.List;

public final class Data extends Base {

    private static final long serialVersionUID = 1L;

    private String dataDef;
    private long dataDefId;
    private long documentId;
    private List<Member> members = new ArrayList<Member>();

    public Data() {
    }

    public String getDataDef() {
        return dataDef;
    }

    public void setDataDef(final String dataDef) {
        this.dataDef = dataDef;
    }

    public long getDataDefId() {
        return dataDefId;
    }

    public void setDataDefId(final long dataDefId) {
        this.dataDefId = dataDefId;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final long documentId) {
        this.documentId = documentId;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(final List<Member> members) {
        this.members = members;
    }

    public void addMember(final Member member) {
        members.add(member);
    }

    public String toStringIds() {
        return "Data [id=" + getId() + ", dataDefId=" + dataDefId
                + ", documentId=" + documentId + "]";
    }
}
