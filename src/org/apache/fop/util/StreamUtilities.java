/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.zip.CRC32;

/**
 * General handy stream i/o methods.
 */
public class StreamUtilities {

    /**
     * Size of buffers. Duh.
     */
    public static final int BUFFER_SIZE = 4096; // cuz I like big buffers...

    /**
     * Binary copies bytes from an input stream to an output stream.
     * The process is buffered, so you shouldn't need
     * BufferedInput/OutputStreams. Flushes when it's finished, but does
     * not close either stream. Returns the number of bytes copied.
     * @param source InputStream to read from
     * @param sink OutputStream to write to
     * @return long the total number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static long streamCopy(InputStream source,
                                  OutputStream sink) throws IOException {
        // set table
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;

        // trough
        int scoop;
        while ((scoop = source.read(buffer)) >= 0) {
            if (scoop == 0) {
                System.out.println("zero scoop!");
            }
            sink.write(buffer, 0, scoop);
            total += scoop;
        }

        // do dishes
        sink.flush();

        return total;
    }

    /**
     * Method streamCopy.
     */
    /**
     * Binary copies up to the given number of bytes from an input
     * stream to an output stream. The process is buffered, so you
     * shouldn't need BufferedInput/OutputStreams.
     * Flushes when it's finished, but does not close either stream.
     * Throws an EOFExeption if there aren't enough bytes available to
     * transfer the requested amount.
     * @param source InputStream to read from
     * @param sink OutputStream to write to
     * @param howMany requested amount of bytes that are to be copied
     * @return long the total number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static long streamCopy(InputStream source,
                                  OutputStream sink, int howMany) throws IOException {
        // set table
        byte[] buffer = new byte[BUFFER_SIZE];
        int left = howMany;

        // trough
        int scoop;
        while (left > 0) {
            scoop = source.read(buffer, 0, Math.min(BUFFER_SIZE, left));
            if (scoop < 0) {
                throw new EOFException(
                  "Not enough bytes to feed you in "
                  + "IOLib.streamCopy(source, sink, howMany); you asked for " 
                  + howMany + " and I only have " + (howMany - left));
            }

            sink.write(buffer, 0, scoop);
            left -= scoop;
        }

        // do dishes
        sink.flush();

        return howMany;
    }

    /**
     * Method streamCopyWithChecksum.
     */
    /**
     * Binary copies up to the given number of bytes from an input
     * stream to an output stream. The process is buffered, so you
     * shouldn't need BufferedInput/OutputStreams.
     * Flushes when it's finished, but does not close either stream.
     * Throws an EOFExeption if there aren't enough bytes available
     * to transfer the requested amount.
     * @param source InputStream to read from
     * @param sink OutputStream to write to
     * @param howMany requested amount of bytes that are to be copied
     * @return long the checksum of the bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static long streamCopyWithChecksum(InputStream source,
            OutputStream sink, int howMany) throws IOException {
        // set table
        byte[] buffer = new byte[BUFFER_SIZE];
        int left = howMany;
        CRC32 checksummer = new CRC32();

        // trough
        int scoop;
        while (left > 0) {
            scoop = source.read(buffer, 0, Math.min(BUFFER_SIZE, left));
            if (scoop < 0) {
                throw new EOFException("Not enough bytes to feed you in "
                        + "IOLib.streamCopy(source, sink, howMany)");
            }

            checksummer.update(buffer, 0, scoop);
            sink.write(buffer, 0, scoop);
            left -= scoop;
        }

        // do dishes
        sink.flush();

        return checksummer.getValue();
    }

    /**
     * Method dataCopy.
     */
    /**
     * Binary copies up to the given number of bytes from a DataInput
     * object to an DataOutput object. The process is buffered. Since
     * DataOutput doesn't support closing or flushing, it does neither.
     * @param source DataInput to read from
     * @param sink DataOutput to write to
     * @param howMany requested amount of bytes that are to be copied
     * @return long the total number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static long dataCopy(DataInput source, DataOutput sink,
                                int howMany) throws IOException {
        // set table
        byte[] buffer = new byte[BUFFER_SIZE];
        int left = howMany;

        // trough
        int scoop;
        while (left > 0) {
            scoop = Math.min(BUFFER_SIZE, left);
            source.readFully(buffer, 0, scoop);
            sink.write(buffer, 0, scoop);
            left -= scoop;
        }

        // do dishes
        return howMany;
    }


    /**
     * Loads the contents of the InputStream to a byte array. The InputStream 
     * isn't closed.
     * @param in InputStream to read from
     * @param initialTargetBufferSize initial number of bytes to allocate 
     *      (expected size to avoid a lot of reallocations)
     * @return byte[] the array of bytes requested
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray(InputStream in, int initialTargetBufferSize) 
                throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream(initialTargetBufferSize);
        try {
            streamCopy(in, baout);
        } finally {
            baout.close();
        }
        return baout.toByteArray();
    }

}
