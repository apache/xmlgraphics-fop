/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.image.FopImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.fo.FOUserAgent;

/**
 * ImageReader object for EPS document image type.
 */
public class EPSReader implements ImageReader {

    private long getLong(byte[] buf, int idx) {
        int b1 = buf[idx] & 0xff;
        int b2 = buf[idx + 1] & 0xff;
        int b3 = buf[idx + 2] & 0xff;
        int b4 = buf[idx + 3] & 0xff;

        return (long)((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
    }

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        boolean isEPS = false;
        fis.mark(32);
        byte[] header = new byte[30];
        fis.read(header, 0, 30);
        fis.reset();

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
            if (epsh[0] == header[0] && epsh[1] == header[1] &&
                    epsh[2] == header[2] && epsh[3] == header[3]) {
                data.isAscii = true;
                isEPS = true;
            }
        }

        if (isEPS) {
            FopImage.ImageInfo info = new FopImage.ImageInfo();
            info.mimeType = getMimeType();
            info.data = data;
            readEPSImage(fis, data);
            data.bbox = readBBox(data);

            if (data.bbox != null) {
                info.width = (int)(data.bbox[2] - data.bbox[0]);
                info.height = (int)(data.bbox[3] - data.bbox[1]);
                return info;
            } else {
                // Ain't eps if no BoundingBox
                isEPS = false;
            }
        }

        return null;
    }

    /** read the eps file and extract eps part */
    private void readEPSImage(BufferedInputStream fis, EPSImage.EPSData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] file;
        byte[] readBuf = new byte[20480];
        int bytes_read;
        int index = 0;
        boolean cont = true;


        try {
            while ((bytes_read = fis.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytes_read);
            }
        } catch (java.io.IOException ex) {
            throw new IOException("Error while loading EPS image " +
                                  ex.getMessage());
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

    /* Get embedded preview or null */
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

    /** Extract bounding box from eps part
     */
    private long[] readBBox(EPSImage.EPSData data) {
        long[] mbbox = null;
        int idx = 0;
        byte[] bbxName = "%%BoundingBox: ".getBytes();
        boolean found = false;

        while (!found && (data.epsFile.length > (idx + bbxName.length))) {
            boolean sfound = true;
            int i = idx;
            for (i = idx; sfound && (i - idx) < bbxName.length; i++) {
                if (bbxName[i - idx] != data.epsFile[i])
                    sfound = false;
            }
            if (sfound) {
                found = true;
                idx = i;
            } else {
                idx++;
            }
        }

        if (!found)
            return mbbox;


        mbbox = new long[4];
        idx += readLongString(data, mbbox, 0, idx);
        idx += readLongString(data, mbbox, 1, idx);
        idx += readLongString(data, mbbox, 2, idx);
        idx += readLongString(data, mbbox, 3, idx);

        return mbbox;
    }

    private int readLongString(EPSImage.EPSData data, long[] mbbox, int i, int idx) {
        while (idx < data.epsFile.length && (data.epsFile[idx] == 32))
            idx++;

        int nidx = idx;

        while (nidx < data.epsFile.length &&
                ((data.epsFile[nidx] >= 48 && data.epsFile[nidx] <= 57) ||
                (data.epsFile[nidx] == 45)))
            nidx++;

        byte[] num = new byte[nidx - idx];
        System.arraycopy(data.epsFile, idx, num, 0, nidx - idx);
        String ns = new String(num);
        mbbox[i] = Long.parseLong(ns);

        return (1 + nidx - idx);
    }

    public String getMimeType() {
        return "image/eps";
    }

}

