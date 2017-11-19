
package org.codetab.gotz.model;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1 extends XmlAdapter<String, Date> {

    public Date unmarshal(String value) {
        return (org.codetab.gotz.model.DateAdapter.parseDate(value));
    }

    public String marshal(Date value) {
        return (org.codetab.gotz.model.DateAdapter.printDate(value));
    }

}
