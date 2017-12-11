package org.codetab.gotz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Data implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String dataDef;
    private Long dataDefId;
    private Long documentId;
    private List<Member> members = new ArrayList<Member>();

    public Data() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDataDef() {
        return dataDef;
    }

    public void setDataDef(final String dataDef) {
        this.dataDef = dataDef;
    }

    public Long getDataDefId() {
        return dataDefId;
    }

    public void setDataDefId(final Long dataDefId) {
        this.dataDefId = dataDefId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final Long documentId) {
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
        return "Data [id=" + id + ", dataDefId=" + dataDefId + ", documentId="
                + documentId + "]";
    }
}
