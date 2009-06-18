/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.*;

public class ASCII85EncodeFilter implements Filter {

    private static final char ASCII85_ZERO = 'z';
    private static final char ASCII85_START = '!';
    private static final char ASCII85_EOL = '\n';
    private static final String ASCII85_EOD = "~>";
    private static final String ENCODING = "US-ASCII";

    private static final long base85_4 = 85;
    private static final long base85_3 = base85_4 * base85_4;
    private static final long base85_2 = base85_3 * base85_4;
    private static final long base85_1 = base85_2 * base85_4;

    protected ASCII85EncodeFilter() {}

    public long write(OutputStream out, byte[] buf, int len,
                      long bw) throws IOException {
        // Assumption: len<80
        int line = (int)(bw % 80) + len;
        if (line >= 80) {
            int first = len - (line - 80);
            out.write(buf, 0, first);
            out.write(ASCII85_EOL);
            out.write(buf, first, len - first);
        } else {
            out.write(buf, 0, len);
        }
        return bw + len;
    }

    public void doFilter(InputStream in,
                         OutputStream out) throws IOException {
        int total = 0;
        int diff = 0;
        long bw = 0;

        // first encode the majority of the data
        // each 4 byte group becomes a 5 byte group
        byte[] data = new byte[4];
        int bytes_read;
        while ((bytes_read = in.read(data)) == data.length) {
            long val = ((data[0] << 24)
                        & 0xff000000L)         // note: must have the L at the
            + ((data[1] << 16) & 0xff0000L)    // end, otherwise you get into
            + ((data[2] << 8) & 0xff00L)       // weird signed value problems
            + (data[3] & 0xffL);               // cause we're using a full 32 bits
            byte[] conv = convertWord(val);

            bw = write(out, conv, conv.length, bw);
        }

        // now take care of the trailing few bytes.
        // with n leftover bytes, we append 0 bytes to make a full group of 4
        // then convert like normal (except not applying the special zero rule)
        // and write out the first n+1 bytes from the result
        if ((bytes_read < data.length) && (bytes_read >= 0)) {
            int n = data.length - bytes_read;
            byte[] lastdata = new byte[4];
            int i = 0;
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

            byte[] conv;
            // special rule for handling zeros at the end
            if (val != 0) {
                conv = convertWord(val);
            } else {
                conv = new byte[5];
                for (int j = 0; j < 5; j++) {
                    conv[j] = (byte)'!';
                }
            }
            // assert n+1 <= 5
            bw = write(out, conv, n + 1, bw);
            // System.out.println("ASCII85 end of data was "+n+" bytes long");

        }
        // finally write the two character end of data marker
        byte[] EOD = ASCII85_EOD.getBytes();
        bw = write(out, EOD, EOD.length, bw);
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
                (byte)(c1 + ASCII85_START), (byte)(c2 + ASCII85_START),
                (byte)(c3 + ASCII85_START), (byte)(c4 + ASCII85_START),
                (byte)(c5 + ASCII85_START)
            };

            for (int i = 0; i < ret.length; i++) {
                if (ret[i] < 33 || ret[i] > 117) {
                    System.out.println("Illegal char value "
                                       + new Integer(ret[i]));
                }
            }
            return ret;
        }
    }


    public static InputStream filter(InputStream in) throws IOException {
        ASCII85EncodeFilter myfilter = new ASCII85EncodeFilter();
        return FilterThread.filter(in, myfilter);
    }

}
