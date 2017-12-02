package org.codetab.gotz.model;

import org.apache.commons.lang3.Validate;

public class Labels {

    private final String name;
    private final String group;
    private final String label;

    public Labels(final String name, final String group) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

        this.name = name;
        this.group = group;
        this.label = String.join(":", name, group);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Labels [name=" + name + ", group=" + group + ", label=" + label
                + "]";
    }

}
