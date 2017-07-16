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

public final class TestUtil {

    private TestUtil() {
    }

    public static Field createField(final String name, final String value) {
        Field field = new Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public static Fields createFields(final String name, final String value,
            final FieldsBase... fieldsBase) {
        Fields fields = new Fields();
        fields.setName(name);
        fields.setValue(value);
        for (FieldsBase fb : fieldsBase) {
            fields.getFields().add(fb);
        }
        return fields;
    }

    public static List<FieldsBase> asList(final FieldsBase fb) {
        List<FieldsBase> list = new ArrayList<>();
        list.add(fb);
        return list;
    }

    public static List<String> readFileAsList(final String fileName) {
        try {
            InputStream is = TestUtil.class.getResourceAsStream(fileName);
            return IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }

    public static void writeListToFile(final List<Object> list,
            final String fileName) throws IOException {
        try (Writer wr = new FileWriter(fileName)) {
            IOUtils.writeLines(list, null, wr);
        }
    }
}
