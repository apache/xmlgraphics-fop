/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * ImageReader object for GIF image type.
 * @author Pankaj Narula
 * @version 1.0
 */
public class GIFReader extends AbstractImageReader {
    static protected final int GIF_SIG_LENGTH = 10;
    protected byte[] header;

    public boolean verifySignature(String uri, BufferedInputStream fis)
            throws IOException {
        this.imageStream = fis;
        this.setDefaultHeader();
        boolean supported = ((header[0] == 'G') && (header[1] == 'I')
                             && (header[2] == 'F') && (header[3] == '8')
                             && (header[4] == '7' || header[4] == '9')
                             && (header[5] == 'a'));
        if (supported) {
            setDimension();
            return true;
        } else
            return false;
    }

    public String getMimeType() {
        return "image/gif";
    }

    protected void setDimension() {
        // little endian notation
        int byte1 = header[6] & 0xff;
        int byte2 = header[7] & 0xff;
        this.width = ((byte2 << 8) | byte1) & 0xffff;

        byte1 = header[8] & 0xff;
        byte2 = header[9] & 0xff;
        this.height = ((byte2 << 8) | byte1) & 0xffff;
    }

    protected void setDefaultHeader() throws IOException {
        this.header = new byte[GIF_SIG_LENGTH];
        try {
            this.imageStream.mark(GIF_SIG_LENGTH + 1);
            this.imageStream.read(header);
            this.imageStream.reset();
        } catch (IOException ex) {
            try {
                this.imageStream.reset();
            } catch (IOException exbis) {}
            throw ex;
        }
    }

}

