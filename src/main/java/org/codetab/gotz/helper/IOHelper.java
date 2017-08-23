package org.codetab.gotz.helper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(IOHelper.class);

    public InputStream getInputStream(final String fileName)
            throws FileNotFoundException {
        InputStream stream = IOHelper.class.getResourceAsStream(fileName);
        if (stream == null) {
            throw new FileNotFoundException(fileName);
        }
        return stream;
    }

    public PrintWriter getPrintWriter(String fileName)
            throws FileNotFoundException {
        return new PrintWriter(fileName);
    }
}
