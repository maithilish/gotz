package org.codetab.gotz.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionUtil {

    static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtil.class);

    private CompressionUtil() {
    }

    public static byte[] compressByteArray(final byte[] input,
            final int bufferLength) throws IOException {

        if (null == input) {
            throw new IllegalArgumentException("input was null");
        }

        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[bufferLength];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        compressor.end();
        bos.close();

        return bos.toByteArray();
    }

    public static byte[] decompressByteArray(final byte[] input,
            final int bufferLength) {
        if (null == input) {
            throw new IllegalArgumentException("input was null");
        }

        final Inflater decompressor = new Inflater();

        decompressor.setInput(input);

        // Create an expandable byte array to hold the decompressed data
        final ByteArrayOutputStream baos =
                new ByteArrayOutputStream(input.length);

        final byte[] buf = new byte[bufferLength];

        try {
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                baos.write(buf, 0, count);
            }
        } catch (DataFormatException ex) {
            LOGGER.error("problem decompressing. {}", ex);
        }

        decompressor.end();

        try {
            baos.close();
        } catch (IOException ex) {
            LOGGER.error("problem closing stream.{}", ex);
        }

        return baos.toByteArray();
    }
}
