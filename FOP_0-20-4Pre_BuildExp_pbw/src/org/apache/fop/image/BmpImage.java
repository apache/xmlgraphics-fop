/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

/**
 * FopImage object for BMP images.
 * @author Art WELCH
 * @see AbstractFopImage
 * @see FopImage
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

public class BmpImage extends AbstractFopImage {
    public BmpImage(URL href, FopImage.ImageInfo imgReader) {
        super(href, imgReader);
    }

    protected boolean loadBitmap(FOUserAgent ua) {
        int wpos = 18;
        int hpos = 22; // offset positioning for w and height in  bmp files
        int[] headermap = new int[54];
        int filepos = 0;
        InputStream file = null;
        byte palette[] = null;
        try {
            file = this.m_href.openStream();
            boolean eof = false;
            while ((!eof) && (filepos < 54)) {
                int input = file.read();
                if (input == -1)
                    eof = true;
                else
                    headermap[filepos++] = input;
            }

            if (headermap[28] == 4 || headermap[28] == 8) {
                int palettesize = 1 << headermap[28];
                palette = new byte[palettesize * 3];
                int countr = 0;
                while (!eof && countr < palettesize) {
                    int count2 = 2;
                    while (!eof && count2 >= -1) {
                        int input = file.read();
                        if (input == -1)
                            eof = true;
                        else if (count2 >= 0) {
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
                                         + this.m_href.toString() + " : "
                                         + e.getClass() + " - "
                                         + e.getMessage(), e);
            return false;
        }
        // gets h & w from headermap
        this.m_width = headermap[wpos] + headermap[wpos + 1] * 256 +
                       headermap[wpos + 2] * 256 * 256 +
                       headermap[wpos + 3] * 256 * 256 * 256;
        this.m_height = headermap[hpos] + headermap[hpos + 1] * 256 +
                        headermap[hpos + 2] * 256 * 256 +
                        headermap[hpos + 3] * 256 * 256 * 256;

        int imagestart = headermap[10] + headermap[11] * 256 +
                         headermap[12] * 256 * 256 + headermap[13] * 256 * 256 * 256;
        this.m_bitsPerPixel = headermap[28];
        this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
        int bytes = 0;
        if (this.m_bitsPerPixel == 1)
            bytes = (this.m_width + 7) / 8;
        else if (this.m_bitsPerPixel == 24)
            bytes = this.m_width * 3;
        else if (this.m_bitsPerPixel == 4 || this.m_bitsPerPixel == 8)
            bytes = this.m_width / (8 / this.m_bitsPerPixel);
        else {
            ua.getLogger().error("Image (" + this.m_href.toString()
                                         + ") has " + this.m_bitsPerPixel
                                         + " which is not a supported BMP format.");
            return false;
        }
        if ((bytes & 0x03) != 0) {
            bytes |= 0x03;
            bytes++;
        }

        // Should take care of the ColorSpace and bitsPerPixel
        this.m_bitmapsSize = this.m_width * this.m_height * 3;
        this.m_bitmaps = new byte[this.m_bitmapsSize];

        int[] temp = new int[bytes * this.m_height];
        try {
            int input;
            int count = 0;
            file.skip((long)(imagestart - filepos));
            while ((input = file.read()) != -1)
                temp[count++] = input;
            file.close();
        } catch (IOException e) {
            ua.getLogger().error("Error while loading image "
                                         + this.m_href.toString() + " : "
                                         + e.getClass() + " - "
                                         + e.getMessage(), e);
            return false;
        }

        for (int i = 0; i < this.m_height; i++) {
            int x = 0;
            int j = 0;
            while (j < bytes) {
                int p = temp[(this.m_height - i - 1) * bytes + j];

                if (this.m_bitsPerPixel == 24 && x < this.m_width) {
                    int countr = 2;
                    do {
                        this.m_bitmaps[3 * (i * this.m_width + x) +
                                       countr] =
                                         (byte)(temp[(this.m_height - i - 1) *
                                                     bytes + j] & 0xFF);
                        j++;
                    } while (--countr >= 0)
                        ;
                    x++;
                } else if (this.m_bitsPerPixel == 1) {
                    for (int countr = 0;
                            countr < 8 && x < this.m_width; countr++) {
                        if ((p & 0x80) != 0) {
                            this.m_bitmaps[3 *
                                           (i * this.m_width + x)] = (byte) 0xFF;
                            this.m_bitmaps[3 * (i * this.m_width + x) +
                                           1] = (byte) 0xFF;
                            this.m_bitmaps[3 * (i * this.m_width + x) +
                                           2] = (byte) 0xFF;
                        } else {
                            this.m_bitmaps[3 *
                                           (i * this.m_width + x)] = (byte) 0;
                            this.m_bitmaps[3 * (i * this.m_width + x) +
                                           1] = (byte) 0;
                            this.m_bitmaps[3 * (i * this.m_width + x) +
                                           2] = (byte) 0;
                        }
                        p <<= 1;
                        x++;
                    }
                    j++;
                } else if (this.m_bitsPerPixel == 4) {
                    for (int countr = 0;
                            countr < 2 && x < this.m_width; countr++) {
                        int pal = ((p & 0xF0) >> 4) * 3;
                        this.m_bitmaps[3 * (i * this.m_width + x)] =
                          palette[pal];
                        this.m_bitmaps[3 * (i * this.m_width + x) +
                                       1] = palette[pal + 1];
                        this.m_bitmaps[3 * (i * this.m_width + x) +
                                       2] = palette[pal + 2];
                        p <<= 4;
                        x++;
                    }
                    j++;
                } else if (this.m_bitsPerPixel == 8) {
                    if (x < this.m_width) {
                        p *= 3;
                        this.m_bitmaps[3 * (i * this.m_width + x)] =
                          palette[p];
                        this.m_bitmaps[3 * (i * this.m_width + x) +
                                       1] = palette[p + 1];
                        this.m_bitmaps[3 * (i * this.m_width + x) +
                                       2] = palette[p + 2];
                        j++;
                        x++;
                    } else
                        j = bytes;
                } else
                    j++;
            }
        }

        // This seems really strange to me, but I noticed that JimiImage hardcodes
        // m_bitsPerPixel to 8. If I do not do this Acrobat is unable to read the resultant PDF,
        // so we will hardcode this...
        this.m_bitsPerPixel = 8;

        return true;
    }

}
