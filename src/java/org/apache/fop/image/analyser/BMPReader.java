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
 * ImageReader object for BMP image type.
 *
 * @author    Pankaj Narula
 * @version   $Id: BMPReader.java,v 1.8 2003/03/06 21:25:45 jeremias Exp $
 */
public class BMPReader implements ImageReader {

    /** Length of the BMP header */
    protected static final int BMP_SIG_LENGTH = 26;

    /** @see org.apache.fop.image.analyser.ImageReader */
    public FopImage.ImageInfo verifySignature(String uri, InputStream bis,
                FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(bis);
        boolean supported = ((header[0] == (byte) 0x42)
                && (header[1] == (byte) 0x4d));
        if (supported) {
            FopImage.ImageInfo info = getDimension(header);
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
        return "image/bmp";
    }

    private FopImage.ImageInfo getDimension(byte[] header) {
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        info.mimeType = getMimeType();

        // little endian notation
        int byte1 = header[18] & 0xff;
        int byte2 = header[19] & 0xff;
        int byte3 = header[20] & 0xff;
        int byte4 = header[21] & 0xff;
        long l = (long) ((byte4 << 24) | (byte3 << 16)
                | (byte2 << 8) | byte1);
        info.width = (int) (l & 0xffffffff);

        byte1 = header[22] & 0xff;
        byte2 = header[23] & 0xff;
        byte3 = header[24] & 0xff;
        byte4 = header[25] & 0xff;
        l = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        info.height = (int) (l & 0xffffffff);
        return info;
    }

    private byte[] getDefaultHeader(InputStream imageStream)
                throws IOException {
        byte[] header = new byte[BMP_SIG_LENGTH];
        try {
            imageStream.mark(BMP_SIG_LENGTH + 1);
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
