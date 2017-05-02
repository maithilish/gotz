package org.codetab.gotz.util;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceStream {

    static final Logger LOGGER = LoggerFactory.getLogger(ResourceStream.class);

    public InputStream getInputStream(String fileName) throws FileNotFoundException {
        InputStream stream = ResourceStream.class.getResourceAsStream(fileName);
        if (stream == null) {
            throw new FileNotFoundException(fileName);
        }
        return stream;
    }

    // public StreamSource getStreamSource(){
    // new File(pathname)
    // }
}
