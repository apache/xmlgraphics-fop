/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.txt;

import java.io.*;

public class TXTStream {
    OutputStream out = null;
    boolean doOutput = true;
    private String encoding;

    public TXTStream(OutputStream os) {
        out = os;
    }

    public void add(String str) {
        if (!doOutput)
            return;

        try {
            byte buff[] = str.getBytes(encoding);
            out.write(buff);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public void setDoOutput(boolean doout) {
        doOutput = doout;
    }

    public void setEncoding(String encoding) {
        if (encoding != null)
            this.encoding = encoding;
        else
            this.encoding = "UTF-8";
    }
}
