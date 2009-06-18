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

import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;

/**
 * ImageReader object for PNG image type.
 * @author Pankaj Narula
 * @version 1.0
 */
public class PNGReader implements ImageReader {
    static protected final int PNG_SIG_LENGTH = 24;

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(fis);
        boolean supported = ((header[0] == (byte) 0x89) &&
                             (header[1] == (byte) 0x50) && (header[2] == (byte) 0x4e) &&
                             (header[3] == (byte) 0x47) && (header[4] == (byte) 0x0d) &&
                             (header[5] == (byte) 0x0a) &&
                             (header[6] == (byte) 0x1a) && (header[7] == (byte) 0x0a));
        if (supported) {
            FopImage.ImageInfo info = getDimension(header);
            info.mimeType = getMimeType();
            return info;
        } else {
            return null;
        }
    }

    public String getMimeType() {
        return "image/png";
    }

    protected FopImage.ImageInfo getDimension(byte[] header) {
        FopImage.ImageInfo info = new FopImage.ImageInfo();

        // png is always big endian
        int byte1 = header[16] & 0xff;
        int byte2 = header[17] & 0xff;
        int byte3 = header[18] & 0xff;
        int byte4 = header[19] & 0xff;
        long l = (long)((byte1 << 24) | (byte2 << 16) |
                        (byte3 << 8) | byte4);
        info.width = (int)(l);

        byte1 = header[20] & 0xff;
        byte2 = header[21] & 0xff;
        byte3 = header[22] & 0xff;
        byte4 = header[23] & 0xff;
        l = (long)((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4);
        info.height = (int)(l);
        return info;
    }

    protected byte[] getDefaultHeader(BufferedInputStream imageStream) throws IOException {
        byte[] header = new byte[PNG_SIG_LENGTH];
        try {
            imageStream.mark(PNG_SIG_LENGTH + 1);
            imageStream.read(header);
            imageStream.reset();
        } catch (IOException ex) {
            try {
                imageStream.reset();
            } catch (IOException exbis) {}
            throw ex;
        }
        return header;
    }

}

