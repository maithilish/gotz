package org.codetab.gotz.model;

import org.apache.commons.lang3.Validate;

public class Labels {

    private final String name;
    private final String group;
    private final String task;
    private final String dataDef;
    private final String label;

    public Labels(final String name, final String group) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");

        this.name = name;
        this.group = group;
        this.task = "na";
        this.dataDef = "na";
        this.label = String.join(":", name, group, dataDef);
    }

    public Labels(final String name, final String group, final String task,
            final String dataDef) {

        Validate.notNull(name, "name must not be null");
        Validate.notNull(group, "group must not be null");
        Validate.notNull(task, "task must not be null");
        Validate.notNull(dataDef, "dataDef must not be null");

        this.name = name;
        this.group = group;
        this.task = task;
        this.dataDef = dataDef;
        this.label = String.join(":", name, group, dataDef);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getTask() {
        return task;
    }

    public String getDataDef() {
        return dataDef;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Labels [name=" + name + ", group=" + group + ", task=" + task
                + ", dataDef=" + dataDef + "]";
    }

}
