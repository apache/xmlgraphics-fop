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
import java.io.IOException;
import java.awt.color.ColorSpace;

// FOP
import org.apache.fop.fo.FOUserAgent;

/**
 * Bitmap image.
 * This supports loading a bitmap image into bitmap data.
 *
 * @author Art WELCH
 * @see AbstractFopImage
 * @see FopImage
 */
public class BmpImage extends AbstractFopImage {
    /**
     * Create a bitmap image with the image data.
     *
     * @param imgInfo the image information
     */
    public BmpImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the bitmap.
     * This laods the bitmap data from the bitmap image.
     *
     * @param ua the user agent
     * @return true if it was loaded successfully
     */
    protected boolean loadBitmap(FOUserAgent ua) {
        int wpos = 18;
        int hpos = 22; // offset positioning for w and height in  bmp files
        int[] headermap = new int[54];
        int filepos = 0;
        byte palette[] = null;
        try {
            boolean eof = false;
            while ((!eof) && (filepos < 54)) {
                int input = inputStream.read();
                if (input == -1) {
                    eof = true;
                } else {
                    headermap[filepos++] = input;
                }
            }

            if (headermap[28] == 4 || headermap[28] == 8) {
                int palettesize = 1 << headermap[28];
                palette = new byte[palettesize * 3];
                int countr = 0;
                while (!eof && countr < palettesize) {
                    int count2 = 2;
                    while (!eof && count2 >= -1) {
                        int input = inputStream.read();
                        if (input == -1) {
                            eof = true;
                        } else if (count2 >= 0) {
                            palette[countr * 3 + count2] =
                              (byte)(input & 0xFF);
                        }
                        count2--;
                        filepos++;
                    }
                    countr++;
                }
            }
        } catch (IOException e) {
            ua.getLogger().error("Error while loading image "
                                         + "" + " : "
                                         + e.getClass() + " - "
                                         + e.getMessage(), e);
            return false;
        }
        // gets h & w from headermap
        this.width = headermap[wpos] 
                + headermap[wpos + 1] * 256
                + headermap[wpos + 2] * 256 * 256
                + headermap[wpos + 3] * 256 * 256 * 256;
        this.height = headermap[hpos] 
                + headermap[hpos + 1] * 256
                + headermap[hpos + 2] * 256 * 256
                + headermap[hpos + 3] * 256 * 256 * 256;

        int imagestart = headermap[10] 
                + headermap[11] * 256
                + headermap[12] * 256 * 256
                + headermap[13] * 256 * 256 * 256;
        this.bitsPerPixel = headermap[28];
        this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        int bytes = 0;
        if (this.bitsPerPixel == 1) {
            bytes = (this.width + 7) / 8;
        } else if (this.bitsPerPixel == 24) {
            bytes = this.width * 3;
        } else if (this.bitsPerPixel == 4 || this.bitsPerPixel == 8) {
            bytes = this.width / (8 / this.bitsPerPixel);
        } else {
            ua.getLogger().error("Image (" + ""
                                         + ") has " + this.bitsPerPixel
                                         + " which is not a supported BMP format.");
            return false;
        }
        if ((bytes & 0x03) != 0) {
            bytes |= 0x03;
            bytes++;
        }

        // Should take care of the ColorSpace and bitsPerPixel
        this.bitmapsSize = this.width * this.height * 3;
        this.bitmaps = new byte[this.bitmapsSize];

        int[] temp = new int[bytes * this.height];
        try {
            int input;
            int count = 0;
            inputStream.skip((long)(imagestart - filepos));
            while ((input = inputStream.read()) != -1) {
                temp[count++] = input;
            }
            inputStream.close();
            inputStream = null;
        } catch (IOException e) {
            ua.getLogger().error("Error while loading image "
                                         + "" + " : "
                                         + e.getClass() + " - "
                                         + e.getMessage(), e);
            return false;
        }

        for (int i = 0; i < this.height; i++) {
            int x = 0;
            int j = 0;
            while (j < bytes) {
                int p = temp[(this.height - i - 1) * bytes + j];

                if (this.bitsPerPixel == 24 && x < this.width) {
                    int countr = 2;
                    do {
                        this.bitmaps[3 * (i * this.width + x) + countr] =
                                         (byte)(temp[(this.height - i - 1)
                                                     * bytes + j] & 0xFF);
                        j++;
                    } while (--countr >= 0)
                        ;
                    x++;
                } else if (this.bitsPerPixel == 1) {
                    for (int countr = 0;
                            countr < 8 && x < this.width; countr++) {
                        if ((p & 0x80) != 0) {
                            this.bitmaps[3 * (i * this.width + x)] = (byte) 0xFF;
                            this.bitmaps[3 * (i * this.width + x) + 1] = (byte) 0xFF;
                            this.bitmaps[3 * (i * this.width + x) + 2] = (byte) 0xFF;
                        } else {
                            this.bitmaps[3 * (i * this.width + x)] = (byte) 0;
                            this.bitmaps[3 * (i * this.width + x) + 1] = (byte) 0;
                            this.bitmaps[3 * (i * this.width + x) + 2] = (byte) 0;
                        }
                        p <<= 1;
                        x++;
                    }
                    j++;
                } else if (this.bitsPerPixel == 4) {
                    for (int countr = 0;
                            countr < 2 && x < this.width; countr++) {
                        int pal = ((p & 0xF0) >> 4) * 3;
                        this.bitmaps[3 * (i * this.width + x)] = palette[pal];
                        this.bitmaps[3 * (i * this.width + x) + 1] = palette[pal + 1];
                        this.bitmaps[3 * (i * this.width + x) + 2] = palette[pal + 2];
                        p <<= 4;
                        x++;
                    }
                    j++;
                } else if (this.bitsPerPixel == 8) {
                    if (x < this.width) {
                        p *= 3;
                        this.bitmaps[3 * (i * this.width + x)] = palette[p];
                        this.bitmaps[3 * (i * this.width + x) + 1] = palette[p + 1];
                        this.bitmaps[3 * (i * this.width + x) + 2] = palette[p + 2];
                        j++;
                        x++;
                    } else {
                        j = bytes;
                    }
                } else {
                    j++;
                }
            }
        }

        // This seems really strange to me, but I noticed that
        // JimiImage hardcodes bitsPerPixel to 8. If I do not
        // do this Acrobat is unable to read the resultant PDF,
        // so we will hardcode this...
        this.bitsPerPixel = 8;

        return true;
    }

}

