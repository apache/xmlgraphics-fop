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
 * ImageReader object for PNG image type.
 * @author Pankaj Narula
 * @version 1.0
 */
public class PNGReader extends AbstractImageReader {
    static protected final int PNG_SIG_LENGTH = 24;
    protected byte[] header;

    public boolean verifySignature(String uri, BufferedInputStream fis)
            throws IOException {
        this.imageStream = fis;
        this.setDefaultHeader();
        boolean supported = ((header[0] == (byte)0x89)
                             && (header[1] == (byte)0x50)
                             && (header[2] == (byte)0x4e)
                             && (header[3] == (byte)0x47)
                             && (header[4] == (byte)0x0d)
                             && (header[5] == (byte)0x0a)
                             && (header[6] == (byte)0x1a)
                             && (header[7] == (byte)0x0a));
        if (supported) {
            setDimension();
            return true;
        } else
            return false;
    }

    public String getMimeType() {
        return "image/png";
    }

    protected void setDimension() {
        // png is always big endian
        int byte1 = header[16] & 0xff;
        int byte2 = header[17] & 0xff;
        int byte3 = header[18] & 0xff;
        int byte4 = header[19] & 0xff;
        long l = (long)((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4);
        this.width = (int)(l);

        byte1 = header[20] & 0xff;
        byte2 = header[21] & 0xff;
        byte3 = header[22] & 0xff;
        byte4 = header[23] & 0xff;
        l = (long)((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4);
        this.height = (int)(l);

    }

    protected void setDefaultHeader() throws IOException {
        this.header = new byte[PNG_SIG_LENGTH];
        try {
            this.imageStream.mark(PNG_SIG_LENGTH + 1);
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

