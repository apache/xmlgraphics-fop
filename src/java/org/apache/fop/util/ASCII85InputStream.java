/*
 * $Id$
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
package org.apache.fop.util;

import java.io.InputStream;
import java.io.IOException;

/**
 * This class applies a ASCII85 decoding to the stream.
 * <p>
 * The class is derived from InputStream instead of FilteredInputStream because
 * we can use the read(byte[], int, int) method from InputStream which simply
 * delegates to read(). This makes the implementation easier.
 * <p>
 * The filter is described in chapter 3.13.3 of the PostScript Language 
 * Reference (third edition).
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public class ASCII85InputStream extends InputStream
            implements ASCII85Constants {

    private InputStream in;
    private boolean eodReached = false;
    private int[] b = new int[4]; //decoded
    private int bSize = 0;
    private int bIndex = 0; 

    /** @see java.io.FilterInputStream **/
    public ASCII85InputStream(InputStream in) {
        super();
        this.in = in;
    }

    /** @see java.io.FilterInputStream **/
    public int read() throws IOException {
        //Check if we need to read the next tuple
        if (bIndex >= bSize) {
            if (eodReached) {
                return -1;
            } 
            readNextTuple();
            if (bSize == 0) {
                if (!eodReached) {
                    throw new IllegalStateException("Internal error");
                }
                return -1;
            }
        }
        int result = b[bIndex];
        result = (result < 0 ? 256 + result : result);
        bIndex++;
        return result;
    }
    
    private int filteredRead() throws IOException {
        int buf;
        while (true) {
            buf = in.read();
            switch (buf) {
                case 0: //null
                case 9: //tab
                case 10: //LF
                case 12: //FF
                case 13: //CR
                case 32: //space
                    continue; //ignore
                case ZERO:
                case 126: //= EOD[0] = '~'
                    return buf;
                default:
                    if ((buf >= START) && (buf <= END)) {
                        return buf;
                    } else {
                        throw new IOException("Illegal character detected: " + buf);
                    }
            }
        }
    }
    
    private void handleEOD() throws IOException {
        final int buf = in.read();
        if (buf != EOD[1]) {
            throw new IOException("'>' expected after '~' (EOD)");
        }
        eodReached = true;
        bSize = 0;
        bIndex = 0;
    }
    
    private void readNextTuple() throws IOException {
        int buf;
        long tuple = 0;
        //Read ahead and check for special "z"
        buf = filteredRead();
        if (buf == ZERO) {
            java.util.Arrays.fill(b, 0);
            bSize = 4;
            bIndex = 0;
        } else if (buf == EOD[0]) {
            handleEOD();
        } else {
            int cIndex = 0;
            tuple = (buf - START) * POW85[cIndex];
            //System.out.println(cIndex + ": " + Long.toHexString(tuple));
            cIndex++;
            while (cIndex < 5) {
                buf = filteredRead();
                if (buf == EOD[0]) {
                    handleEOD();
                    break;
                } else if (buf == ZERO) {
                    //Violation 2
                    throw new IOException("Illegal 'z' within tuple");
                } else {
                    tuple += (buf - START) * POW85[cIndex];
                    //System.out.println(cIndex + ": " + Long.toHexString(tuple));
                    cIndex++;
                }
            }
            int cSize = cIndex;
            if (cSize == 1) {
                //Violation 3
                throw new IOException("Only one character in tuple");
            }
            //Handle optional, trailing, incomplete tuple 
            while (cIndex < 5) {
                tuple += POW85[cIndex - 1];
                cIndex++;
            }
            if (tuple > (2L << 31) - 1) {
                //Violation 1
                throw new IOException("Illegal tuple (> 2^32 - 1)");
            }
            //Convert tuple
            b[0] = (byte)((tuple >> 24) & 0xFF);
            b[1] = (byte)((tuple >> 16) & 0xFF);
            b[2] = (byte)((tuple >> 8) & 0xFF);
            b[3] = (byte)((tuple) & 0xFF);
            bSize = cSize - 1;
            bIndex = 0;
        }
    }

}


