/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.io.IOException;

class TTFDirTabEntry {
    
    private byte[] tag = new byte[4];
    private int checksum;
    private long offset;
    private long length;

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

        //System.out.println(this.toString());
        return new String(tag, "ISO-8859-1");
    }
    
    
    public String toString() {
        return "Read dir tab [" 
            + tag[0] + " " + tag[1] + " " + tag[2] + " " + tag[3] + "]"
            + " offset: " + offset
            + " length: " + length 
            + " name: " + tag;
    }

    /**
     * Returns the checksum.
     * @return int
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * Returns the length.
     * @return long
     */
    public long getLength() {
        return length;
    }

    /**
     * Returns the offset.
     * @return long
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the tag.
     * @return byte[]
     */
    public byte[] getTag() {
        return tag;
    }

}
