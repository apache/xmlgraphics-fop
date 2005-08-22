/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// FOP
import org.apache.commons.io.IOUtils;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.apps.FOUserAgent;

/**
 * ImageReader object for EPS document image type.
 *
 * @version   $Id$
 */
public class EPSReader implements ImageReader {

    private static final byte[] EPS_HEADER_ASCII = "%!PS".getBytes();
    private static final byte[] BOUNDINGBOX = "%%BoundingBox: ".getBytes();
    //private static final byte[] HIRESBOUNDINGBOX = "%%HiResBoundingBox: ".getBytes();
    //TODO Implement HiResBoundingBox, ImageInfo probably needs some changes for that

    /** @see org.apache.fop.image.analyser.ImageReader */
    public FopImage.ImageInfo verifySignature(String uri, InputStream bis,
                FOUserAgent ua) throws IOException {

        boolean isEPS = false;

        bis.mark(32);
        byte[] header = new byte[30];
        bis.read(header, 0, 30);
        bis.reset();

        EPSImage.EPSData data = new EPSImage.EPSData();

        // Check if binary header
        if (getLong(header, 0) == 0xC6D3D0C5) {
            data.isAscii = false;
            isEPS = true;

            data.psStart = getLong(header, 4);
            data.psLength = getLong(header, 8);
            data.wmfStart = getLong(header, 12);
            data.wmfLength = getLong(header, 16);
            data.tiffStart = getLong(header, 20);
            data.tiffLength = getLong(header, 24);

        } else {
            // Check if plain ascii
            byte[] epsh = "%!PS".getBytes();
            if (EPS_HEADER_ASCII[0] == header[0]
                    && EPS_HEADER_ASCII[1] == header[1]
                    && EPS_HEADER_ASCII[2] == header[2]
                    && EPS_HEADER_ASCII[3] == header[3]) {
                data.isAscii = true;
                isEPS = true;
            }
        }

        if (isEPS) {
            FopImage.ImageInfo info = new FopImage.ImageInfo();
            info.originalURI = uri;
            info.mimeType = getMimeType();
            info.data = data;
            readEPSImage(bis, data);
            data.bbox = readBBox(data);

            if (data.bbox != null) {
                info.width = (int) (data.bbox[2] - data.bbox[0]);
                info.height = (int) (data.bbox[3] - data.bbox[1]);

                // image data read
                IOUtils.closeQuietly(bis);
                info.inputStream = null;

                return info;
            } else {
                // Ain't eps if no BoundingBox
                isEPS = false;
            }
        }

        return null;
    }

    /**
     * Returns the MIME type supported by this implementation.
     *
     * @return   The MIME type
     */
    public String getMimeType() {
        return "image/eps";
    }

    private long getLong(byte[] buf, int idx) {
        int b1 = buf[idx] & 0xff;
        int b2 = buf[idx + 1] & 0xff;
        int b3 = buf[idx + 2] & 0xff;
        int b4 = buf[idx + 3] & 0xff;

        return (long) ((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
    }

    /**
     * Read the eps file and extract eps part.
     *
     * @param bis              The InputStream
     * @param data             EPSData object to write the results to
     * @exception IOException  If an I/O error occurs
     */
    private void readEPSImage(InputStream bis, EPSImage.EPSData data)
                throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] file;
        byte[] readBuf = new byte[20480];
        int bytesRead;
        int index = 0;
        boolean cont = true;

        try {
            while ((bytesRead = bis.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytesRead);
            }
        } catch (java.io.IOException ex) {
            throw new IOException("Error while loading EPS image: "
                    + ex.getMessage());
        }

        file = baos.toByteArray();

        if (data.isAscii) {
            data.rawEps = null;
            data.epsFile = new byte[file.length];
            System.arraycopy(file, 0, data.epsFile, 0, data.epsFile.length);
        } else {
            data.rawEps = new byte[file.length];
            data.epsFile = new byte[(int) data.psLength];
            System.arraycopy(file, 0, data.rawEps, 0, data.rawEps.length);
            System.arraycopy(data.rawEps, (int) data.psStart, data.epsFile, 0,
                    (int) data.psLength);
        }
    }

    /**
     * Get embedded TIFF preview or null.
     *
     * @param data  The EPS payload
     * @return      The embedded preview
     */
    public byte[] getPreview(EPSImage.EPSData data) {
        if (data.preview == null) {
            if (data.tiffLength > 0) {
                data.preview = new byte[(int) data.tiffLength];
                System.arraycopy(data.rawEps, (int) data.tiffStart, data.preview, 0,
                        (int) data.tiffLength);
            }
        }
        return data.preview;
    }

    /**
     * Extract bounding box from eps part.
     *
     * @param data  The EPS payload
     * @return      An Array of four coordinates making up the bounding box
     */
    private long[] readBBox(EPSImage.EPSData data) {
        long[] mbbox = null;
        int idx = 0;
        boolean found = false;

        while (!found && (data.epsFile.length > (idx + BOUNDINGBOX.length))) {
            boolean sfound = true;
            int i = idx;
            for (i = idx; sfound && (i - idx) < BOUNDINGBOX.length; i++) {
                if (BOUNDINGBOX[i - idx] != data.epsFile[i]) {
                    sfound = false;
                }
            }
            if (sfound) {
                found = true;
                idx = i;
            } else {
                idx++;
            }
        }

        if (!found) {
            return mbbox;
        }

        mbbox = new long[4];
        idx += readLongString(data, mbbox, 0, idx);
        idx += readLongString(data, mbbox, 1, idx);
        idx += readLongString(data, mbbox, 2, idx);
        idx += readLongString(data, mbbox, 3, idx);

        return mbbox;
    }

    private int readLongString(EPSImage.EPSData data, long[] mbbox, int i, int idx) {
        while (idx < data.epsFile.length && (data.epsFile[idx] == 32)) {
            idx++;
        }

        int nidx = idx;

        // check also for ANSI46(".") to identify floating point values
        while (nidx < data.epsFile.length
                && ((data.epsFile[nidx] >= 48 && data.epsFile[nidx] <= 57)
                || (data.epsFile[nidx] == 45)
                || (data.epsFile[nidx] == 46))) {
            nidx++;
        }

        byte[] num = new byte[nidx - idx];
        System.arraycopy(data.epsFile, idx, num, 0, nidx - idx);
        String ns = new String(num);

        //if( ns.indexOf(".") != -1 ) {
        // do something like logging a warning
        //}

        // then parse the double and round off to the next math. Integer
        mbbox[i] = (long) Math.ceil(Double.parseDouble(ns));

        return (1 + nidx - idx);
    }

}

