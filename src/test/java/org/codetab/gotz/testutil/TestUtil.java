package org.codetab.gotz.testutil;

import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;

public class TestUtil {

    public static Field createField(String name, String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public static Fields createFields(String name, String value, FieldsBase... fieldsBase) {
        Fields fields = new Fields();
        fields.setName(name);
        fields.setValue(value);
        for(FieldsBase fb : fieldsBase){
            fields.getFields().add(fb);
        }
        return fields;
    }
}
