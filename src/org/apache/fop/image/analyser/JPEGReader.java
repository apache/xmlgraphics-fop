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
 * ImageReader object for JPEG image type.
 * @author Pankaj Narula
 * @version 1.0
 */
public class JPEGReader implements ImageReader {

    /**
     * Only SOFn and APPn markers are defined as SOFn is needed for the height and
     * width search. APPn is also defined because if the JPEG contains thumbnails
     * the dimensions of the thumnail would also be after the SOFn marker enclosed
     * inside the APPn marker. And we don't want to confuse those dimensions with
     * the image dimensions.
     */
    static protected final int MARK = 0xff; // Beginneing of a Marker
    static protected final int NULL = 0x00; // Special case for 0xff00
    static protected final int SOF1 = 0xc0; // Baseline DCT
    static protected final int SOF2 = 0xc1; // Extended Sequential DCT
    static protected final int SOF3 = 0xc2; // Progrssive DCT only PDF 1.3
    static protected final int SOFA = 0xca; // Progressice DCT only PDF 1.3
    static protected final int APP0 = 0xe0; // Application marker, JFIF
    static protected final int APPF = 0xef; // Application marker
    static protected final int SOS = 0xda; // Start of Scan
    static protected final int SOI = 0xd8; // start of Image
    static protected final int JPG_SIG_LENGTH = 2;

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(fis);
        boolean supported = ((header[0] == (byte) 0xff) &&
                             (header[1] == (byte) 0xd8));
        if (supported) {
            FopImage.ImageInfo info = getDimension(fis);
            info.mimeType = getMimeType();
            return info;
        } else {
            return null;
        }
    }

    public String getMimeType() {
        return "image/jpeg";
    }

    protected byte[] getDefaultHeader(BufferedInputStream imageStream) throws IOException {
        byte[] header = new byte[JPG_SIG_LENGTH];
        try {
            imageStream.mark(JPG_SIG_LENGTH + 1);
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

    protected FopImage.ImageInfo getDimension(BufferedInputStream imageStream) throws IOException {
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        try {
            int marker = NULL;
            long length, skipped;
            outer:
            while (imageStream.available() > 0) {
                while ((marker = imageStream.read()) != MARK) {
                    ;
                }
                do {
                    marker = imageStream.read();
                } while (marker == MARK)
                    ;
                switch (marker) {
                    case SOI:
                        break;
                    case NULL:
                        break;
                    case SOF1:
                    case SOF2:
                    case SOF3: // SOF3 and SOFA are only supported by PDF 1.3
                    case SOFA:
                        this.skip(imageStream, 3);
                        info.height = this.read2bytes(imageStream);
                        info.width = this.read2bytes(imageStream);
                        break outer;
                    default:
                        length = this.read2bytes(imageStream);
                        skipped = this.skip(imageStream, length - 2);
                        if (skipped != length - 2)
                            throw new IOException("Skipping Error");
                }
            }
        } catch (IOException ioe) {
            try {
                imageStream.reset();
            } catch (IOException exbis) {}
            throw ioe;
        }
        return info;
    }

    protected int read2bytes(BufferedInputStream imageStream) throws IOException {
        int byte1 = imageStream.read();
        int byte2 = imageStream.read();
        return (int)((byte1 << 8) | byte2);
    }

    protected long skip(BufferedInputStream imageStream, long n) throws IOException {
        long discarded = 0;
        while (discarded != n) {
            imageStream.read();
            discarded++;
        }
        return discarded; // scope for exception
    }

}

