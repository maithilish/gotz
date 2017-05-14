package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DataTest {

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data();
    }

    @Test
    public void testData() {
        assertThat(data.getMembers()).isNotNull();
    }

    @Test
    public void testGetDataDef() {
        data.setDataDef("x");
        assertThat(data.getDataDef()).isEqualTo("x");
    }

    @Test
    public void testGetDataDefId() {
        data.setDataDefId(10L);
        assertThat(data.getDataDefId()).isEqualTo(10L);
    }


    @Test
    public void testGetDocumentId() {
        data.setDocumentId(20L);
        assertThat(data.getDocumentId()).isEqualTo(20L);
    }

    @Test
    public void testGetMembers() {
        List<Member> members = new ArrayList<>();
        data.setMembers(members);
        assertThat(data.getMembers()).isSameAs(members);
    }

    @Test
    public void testAddMember() {
        Member member = new Member();
        member.setName("x");
        member.setGroup("y");

        List<Member> members = new ArrayList<>();
        data.setMembers(members);
        data.addMember(member);

        assertThat(data.getMembers()).contains(member);
    }

    @Test
    public void testToStringIds() {
        data.setId(1L);
        data.setDataDefId(2L);
        data.setDocumentId(3L);

        String expected = "Data [id=1, dataDefId=2, documentId=3]";

        assertThat(data.toStringIds()).isEqualTo(expected);
    }
}
