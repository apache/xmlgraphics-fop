/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;
import java.io.*;

class TTFDirTabEntry {
    byte[] tag;
    int checksum;
    long offset;
    long length;

    TTFDirTabEntry() {
        tag = new byte[4];
    }

    /**
     * Read Dir Tab, return tag name
     */
    public String read(FontFileReader in) throws IOException {
        tag[0] = in.readTTFByte();
        tag[1] = in.readTTFByte();
        tag[2] = in.readTTFByte();
        tag[3] = in.readTTFByte();

        in.skip(4);    // Skip checksum

        offset = in.readTTFULong();
        length = in.readTTFULong();

        /*
         * System.out.println ("Read dir tab [" + tag[0]+
         * " "+tag[1] +
         * " "+tag[2] +
         * " "+tag[3] +
         * "] offset: " + offset +
         * " length: " + length +
         * " name: " + new String(tag));
         */
        return new String(tag, "ISO-8859-1");
    }

}
