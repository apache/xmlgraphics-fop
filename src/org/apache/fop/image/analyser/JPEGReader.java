/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.IOException;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;

/**
 * ImageReader object for JPEG image type.
 *
 * @author Pankaj Narula
 * @version $Id$
 */
public class JPEGReader implements ImageReader {

    /**
     * Only SOFn and APPn markers are defined as SOFn is needed for the height and
     * width search. APPn is also defined because if the JPEG contains thumbnails
     * the dimensions of the thumnail would also be after the SOFn marker enclosed
     * inside the APPn marker. And we don't want to confuse those dimensions with
     * the image dimensions.
     */
    private static final int MARK = 0xff; // Beginning of a Marker
    private static final int NULL = 0x00; // Special case for 0xff00
    private static final int SOF1 = 0xc0; // Baseline DCT
    private static final int SOF2 = 0xc1; // Extended Sequential DCT
    private static final int SOF3 = 0xc2; // Progrssive DCT only PDF 1.3
    private static final int SOFA = 0xca; // Progressice DCT only PDF 1.3
    private static final int APP0 = 0xe0; // Application marker, JFIF
    private static final int APPF = 0xef; // Application marker
    private static final int SOS = 0xda; // Start of Scan
    private static final int SOI = 0xd8; // start of Image
    private static final int JPG_SIG_LENGTH = 2;

    /** @see org.apache.fop.image.analyser.ImageReader */
    public FopImage.ImageInfo verifySignature(String uri, InputStream fis,
                                   FOUserAgent ua) throws IOException {
        byte[] header = getDefaultHeader(fis);
        boolean supported = ((header[0] == (byte) 0xff)
                    && (header[1] == (byte) 0xd8));
        if (supported) {
            FopImage.ImageInfo info = getDimension(fis);
            info.mimeType = getMimeType();
            info.inputStream = fis;
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
        return "image/jpeg";
    }

    private byte[] getDefaultHeader(InputStream imageStream) throws IOException {
        byte[] header = new byte[JPG_SIG_LENGTH];
        try {
            imageStream.mark(JPG_SIG_LENGTH + 1);
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

    private FopImage.ImageInfo getDimension(InputStream imageStream) throws IOException {
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        try {
            imageStream.mark(imageStream.available());
            int marker = NULL;
            long length, skipped;
outer:
            while (imageStream.available() > 0) {
                while ((marker = imageStream.read()) != MARK) {
                    //nop, simply skip
                }

                do {
                    marker = imageStream.read();
                } while (marker == MARK);

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
                        if (skipped != length - 2) {
                            throw new IOException("Skipping Error");
                        }
                }
            }
            imageStream.reset();
        } catch (IOException ioe) {
            try {
                imageStream.reset();
            } catch (IOException exbis) {
                // throw the original exception, not this one
            }
            throw ioe;
        }
        return info;
    }

    private int read2bytes(InputStream imageStream) throws IOException {
        int byte1 = imageStream.read();
        int byte2 = imageStream.read();
        return (int) ((byte1 << 8) | byte2);
    }

    private long skip(InputStream imageStream, long n) throws IOException {
        long discarded = 0;
        while (discarded != n) {
            imageStream.read();
            discarded++;
        }
        return discarded; // scope for exception
    }

}

