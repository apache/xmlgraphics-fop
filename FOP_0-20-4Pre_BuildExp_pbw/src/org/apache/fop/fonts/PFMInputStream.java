/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.io.*;

/**
 * This is a helper class for reading PFM files. It defines functions for
 * extracting specific values out of the stream.
 *
 * @author  jeremias.maerki@outline.ch
 */
public class PFMInputStream extends java.io.FilterInputStream {

    DataInputStream inStream;

    /**
     * Constructs a PFMInputStream based on an InputStream representing the PFM file.
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    public PFMInputStream(InputStream in) {
        super(in);
        inStream = new DataInputStream(in);
    }

    /**
     * Parses a one byte value out of the stream.
     *
     * @return The value extracted.
     */
    public short readByte() throws IOException {
        short s = inStream.readByte();
        // Now, we've got to trick Java into forgetting the sign
        int s1 = (((s & 0xF0) >>> 4) << 4) + (s & 0x0F);
        return (short)s1;
    }

    /**
     * Parses a two byte value out of the stream.
     *
     * @return The value extracted.
     */
    public int readShort() throws IOException {
        int i = inStream.readShort();

        // Change byte order
        int high = (i & 0xFF00) >>> 8;
        int low = (i & 0x00FF) << 8;
        return low + high;
    }

    /**
     * Parses a four byte value out of the stream.
     *
     * @return The value extracted.
     */
    public long readInt() throws IOException {
        int i = inStream.readInt();

        // Change byte order
        int i1 = (i & 0xFF000000) >>> 24;
        int i2 = (i & 0x00FF0000) >>> 8;
        int i3 = (i & 0x0000FF00) << 8;
        int i4 = (i & 0x000000FF) << 24;
        return i1 + i2 + i3 + i4;
    }

    /**
     * Parses a zero-terminated string out of the stream.
     *
     * @return The value extracted.
     */
    public String readString() throws IOException {
        InputStreamReader reader = new InputStreamReader(in, "ISO-8859-1");
        StringBuffer buf = new StringBuffer();
        int ch = reader.read();
        while (ch != 0) {
            buf.append((char)ch);
            ch = reader.read();
        }
        return buf.toString();
    }

}
