/*
 * $Id: JpegImage.java,v 1.12 2003/03/06 21:25:44 jeremias Exp $
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
import java.io.ByteArrayOutputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

// FOP
import org.apache.fop.fo.FOUserAgent;

/**
 * FopImage object for JPEG images, Using Java native classes.
 * @author Eric Dalquist
 * @see AbstractFopImage
 * @see FopImage
 */
public class JpegImage extends AbstractFopImage {
    private ICC_Profile iccProfile = null;
    private boolean foundICCProfile = false;
    private boolean foundDimensions = false;

    /**
     * Create a jpeg image with the info.
     *
     * @param imgInfo the image info for this jpeg
     */
    public JpegImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the original jpeg data.
     * This loads the original jpeg data and reads the color space,
     * and icc profile if any.
     *
     * @param ua the user agent
     * @return true if loaded false for any error
     */
    protected boolean loadOriginalData(FOUserAgent ua) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream iccStream = new ByteArrayOutputStream();
        int index = 0;
        boolean cont = true;

        try {
            byte[] readBuf = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytesRead);
            }
            inputStream.close();
            inputStream = null;
        } catch (java.io.IOException ex) {
            ua.getLogger().error("Error while loading image "
                                         + " : " + ex.getClass()
                                         + " - " + ex.getMessage(), ex);
            return false;
        }

        this.bitmaps = baos.toByteArray();
        this.bitsPerPixel = 8;
        this.isTransparent = false;

        if (this.bitmaps.length > (index + 2)
                && uByte(this.bitmaps[index]) == 255
                && uByte(this.bitmaps[index + 1]) == 216) {
            index += 2;

            while (index < this.bitmaps.length && cont) {
                //check to be sure this is the begining of a header
                if (this.bitmaps.length > (index + 2)
                        && uByte(this.bitmaps[index]) == 255) {

                    //192 or 194 are the header bytes that contain
                    // the jpeg width height and color depth.
                    if (uByte(this.bitmaps[index + 1]) == 192
                            || uByte(this.bitmaps[index + 1]) == 194) {

                        this.height = calcBytes(this.bitmaps[index + 5],
                                                  this.bitmaps[index + 6]);
                        this.width = calcBytes(this.bitmaps[index + 7],
                                                 this.bitmaps[index + 8]);

                        if (this.bitmaps[index + 9] == 1) {
                            this.colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_GRAY);
                        } else if (this.bitmaps[index + 9] == 3) {
                            this.colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_LINEAR_RGB);
                        } else if (this.bitmaps[index + 9] == 4) {
                            // howto create CMYK color space
                            this.colorSpace = ColorSpace.getInstance(
                              ColorSpace.CS_CIEXYZ);
                        } else {
                            ua.getLogger().error("Unknown ColorSpace for image: "
                                                   + "");
                            return false;
                        }

                        foundDimensions = true;
                        if (foundICCProfile) {
                            cont = false;
                            break;
                        }
                        index += calcBytes(this.bitmaps[index + 2],
                                           this.bitmaps[index + 3]) + 2;

                    } else if (uByte(this.bitmaps[index + 1]) == 226
                                   && this.bitmaps.length > (index + 60)) {
                        // Check if ICC profile
                        byte[] iccString = new byte[11];
                        System.arraycopy(this.bitmaps, index + 4,
                                         iccString, 0, 11);

                        if ("ICC_PROFILE".equals(new String(iccString))) {
                            int chunkSize = calcBytes(
                                              this.bitmaps[index + 2],
                                              this.bitmaps[index + 3]) + 2;

                            iccStream.write(this.bitmaps,
                                            index + 18, chunkSize - 18);

                        }

                        index += calcBytes(this.bitmaps[index + 2],
                                           this.bitmaps[index + 3]) + 2;
                    } else {
                        index += calcBytes(this.bitmaps[index + 2],
                                           this.bitmaps[index + 3]) + 2;
                    }

                } else {
                    cont = false;
                }
            }
        } else {
            ua.getLogger().error("Error while loading "
                                          + "JpegImage - Invalid JPEG Header.");
            return false;
        }
        if (iccStream.size() > 0) {
            byte[] align = new byte[((iccStream.size()) % 8) + 8];
            try {
                iccStream.write(align);
            } catch (Exception e) {
                ua.getLogger().error("Error while loading image "
                                              + " : "
                                              + e.getMessage(), e);
                return false;
            }
            try {
                iccProfile = ICC_Profile.getInstance(iccStream.toByteArray());
            } catch (Exception e) {
                ua.getLogger().error("Invalid ICC profile: " + e, e);
                return false;
            }
        } else if (this.colorSpace == null) {
            ua.getLogger().error("ColorSpace not specified for JPEG image");
            return false;
        }
        return true;
    }

    /**
     * Get the ICC profile for this Jpeg image.
     *
     * @return the icc profile or null if not found
     */
    public ICC_Profile getICCProfile() {
        return iccProfile;
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

