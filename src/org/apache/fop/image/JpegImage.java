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
package org.apache.fop.image;

// Java
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.image.analyser.ImageReader;

/**
 * FopImage object for JPEG images, Using Java native classes.
 * @author Eric Dalquist
 * @author Ben Galbraith (ben [at] galbraiths [dot] org)
 * @see AbstractFopImage
 * @see FopImage
 */
public class JpegImage extends AbstractFopImage {
    boolean isPhotoshopJfif = false;
    boolean hasAPPEMarker = false;
    boolean found_icc_profile = false;
    boolean found_dimensions = false;

    public JpegImage(URL href) throws FopImageException {
        super(href);
    }

    public JpegImage(URL href,
                     ImageReader imgReader) throws FopImageException {
        super(href, imgReader);
    }

    protected void loadImage() throws FopImageException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream iccStream = new ByteArrayOutputStream();
        InputStream inStream;
        this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_UNKNOWN);
        byte[] readBuf = new byte[4096];
        int bytes_read;
        int index = 0;
        boolean cont = true;

        this.m_compressionType = new DCTFilter();
        this.m_compressionType.setApplied(true);

        try {
            inStream = this.m_href.openStream();

            while ((bytes_read = inStream.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytes_read);
            }
        } catch (java.io.IOException ex) {
            throw new FopImageException("Error while loading image " +
                                        this.m_href.toString() + " : " + ex.getClass() +
                                        " - " + ex.getMessage());
        }

        this.m_bitmaps = baos.toByteArray();
        this.m_bitsPerPixel = 8;
        this.m_isTransparent = false;

        if (this.m_bitmaps.length > (index + 2) &&
                uByte(this.m_bitmaps[index]) == 255 &&
                uByte(this.m_bitmaps[index + 1]) == 216) {
            index += 2;

            while (index < this.m_bitmaps.length && cont) {
                //check to be sure this is the begining of a header
                if (this.m_bitmaps.length > (index + 2) &&
                        uByte(this.m_bitmaps[index]) == 255) {

                    //192 or 194 are the header bytes that contain the jpeg width height and color depth.
                    if (uByte(this.m_bitmaps[index + 1]) == 192 ||
                            uByte(this.m_bitmaps[index + 1]) == 194) {

                        this.m_height = calcBytes(this.m_bitmaps[index + 5],
                                                  this.m_bitmaps[index + 6]);
                        this.m_width = calcBytes(this.m_bitmaps[index + 7],
                                                 this.m_bitmaps[index + 8]);

                        if (this.m_bitmaps[index + 9] == 1) {
                            this.m_colorSpace.setColorSpace(ColorSpace.DEVICE_GRAY);
                        } else if (this.m_bitmaps[index + 9] == 3) {
                            this.m_colorSpace.setColorSpace(ColorSpace.DEVICE_RGB);
                        } else if (this.m_bitmaps[index + 9] == 4) {
                            this.m_colorSpace.setColorSpace(ColorSpace.DEVICE_CMYK);
                        }

                        found_dimensions = true;
                        if (found_icc_profile) {
                            cont = false;
                            break;
                        }
                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;

                    } else if (uByte(this.m_bitmaps[index+1]) == 226 &&
                               this.m_bitmaps.length > (index+60)) {
                        // Check if ICC profile
                        byte[] icc_string = new byte[11];
                        System.arraycopy(this.m_bitmaps, index+4, icc_string, 0, 11);

                        /*
                        byte[] acsp = new byte[4];
                        System.arraycopy(this.m_bitmaps, index+18+36, acsp, 0, 4);
                        boolean first_chunk = false;
                        if ("acsp".equals(new String(acsp))) {
                            System.out.println("1st icc chunk");
                            first_chunk = true;
                        }
                        */
                        if ("ICC_PROFILE".equals(new String(icc_string))){
                            int chunkSize = calcBytes(this.m_bitmaps[index + 2],
                                                      this.m_bitmaps[index + 3]) + 2;

                            iccStream.write(this.m_bitmaps, index+16, chunkSize - 18); // eller 18..
                        }

                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                      // Check for Adobe APPE Marker
                    } else if ((uByte(this.m_bitmaps[index]) == 0xff &&
                                uByte(this.m_bitmaps[index+1]) == 0xee &&
                                uByte(this.m_bitmaps[index+2]) == 0 &&
                                uByte(this.m_bitmaps[index+3]) == 14 &&
                                "Adobe".equals(new String(this.m_bitmaps, index+4, 5)))) {
                        /*
                         * It turns out that the Adobe APPE marker specification is used by more than just Adobe.
                         * However, Photoshop is the only widely identified program that inverts the CMYK values.
                         * This issue is documented in the document "USING THE IJG JPEG LIBRARY" produced by the
                         * Independent JPEG Group's software.  Thus, the presence of the APPE marker will no longer be
                         * used to assume that the CMYK values have been inverted, as was the case in earlier versions
                         * of this file.
                         *
                         * I could not find a test for determining if the values themselves are inverted; instead,
                         * I will check if Adobe Photoshop was used to create the file below.
                         *
                         * This code will remain present for the time being. -blg
                         */
                        //hasAPPEMarker = true;

                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                        // check if Adobe Photoshop created this file
                    } else if (((uByte(this.m_bitmaps[index]) == 0xff)) &&
                            (uByte(this.m_bitmaps[index + 1]) == 0xe1)) {
                        /*
                         * Check if Adobe Photoshop was used to generate this file; if so, a later check will
                         * determine if the color space is CMYK and thus the colors should be inverted -blg
                         */
                        if (this.m_bitmaps.length >= index + 124 + 15) { // prevent index out of range error
                            if ("Adobe Photoshop".equals(new String(this.m_bitmaps, index+124, 15))) {
                                isPhotoshopJfif = true;
                            }
                        }

                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                    } else {
                        index += calcBytes(this.m_bitmaps[index + 2],
                                           this.m_bitmaps[index + 3]) + 2;
                    }


                } else {
                    cont = false;
                    /*
                    throw new FopImageException(
                      "\n2 Error while loading image " +
                      this.m_href.toString() + " : JpegImage - Invalid JPEG Header (bad header byte).");
                      */
                }
            }
        } else {
            throw new FopImageException( "\n1 Error while loading image " +
                                         this.m_href.toString() + " : JpegImage - Invalid JPEG Header.");
        }
        if (iccStream.size() > 0) {
            byte[] align = new byte[((iccStream.size()) % 8) + 8];
            try {iccStream.write(align);} catch (Exception e) {
                throw new FopImageException( "\n1 Error while loading image " +
                              this.m_href.toString() + " : " + e.getMessage());
            }
            this.m_colorSpace.setICCProfile(iccStream.toByteArray());
        }

        if (isPhotoshopJfif && this.m_colorSpace.getColorSpace() == ColorSpace.DEVICE_CMYK)
            this.m_invertImage = true;
    }

    private int calcBytes(byte bOne, byte bTwo) {
        return (uByte(bOne) * 256) + uByte(bTwo);
    }

    private int uByte(byte bIn) {
        if (bIn < 0) {
            return 256 + bIn;
        } else {
            return bIn;
        }
    }
}


