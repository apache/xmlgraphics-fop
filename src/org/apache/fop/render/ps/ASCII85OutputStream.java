/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class applies a ASCII85 encoding to the stream.
 *
 * @author <a href="mailto:jeremias.maerki@outline.ch">Jeremias Maerki</a>
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
        checkedWrite(buf, buf.length);
    }


    private void checkedWrite(byte[] buf , int len) throws IOException {
        if (posinline + len > 80) {
            int firstpart = len - (posinline + len - 80);
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
        checkedWrite(EOD);

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


