package in.m.picks.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import in.m.picks.util.FieldsUtil;

public final class Axis implements Comparable<Axis>, Serializable {

    private static final long serialVersionUID = 1L;

    private AxisName name;
    private String value;
    private String match;
    private Integer index;
    private Integer order;
    private List<FieldsBase> fields;

    public Axis() {
    }

    public AxisName getName() {
        return name;
    }

    public void setName(final AxisName name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(final String match) {
        this.match = match;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(final Integer index) {
        this.index = index;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public List<FieldsBase> getFields() {
        if (fields == null) {
            fields = new ArrayList<FieldsBase>();
        }
        return this.fields;
    }

    @Override
    public int compareTo(final Axis other) {
        // TODO write test
        // String name is converted to Enum AxisName and compared
        AxisName a1 = name;
        AxisName a2 = other.name;
        return a1.compareTo(a2);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Axis [name=");
        builder.append(name);
        builder.append(", value=");
        builder.append(value);
        builder.append(", match=");
        builder.append(match);
        builder.append(", index=");
        builder.append(index);
        builder.append(", order=");
        builder.append(order);
        builder.append(",");
        builder.append(FieldsUtil.getFormattedFields(fields));
        builder.append("]");
        return builder.toString();
    }

}
