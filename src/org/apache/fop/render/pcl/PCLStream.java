/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pcl;

import java.io.*;

public class PCLStream {
    OutputStream out = null;
    boolean doOutput = true;

    public PCLStream(OutputStream os) {
        out = os;
    }

    public void add(String str) {
        if (!doOutput)
            return;

        byte buff[] = new byte[str.length()];
        int countr;
        int len = str.length();
        for (countr = 0; countr < len; countr++)
            buff[countr] = (byte)str.charAt(countr);
        try {
            out.write(buff);
        } catch (IOException e) {
            // e.printStackTrace();
            // e.printStackTrace(System.out);
            throw new RuntimeException(e.toString());
        }
    }

    public void setDoOutput(boolean doout) {
        doOutput = doout;
    }

}
