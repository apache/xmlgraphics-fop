/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * PSStream is used to to output PostScript code from the PostScript renderer.
 */
public class PSStream extends FilterOutputStream {

    /** @see java.io.FilterOutputStream **/
    public PSStream(OutputStream out) {
        super(out);
    }


    /**
     * Writes a PostScript command to the stream.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void write(String cmd) throws IOException {
        if (cmd.length() > 255) {
            throw new RuntimeException("PostScript command exceeded limit of 255 characters");
        }
        write(cmd.getBytes("US-ASCII"));
        write('\n');
    }


    /**
     * Writes encoded data to the PostScript stream.
     *
     * @param cmd              The encoded PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void writeByteArr(byte[] cmd) throws IOException {
        write(cmd);
        write('\n');
    }

}
