/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * I/O utilities. This class should be replaced as soon as Jakarta Commons IO
 * announces its first release.
 * @todo Replace with Jakarta Commons I/O
 */
public class IOUtil {

    /**
     * Copies the contents of the InputStream over to the OutputStream. This 
     * method doesn't close the streams.
     * @param in InputStream to read from
     * @param out OutputStream to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        final int bufferSize = 2048;
        final byte[] buf = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
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
            copyStream(in, baout);
        } finally {
            baout.close();
        }
        return baout.toByteArray();
    }

}
