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
 * ImageReader object for TIFF image type.
 * @author Pankaj Narula, Michael Lee
 * @version 1.0
 */
public class TIFFReader implements ImageReader {
    static protected final int TIFF_SIG_LENGTH = 8;

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(fis);
        boolean supported = false;

        if (header[0] == (byte) 0x49 && header[1] == (byte) 0x49)// first 2 bytes = II (little endian encoding)
        {
            // look for '42' in byte 3 and '0' in byte 4
            if (header[2] == 42 && header[3] == 0)
                supported = true;
        }

        if (header[0] == (byte) 0x4D && header[1] == (byte) 0x4D)// first 2 bytes == MM (big endian encoding)
        {
            // look for '42' in byte 4 and '0' in byte 3
            if (header[2] == 0 && header[3] == 42)
                supported = true;
        }

        if (supported) {
            FopImage.ImageInfo info = getDimension(header);
            info.mimeType = getMimeType();
            return info;
        } else {
            return null;
        }
    }

    public String getMimeType() {
        return "image/tiff";
    }

    protected FopImage.ImageInfo getDimension(byte[] header) {
        // currently not setting the width and height
        // these are set again by the Jimi image reader.
        // I suppose I'll do it one day to be complete.  Or
        // someone else will.
        // Note: bytes 4,5,6,7 contain the byte offset in the stream of the first IFD block
        /*
         * //png is always big endian
         * int byte1 = header[ 16 ] & 0xff;
         * int byte2 = header[ 17 ] & 0xff;
         * int byte3 = header[ 18 ] & 0xff;
         * int byte4 = header[ 19 ] & 0xff;
         * long l = ( long ) ( ( byte1 << 24 ) | ( byte2 << 16 ) |
         * ( byte3 << 8 ) | byte4 );
         * this.width = ( int ) ( l );
         * byte1 = header[ 20 ] & 0xff;
         * byte2 = header[ 21 ] & 0xff;
         * byte3 = header[ 22 ] & 0xff;
         * byte4 = header[ 23 ] & 0xff;
         * l = ( long ) ( ( byte1 << 24 ) | ( byte2 << 16 ) | ( byte3 << 8 ) |
         * byte4 );
         * this.height = ( int ) ( l );
         */
         return null;
    }

    protected byte[] getDefaultHeader(BufferedInputStream imageStream) throws IOException {
        byte[] header = new byte[TIFF_SIG_LENGTH];
        try {
            imageStream.mark(TIFF_SIG_LENGTH + 1);
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

