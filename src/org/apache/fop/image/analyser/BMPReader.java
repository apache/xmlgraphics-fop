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
 * ImageReader object for BMP image type.
 * @author Pankaj Narula
 * @version 1.0
 */
public class BMPReader implements ImageReader {
    static protected final int BMP_SIG_LENGTH = 26;

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(fis);
        boolean supported = ((header[0] == (byte) 0x42) &&
                             (header[1] == (byte) 0x4d));
        if (supported) {
            return getDimension(header);
        } else {
            return null;
        }
    }

    public String getMimeType() {
        return "image/bmp";
    }

    protected FopImage.ImageInfo getDimension(byte[] header) {
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        info.mimeType = getMimeType();

        // little endian notation
        int byte1 = header[18] & 0xff;
        int byte2 = header[19] & 0xff;
        int byte3 = header[20] & 0xff;
        int byte4 = header[21] & 0xff;
        long l = (long)((byte4 << 24) | (byte3 << 16) |
                        (byte2 << 8) | byte1);
        info.width = (int)(l & 0xffffffff);

        byte1 = header[22] & 0xff;
        byte2 = header[23] & 0xff;
        byte3 = header[24] & 0xff;
        byte4 = header[25] & 0xff;
        l = (long)((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        info.height = (int)(l & 0xffffffff);
        return info;
    }

    protected byte[] getDefaultHeader(BufferedInputStream imageStream) throws IOException {
        byte[] header = new byte[BMP_SIG_LENGTH];
        try {
            imageStream.mark(BMP_SIG_LENGTH + 1);
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

