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
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class applies a ASCII85 encoding to the stream.
 *
 * @author Jeremias Maerki
 * @version $Id$
 */
public class ASCII85OutputStream extends FilterOutputStream
            implements Finalizable {

    private static final int ZERO          = 0x7A; //"z"
    private static final byte[] ZERO_ARRAY = {(byte)ZERO};
    private static final int START         = 0x21; //"!"
    private static final int EOL           = 0x0A; //"\n"
    private static final byte[] EOD        = {0x7E, 0x3E}; //"~>"

    private static final long base85_4 = 85;
    private static final long base85_3 = base85_4 * base85_4;
    private static final long base85_2 = base85_3 * base85_4;
    private static final long base85_1 = base85_2 * base85_4;

    private static final boolean DEBUG = false;

    private int pos = 0;
    private long buffer = 0;
    private int posinline = 0;
    private int bw = 0;


    public ASCII85OutputStream(OutputStream out) {
        super(out);
    }


    public void write(int b) throws IOException {
        if (pos == 0) {
            buffer += (b << 24) & 0xff000000L;
        } else if (pos == 1) {
            buffer += (b << 16) & 0xff0000L;
        } else if (pos == 2) {
            buffer += (b << 8) & 0xff00L;
        } else {
            buffer += b & 0xffL;
        }
        pos++;

        if (pos > 3) {
            checkedWrite(convertWord(buffer));
            buffer = 0;
            pos = 0;
        }
    }


    private void checkedWrite(int b) throws IOException {
        if (posinline == 80) {
            out.write(EOL); bw++;
            posinline = 0;
        }
        checkedWrite(b);
        posinline++;
        bw++;
    }


    private void checkedWrite(byte[] buf) throws IOException {
        checkedWrite(buf, buf.length, false);
    }


    private void checkedWrite(byte[] buf, boolean nosplit) throws IOException {
        checkedWrite(buf, buf.length, nosplit);
    }


    private void checkedWrite(byte[] buf , int len) throws IOException {
        checkedWrite(buf, len, false);
    }


    private void checkedWrite(byte[] buf , int len, boolean nosplit) throws IOException {
        if (posinline + len > 80) {
            int firstpart = (nosplit ? 0 : len - (posinline + len - 80));
            if (firstpart > 0) out.write(buf, 0, firstpart);
            out.write(EOL); bw++;
            int rest = len - firstpart;
            if (rest > 0) out.write(buf, firstpart, rest);
            posinline = rest;
        } else {
            out.write(buf, 0, len);
            posinline += len;
        }
        bw += len;
    }


    /**
     * This converts a 32 bit value (4 bytes) into 5 bytes using base 85.
     * each byte in the result starts with zero at the '!' character so
     * the resulting base85 number fits into printable ascii chars
     *
     * @param word the 32 bit unsigned (hence the long datatype) word
     * @return 5 bytes (or a single byte of the 'z' character for word
     * values of 0)
     */
    private byte[] convertWord(long word) {
        word = word & 0xffffffff;

        if (word == 0) {
            return ZERO_ARRAY;
        } else {
            if (word < 0) {
                word = -word;
            }
            byte c1 = (byte)((word / base85_1) & 0xFF);
            byte c2 = (byte)(((word - (c1 * base85_1)) / base85_2) & 0xFF);
            byte c3 =
                (byte)(((word - (c1 * base85_1) - (c2 * base85_2)) / base85_3)
                       & 0xFF);
            byte c4 =
                (byte)(((word - (c1 * base85_1) - (c2 * base85_2) - (c3 * base85_3)) / base85_4)
                       & 0xFF);
            byte c5 =
                (byte)(((word - (c1 * base85_1) - (c2 * base85_2) - (c3 * base85_3) - (c4 * base85_4)))
                       & 0xFF);

            byte[] ret = {
                (byte)(c1 + START), (byte)(c2 + START),
                (byte)(c3 + START), (byte)(c4 + START),
                (byte)(c5 + START)
            };

            if (DEBUG) {
                for (int i = 0; i < ret.length; i++) {
                    if (ret[i] < 33 || ret[i] > 117) {
                        System.out.println("Illegal char value "
                                        + new Integer(ret[i]));
                    }
                }
            }
            return ret;
        }
    }


    public void finalizeStream() throws IOException {
        // now take care of the trailing few bytes.
        // with n leftover bytes, we append 0 bytes to make a full group of 4
        // then convert like normal (except not applying the special zero rule)
        // and write out the first n+1 bytes from the result
        if (pos > 0) {
            int rest = pos;
            /*
            byte[] lastdata = new byte[4];
            int i = 0;
            for (int j = 0; j < 4; j++) {
                if (j < rest) {
                    lastdata[j] = data[i++];
                } else {
                    lastdata[j] = 0;
                }
            }

            long val = ((lastdata[0] << 24) & 0xff000000L)
                       + ((lastdata[1] << 16) & 0xff0000L)
                       + ((lastdata[2] << 8) & 0xff00L)
                       + (lastdata[3] & 0xffL);
            */

            byte[] conv;
            // special rule for handling zeros at the end
            if (buffer != 0) {
                conv = convertWord(buffer);
            } else {
                conv = new byte[5];
                for (int j = 0; j < 5; j++) {
                    conv[j] = (byte)'!';
                }
            }
            // assert rest+1 <= 5
            checkedWrite(conv, rest + 1);
        }
        // finally write the two character end of data marker
        checkedWrite(EOD, true);

        flush();
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        }
    }


    public void close() throws IOException {
        finalizeStream();
        super.close();
    }

}


