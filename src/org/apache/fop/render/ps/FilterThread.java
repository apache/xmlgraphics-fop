/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.*;

public class FilterThread extends Thread {

    private Filter filter;
    private InputStream in;
    private OutputStream out;

    private FilterThread(Filter filter, InputStream in, OutputStream out) {
        this.filter = filter;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            try {
                this.filter.doFilter(in, out);
                this.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            this.filter = null;
            this.in = null;
            this.out = null;
        }
    }

    public static InputStream filter(InputStream in,
                                     Filter filter) throws IOException {
        PipedInputStream pin = new PipedInputStream();
        PipedOutputStream pout = new PipedOutputStream(pin);
        FilterThread thread = new FilterThread(filter, in, pout);
        thread.start();
        return pin;
    }

}
