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
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class ASCII85Filter extends PDFFilter {
    private static final char ASCII85_ZERO = 'z';
    private static final char ASCII85_START = '!';
    private static final String ASCII85_EOD = "~>";

    private static final long base85_4 = 85;
//      private static final long base85_3 = base85_4 * base85_4;
//      private static final long base85_2 = base85_3 * base85_4;
//      private static final long base85_1 = base85_2 * base85_4;



    public String getName() {
        return "/ASCII85Decode";
    }

    public String getDecodeParms() {
        return null;
    }

    public byte[] encode(byte[] data) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int i;
        int total = 0;
        int diff = 0;

        // first encode the majority of the data
        // each 4 byte group becomes a 5 byte group
        for (i = 0; i + 3 < data.length; i += 4) {

            long val = ((data[i] << 24)
                        & 0xff000000L)          // note: must have the L at the
            + ((data[i + 1] << 16) & 0xff0000L)    // end, otherwise you get into
            + ((data[i + 2] << 8) & 0xff00L)    // weird signed value problems
            + (data[i + 3] & 0xffL);            // cause we're using a full 32 bits
            byte[] conv = convertWord(val);

            buffer.write(conv, 0, conv.length);

        }

        // now take care of the trailing few bytes.
        // with n leftover bytes, we append 0 bytes to make a full group of 4
        // then convert like normal (except not applying the special zero rule)
        // and write out the first n+1 bytes from the result
        if (i < data.length) {
            int n = data.length - i;
            byte[] lastdata = new byte[4];
            for (int j = 0; j < 4; j++) {
                if (j < n) {
                    lastdata[j] = data[i++];
                } else {
                    lastdata[j] = 0;
                }
            }

            long val = ((lastdata[0] << 24) & 0xff000000L)
                       + ((lastdata[1] << 16) & 0xff0000L)
                       + ((lastdata[2] << 8) & 0xff00L)
                       + (lastdata[3] & 0xffL);
            byte[] conv = convertWord(val);

            // special rule for handling zeros at the end
            if (val == 0) {
                conv = new byte[5];
                for (int j = 0; j < 5; j++) {
                    conv[j] = (byte)'!';
                }
            }
            // assert n+1 <= 5
            buffer.write(conv, 0, n + 1);
            // System.out.println("ASCII85 end of data was "+n+" bytes long");

        }
        // finally write the two character end of data marker
        byte[] eod;
        try {
            eod = ASCII85_EOD.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            eod = ASCII85_EOD.getBytes();
        }       
        buffer.write(eod, 0, eod.length);
        byte[] result = buffer.toByteArray();

        // assert that we have the correct outgoing length
        /*
         * int in = (data.length % 4);
         * int out = (result.length-ASCII85_EOD.getBytes().length) % 5;
         * if ((in+1 != out) && !(in == 0 && out == 0)) {
         * System.out.println("ASCII85 assertion failed:");
         * System.out.println("        inlength = "+data.length+" inlength % 4 = "+(data.length % 4)+" outlength = "+(result.length-ASCII85_EOD.getBytes().length)+" outlength % 5 = "+((result.length-ASCII85_EOD.getBytes().length) % 5));
         * }
         */
        return result;

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
        if (word < 0) {
            word = -word;
        }

        if (word == 0) {
            byte[] result = {
                (byte)ASCII85_ZERO
            };
            return result;
        } else {
//              byte c1 = (byte)((word / base85_1) & 0xFF);
//              byte c2 = (byte)(((word - (c1 * base85_1)) / base85_2) & 0xFF);
//              byte c3 =
//                  (byte)(((word - (c1 * base85_1) - (c2 * base85_2)) / base85_3)
//                         & 0xFF);
//              byte c4 =
//                  (byte)(((word - (c1 * base85_1) - (c2 * base85_2) - (c3 * base85_3)) / base85_4)
//                         & 0xFF);
//              byte c5 =
//                  (byte)(((word - (c1 * base85_1) - (c2 * base85_2) - (c3 * base85_3) - (c4 * base85_4)))
//                         & 0xFF);

//              byte[] ret = {
//                  (byte)(c1 + ASCII85_START), (byte)(c2 + ASCII85_START),
//                  (byte)(c3 + ASCII85_START), (byte)(c4 + ASCII85_START),
//                  (byte)(c5 + ASCII85_START)
            byte c5 = (byte)((word % base85_4) + ASCII85_START);
            word = word / base85_4;
            byte c4 = (byte)((word % base85_4) + ASCII85_START);
            word = word / base85_4;
            byte c3 = (byte)((word % base85_4) + ASCII85_START);
            word = word / base85_4;
            byte c2 = (byte)((word % base85_4) + ASCII85_START);
            word = word / base85_4;
            byte c1 = (byte)((word % base85_4) + ASCII85_START);

            byte[] ret = {
              c1 , c2, c3, c4, c5
            };
            for (int i = 0; i < ret.length; i++) {
                if (ret[i] < 33 || ret[i] > 117) {
                    System.out.println("illegal char value "
                                       + new Integer(ret[i]));
                }
            }

            return ret;


        }
    }

}
