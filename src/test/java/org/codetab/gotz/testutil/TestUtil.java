package org.codetab.gotz.testutil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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

    public static Fields createFields(String name, String value,
            FieldsBase... fieldsBase) {
        Fields fields = new Fields();
        fields.setName(name);
        fields.setValue(value);
        for (FieldsBase fb : fieldsBase) {
            fields.getFields().add(fb);
        }
        return fields;
    }

    public static List<String> readFileAsList(String fileName) {
        try {
            InputStream is = TestUtil.class.getResourceAsStream(fileName);
            return IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }

    public static void writeListToFile(List<String> list, String fileName)
            throws IOException {
        try (Writer wr = new FileWriter(fileName)) {
            IOUtils.writeLines(list, null, wr);
        }
    }
}
