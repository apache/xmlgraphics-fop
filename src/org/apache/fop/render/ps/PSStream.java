/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

import java.io.*;

public class PSStream extends FilterOutputStream {

    public PSStream(OutputStream out) {
        super(out);
    }
    
    public void write(String cmd) throws IOException {
        if (cmd.length() > 255)
            throw new RuntimeException("PostScript command exceeded limit of 255 characters");
        write(cmd.getBytes("US-ASCII"));
        write('\n');
    }
    
    public void writeByteArr(byte[] cmd) throws IOException {
        write(cmd);
        write('\n');
    }
}
