/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * ImageReader object for EMF image type.
 *
 * @author    Peter Herweg
 */
public class EMFReader implements ImageReader {

    /** Length of the EMF header */
    protected static final int EMF_SIG_LENGTH = 88;
    
    /** offset to signature */
    private static final int SIGNATURE_OFFSET = 40;
    /** offset to width */
    private static final int WIDTH_OFFSET = 32;
    /** offset to height */
    private static final int HEIGHT_OFFSET = 36;
    /** offset to horizontal resolution in pixel */
    private static final int HRES_PIXEL_OFFSET = 72;
    /** offset to vertical resolution in pixel */
    private static final int VRES_PIXEL_OFFSET = 76;
    /** offset to horizontal resolution in mm */
    private static final int HRES_MM_OFFSET = 80;
    /** offset to vertical resolution in mm */
    private static final int VRES_MM_OFFSET = 84;

    /** {@inheritDoc} */
    public FopImage.ImageInfo verifySignature(String uri, InputStream bis,
                FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(bis);
        boolean supported 
                = ( (header[SIGNATURE_OFFSET + 0] == (byte) 0x20)
                && (header[SIGNATURE_OFFSET + 1] == (byte) 0x45)
                && (header[SIGNATURE_OFFSET + 2] == (byte) 0x4D)
                && (header[SIGNATURE_OFFSET + 3] == (byte) 0x46) );
        
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
        return "image/emf";
    }

    private FopImage.ImageInfo getDimension(byte[] header) {
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        long value = 0;
        int byte1;
        int byte2;
        int byte3;
        int byte4;
        
        // little endian notation

        //resolution        
        byte1 = header[HRES_MM_OFFSET] & 0xff;
        byte2 = header[HRES_MM_OFFSET + 1] & 0xff;
        byte3 = header[HRES_MM_OFFSET + 2] & 0xff;
        byte4 = header[HRES_MM_OFFSET + 3] & 0xff;
        long hresMM = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        
        byte1 = header[VRES_MM_OFFSET] & 0xff;
        byte2 = header[VRES_MM_OFFSET + 1] & 0xff;
        byte3 = header[VRES_MM_OFFSET + 2] & 0xff;
        byte4 = header[VRES_MM_OFFSET + 3] & 0xff;
        long vresMM = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        
        byte1 = header[HRES_PIXEL_OFFSET] & 0xff;
        byte2 = header[HRES_PIXEL_OFFSET + 1] & 0xff;
        byte3 = header[HRES_PIXEL_OFFSET + 2] & 0xff;
        byte4 = header[HRES_PIXEL_OFFSET + 3] & 0xff;
        long hresPixel = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        
        byte1 = header[VRES_PIXEL_OFFSET] & 0xff;
        byte2 = header[VRES_PIXEL_OFFSET + 1] & 0xff;
        byte3 = header[VRES_PIXEL_OFFSET + 2] & 0xff;
        byte4 = header[VRES_PIXEL_OFFSET + 3] & 0xff;
        long vresPixel = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        
        info.dpiHorizontal = hresPixel / (hresMM / 25.4f);
        info.dpiVertical   = vresPixel / (vresMM / 25.4f);
        
        //width
        byte1 = header[WIDTH_OFFSET] & 0xff;
        byte2 = header[WIDTH_OFFSET + 1] & 0xff;
        byte3 = header[WIDTH_OFFSET + 2] & 0xff;
        byte4 = header[WIDTH_OFFSET + 3] & 0xff;
        value = (long) ((byte4 << 24) | (byte3 << 16)
                | (byte2 << 8) | byte1);
        value = Math.round(value / 100f / 25.4f * info.dpiHorizontal);
        info.width = (int) (value & 0xffffffff);

        //height
        byte1 = header[HEIGHT_OFFSET] & 0xff;
        byte2 = header[HEIGHT_OFFSET + 1] & 0xff;
        byte3 = header[HEIGHT_OFFSET + 2] & 0xff;
        byte4 = header[HEIGHT_OFFSET + 3] & 0xff;
        value = (long) ((byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1);
        value = Math.round(value / 100f / 25.4f * info.dpiVertical);
        info.height = (int) (value & 0xffffffff);

        return info;
    }

    private byte[] getDefaultHeader(InputStream imageStream)
                throws IOException {
        byte[] header = new byte[EMF_SIG_LENGTH];
        try {
            imageStream.mark(EMF_SIG_LENGTH + 1);
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
