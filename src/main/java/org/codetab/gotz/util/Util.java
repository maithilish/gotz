package org.codetab.gotz.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.codetab.gotz.model.Wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Util {

    final public static String LINE = System.lineSeparator();

    private Util() {
    }

    /*
     * uses serialization to get deep clone of an object
     */
    public static <T> T deepClone(final Class<T> ofClass, final T obj)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ofClass.cast(ois.readObject());
    }

    public static boolean hasNulls(final Object... sets) {
        for (Object o : sets) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    public static Set<Set<Object>> cartesianProduct(final Set<?>... sets) {
        if (sets.length < 2) {
            throw new IllegalArgumentException(buildString(
                    "Can't have a product of fewer than two sets (has ",
                    String.valueOf(sets.length), " sets)"));
        }
        for (Set<?> set : sets) {
            if (set.size() == 0) {
                throw new IllegalArgumentException(
                        "argument contains a empty set");
            }
        }
        return cartesianProduct(0, sets);
    }

    private static Set<Set<Object>> cartesianProduct(final int index,
            final Set<?>... sets) {
        Set<Set<Object>> ret = new HashSet<Set<Object>>();
        if (index == sets.length) {
            ret.add(new HashSet<Object>());
        } else {
            for (Object obj : sets[index]) {
                for (Set<Object> set : cartesianProduct(index + 1, sets)) {
                    set.add(obj);
                    ret.add(set);
                }
            }
        }
        return ret;
    }

    public static String stripe(final String string, final int noOfLines,
            final String prefix, final String suffix) {
        StringBuffer sb = new StringBuffer();
        if (prefix != null) {
            sb.append(prefix);
        }
        if (StringUtils.countMatches(string, "\n") <= noOfLines) {
            sb.append(string);
        } else {
            sb.append(head(string, noOfLines));
            sb.append("\n      ...\n");
            sb.append(tail(string, noOfLines));
        }
        if (suffix != null) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    public static String head(final String string, final int noOfLines) {
        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        return string.substring(0, StringUtils.ordinalIndexOf(string, "\n", n));
    }

    public static String tail(final String string, final int noOfLines) {
        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        if (StringUtils.endsWith(string, "\n")) {
            n++;
        }
        return string
                .substring(StringUtils.lastOrdinalIndexOf(string, "\n", n) + 1);
    }

    public static String getJson(final Object obj, final boolean prettyPrint) {
        GsonBuilder gb = new GsonBuilder();
        if (prettyPrint) {
            gb.setPrettyPrinting();
            gb.serializeNulls();
            gb.disableHtmlEscaping();
        }
        Gson gson = gb.create();
        String json = gson.toJson(obj);
        return json;
    }

    public static String getIndentedJson(final Object obj,
            final boolean prettyPrint) {
        String json = getJson(obj, prettyPrint);
        String indentedJson = json.replaceAll("(?m)^", Util.logIndent());
        return indentedJson;
    }

    public static String buildString(final String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
        }
        return sb.toString();
    }

    /*
     * to marshal/unmarshal a list with a JAXB, we need to create a wrapper
     * object to hold the list such as list of datadefs, locators etc. It is
     * cumbersome to create multiple wrapper objects and to avoid this generic
     * list wrapper is useful. See - blog.bdoughan.com jaxb list wrapper
     */
    public static List<Object> unmarshal(final Unmarshaller um,
            final String xmlFile) throws JAXBException, FileNotFoundException {
        StreamSource xmlSource =
                new StreamSource(Util.getResourceAsStream(xmlFile));
        Wrapper wrapper = um.unmarshal(xmlSource, Wrapper.class).getValue();
        return wrapper.getAny();
    }

    public static InputStream getResourceAsStream(final String resource)
            throws FileNotFoundException {
        InputStream stream = Util.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new FileNotFoundException(
                    "Resource [" + resource + "] not found");
        }
        return stream;
    }

    public static String logIndent() {
        return "\t\t\t";
    }

    /*
     * Parse ISO-8601 duration format PnDTnHnMn.nS or period format PnYnMnWnD as
     * TemporalAmount (Duration or Period)
     */
    public static TemporalAmount praseTemporalAmount(final CharSequence text)
            throws DateTimeParseException {
        TemporalAmount ta;
        try {
            ta = Duration.parse(text);
        } catch (DateTimeParseException e) {
            ta = Period.parse(text);
        }
        return ta;
    }

    public static StringBuilder getState(final Object object) {
        String line = System.lineSeparator();
        String json = Util.getIndentedJson(object, true);
        String className = object.getClass().getName();
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append(line);
        sb.append(json);
        return sb;
    }

    public static String getMessage(final Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
    }
}
