/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.*;

public class ASCIIHexEncodeFilter implements Filter {

    private static final char ASCIIHEX_EOL = '\n';
    private static final String ASCIIHEX_EOD = ">";
    private static final String ENCODING = "US-ASCII";

    protected ASCIIHexEncodeFilter() {}

    public long write(OutputStream out, byte[] buf, int len,
                      long bw) throws IOException {
        boolean last = false;
        int pos = 0;
        int rest = len;
        while (rest > 0) {
            int restofline = 80 - (int)((bw + pos) % 80);
            if (rest < restofline) {
                // last line
                restofline = rest;
                last = true;
            }
            if (restofline > 0) {
                out.write(buf, pos, restofline);
                pos += restofline;
                if (!last)
                    out.write(ASCIIHEX_EOL);
            }
            rest = len - pos;
        }
        return bw + len;
    }

    public void doFilter(InputStream in,
                         OutputStream out) throws IOException {
        long bw = 0;
        byte[] buf = new byte[2048];
        int bytes_read;
        StringBuffer sb = new StringBuffer(2048 * 2);
        while ((bytes_read = in.read(buf)) != -1) {
            sb.setLength(0);
            for (int i = 0; i < bytes_read; i++) {
                int val = (int)(buf[i] & 0xFF);
                if (val < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(val));
            }
            bw = write(out, sb.toString().getBytes(ENCODING), bytes_read * 2,
                       bw);
        }
        byte[] eod = ASCIIHEX_EOD.getBytes(ENCODING);
        bw = write(out, eod, eod.length, bw);
    }

    public static InputStream filter(InputStream in) throws IOException {
        ASCIIHexEncodeFilter myfilter = new ASCIIHexEncodeFilter();
        return FilterThread.filter(in, myfilter);
    }

}
