/*
 * $Id: TTFDirTabEntry.java,v 1.2 2003/03/06 17:43:06 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * This class represents an entry to a TrueType font's Dir Tab.
 */
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
        String tagStr = new String(tag, "ISO-8859-1");
        // System.err.println("tag='" + tagStr + "'");

        //System.out.println(this.toString());
        return tagStr;
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
     * Returns the tag bytes.
     * @return byte[]
     */
    public byte[] getTag() {
        return tag;
    }

    /**
     * Returns the tag bytes.
     * @return byte[]
     */
    public String getTagString() {
        try {
            return new String(tag, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return this.toString(); // Should never happen.
        }
    }

}
