/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

/**
 * FopImage object for GIF images, using Java native classes.
 * @author Eric SCHAEFFER
 * @author Modified by Eric Dalquist - 9/14/2001 - ebdalqui@mtu.edu
 * @see AbstractFopImage
 * @see FopImage
 */
public class GifImage extends AbstractFopImage {
    public GifImage(URL href, FopImage.ImageInfo imgReader) {
        super(href, imgReader);
    }

    protected boolean loadBitmap(FOUserAgent ua) {
        int[] tmpMap = null;
        try {
            ImageProducer ip = (ImageProducer) this.m_href.getContent();
            FopImageConsumer consumer = new FopImageConsumer(ip);
            ip.startProduction(consumer);


            //Load the image into memory
            while (!consumer.isImageReady()) {
                Thread.sleep(500);
            }

            this.m_height = consumer.getHeight();
            this.m_width = consumer.getWidth();

            try {
                tmpMap = consumer.getImage();
            } catch (Exception ex) {
                ua.getLogger().error("Image grabbing interrupted : "
                                             + ex.getMessage(), ex);
                return false;
            }

            ColorModel cm = consumer.getColorModel();
            this.m_bitsPerPixel = 8;
            // this.m_bitsPerPixel = cm.getPixelSize();
            this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
            if (cm.hasAlpha()) {
                int transparencyType = cm.getTransparency(); // java.awt.Transparency. BITMASK or OPAQUE or TRANSLUCENT
                if (transparencyType == java.awt.Transparency.OPAQUE) {
                    this.m_isTransparent = false;
                } else if (transparencyType ==
                    java.awt.Transparency.BITMASK) {
                    if (cm instanceof IndexColorModel) {
                        this.m_isTransparent = false;
                        byte[] alphas = new byte[
                                          ((IndexColorModel) cm).getMapSize()];
                        byte[] reds = new byte[
                                        ((IndexColorModel) cm).getMapSize()];
                        byte[] greens = new byte[
                                          ((IndexColorModel) cm).getMapSize()];
                        byte[] blues = new byte[
                                         ((IndexColorModel) cm).getMapSize()];
                        ((IndexColorModel) cm).getAlphas(alphas);
                        ((IndexColorModel) cm).getReds(reds);
                        ((IndexColorModel) cm).getGreens(greens);
                        ((IndexColorModel) cm).getBlues(blues);
                        for (int i = 0;
                                i < ((IndexColorModel) cm).getMapSize();
                                i++) {
                            if ((alphas[i] & 0xFF) == 0) {
                                this.m_isTransparent = true;
                                this.m_transparentColor = new PDFColor(
                                                            (int)(reds[i] & 0xFF),
                                                            (int)(greens[i] & 0xFF),
                                                            (int)(blues[i] & 0xFF));
                                break;
                            }
                        }
                    } else {
                        // TRANSLUCENT
                        /*
                         * this.m_isTransparent = false;
                         * for (int i = 0; i < this.m_width * this.m_height; i++) {
                         * if (cm.getAlpha(tmpMap[i]) == 0) {
                         * this.m_isTransparent = true;
                         * this.m_transparentColor = new PDFColor(cm.getRed(tmpMap[i]), cm.getGreen(tmpMap[i]), cm.getBlue(tmpMap[i]));
                         * break;
                         * }
                         * }
                         */
                        // use special API...
                        this.m_isTransparent = false;
                    }
                } else {
                    this.m_isTransparent = false;
                }
            } else {
                this.m_isTransparent = false;
            }
        } catch (Exception ex) {
            ua.getLogger().error("Error while loading image "
                                         + this.m_href.toString() + " : "
                                         + ex.getClass() + " - "
                                         + ex.getMessage(), ex);
            return false;
        }

        // Should take care of the ColorSpace and bitsPerPixel
        this.m_bitmapsSize = this.m_width * this.m_height * 3;
        this.m_bitmaps = new byte[this.m_bitmapsSize];
        for (int i = 0; i < this.m_height; i++) {
            for (int j = 0; j < this.m_width; j++) {
                int p = tmpMap[i * this.m_width + j];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                this.m_bitmaps[3 * (i * this.m_width + j)] =
                  (byte)(r & 0xFF);
                this.m_bitmaps[3 * (i * this.m_width + j) + 1] =
                  (byte)(g & 0xFF);
                this.m_bitmaps[3 * (i * this.m_width + j) + 2] =
                  (byte)(b & 0xFF);
            }
        }
        return true;
    }

}

