/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
        //Now, we've got to trick Java into forgetting the sign
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

        //Change byte order
        int high = (i & 0xFF00) >>> 8;
        int low  = (i & 0x00FF) << 8;
        return low + high;
    }

    /**
     * Parses a four byte value out of the stream.
     * 
     * @return The value extracted.
     */
    public long readInt() throws IOException {
        int i = inStream.readInt();

        //Change byte order
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
          buf.append((char) ch);
          ch = reader.read();
        }
        return buf.toString();
    }
} 