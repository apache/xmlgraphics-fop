/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.*;
import java.util.zip.DeflaterOutputStream;

public class FlateEncodeFilter implements Filter {

    protected FlateEncodeFilter() {}

    private long copyStream(InputStream in, OutputStream out,
                            int bufferSize) throws IOException {
        long bytes_total = 0;
        byte[] buf = new byte[bufferSize];
        int bytes_read;
        while ((bytes_read = in.read(buf)) != -1) {
            bytes_total += bytes_read;
            out.write(buf, 0, bytes_read);
        }
        return bytes_total;
    }

    public void doFilter(InputStream in,
                         OutputStream out) throws IOException {
        DeflaterOutputStream dout = new DeflaterOutputStream(out);
        copyStream(in, dout, 2048);
        // dout.flush();
        dout.close();
    }

    public static InputStream filter(InputStream in) throws IOException {
        FlateEncodeFilter myfilter = new FlateEncodeFilter();
        return FilterThread.filter(in, myfilter);
    }

}
