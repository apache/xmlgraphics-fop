/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.IOException;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOUserAgent;

/**
 * ImageReader object for TIFF image type.
 *
 * @author    Pankaj Narula, Michael Lee
 * @version   $Id$
 */
public class TIFFReader implements ImageReader {

    private static final int TIFF_SIG_LENGTH = 8;

    /** @see org.apache.fop.image.analyser.ImageReader */
    public FopImage.ImageInfo verifySignature(String uri, InputStream bis,
                FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(bis);
        boolean supported = false;

        // first 2 bytes = II (little endian encoding)
        if (header[0] == (byte) 0x49 && header[1] == (byte) 0x49) {

            // look for '42' in byte 3 and '0' in byte 4
            if (header[2] == 42 && header[3] == 0) {
                supported = true;
            }
        }

        // first 2 bytes == MM (big endian encoding)
        if (header[0] == (byte) 0x4D && header[1] == (byte) 0x4D) {

            // look for '42' in byte 4 and '0' in byte 3
            if (header[2] == 0 && header[3] == 42) {
                supported = true;
            }
        }

        if (supported) {
            FopImage.ImageInfo info = getDimension(header);
            info.originalURI = uri;
            info.mimeType = getMimeType();
            info.inputStream = bis;
            return info;
        } else {
            return null;
        }
    }

    /**
     * Returns the MIME type supported by this implementation.
     *
     * @return   The MIME type
     */
    public String getMimeType() {
        return "image/tiff";
    }

    private FopImage.ImageInfo getDimension(byte[] header) {
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
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        info.width = -1;
        info.height = -1;
        return info;
    }

    private byte[] getDefaultHeader(InputStream imageStream)
        throws IOException {
        byte[] header = new byte[TIFF_SIG_LENGTH];
        try {
            imageStream.mark(TIFF_SIG_LENGTH + 1);
            imageStream.read(header);
            imageStream.reset();
        } catch (IOException ex) {
            try {
                imageStream.reset();
            } catch (IOException exbis) {
                // throw the original exception, not this one
            }
            throw ex;
        }
        return header;
    }

}

