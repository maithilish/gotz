package org.codetab.gotz.helper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

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

    public URL getURL(final String fileName) throws FileNotFoundException {
        URL url = IOHelper.class.getResource(fileName);
        if (url == null) {
            throw new FileNotFoundException(fileName);
        }
        return url;
    }

    public StreamSource getStreamSource(final String fileName)
            throws FileNotFoundException {
        InputStream is = getInputStream(fileName);
        return new StreamSource(is);
    }

    public PrintWriter getPrintWriter(final String fileName)
            throws FileNotFoundException {
        return new PrintWriter(fileName);
    }
}
