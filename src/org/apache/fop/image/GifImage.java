/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.color.ColorSpace;

// FOP
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
    public GifImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
    }

    protected boolean loadBitmap(FOUserAgent ua) {
        int[] tmpMap = null;

        try {
            ImageProducer ip = null;
            // todo: how to load gif image from stream
            //ip = (ImageProducer) inputStream.getContent();
            inputStream.close();
            inputStream = null;
            if (ip == null) {
                return false;
            }
            FopImageConsumer consumer = new FopImageConsumer(ip);
            ip.startProduction(consumer);


            //Load the image into memory
            while (!consumer.isImageReady()) {
                Thread.sleep(500);
            }

            this.height = consumer.getHeight();
            this.width = consumer.getWidth();

            try {
                tmpMap = consumer.getImage();
            } catch (Exception ex) {
                ua.getLogger().error("Image grabbing interrupted : "
                                             + ex.getMessage(), ex);
                return false;
            }

            ColorModel cm = consumer.getColorModel();
            this.bitsPerPixel = 8;
            // this.bitsPerPixel = cm.getPixelSize();
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            if (cm.hasAlpha()) {
                int transparencyType = cm.getTransparency(); // java.awt.Transparency. BITMASK or OPAQUE or TRANSLUCENT
                if (transparencyType == java.awt.Transparency.OPAQUE) {
                    this.isTransparent = false;
                } else if (transparencyType ==
                    java.awt.Transparency.BITMASK) {
                    if (cm instanceof IndexColorModel) {
                        IndexColorModel indexcm = (IndexColorModel) cm;
                        this.isTransparent = false;
                        byte[] alphas = new byte[indexcm.getMapSize()];
                        byte[] reds = new byte[indexcm.getMapSize()];
                        byte[] greens = new byte[indexcm.getMapSize()];
                        byte[] blues = new byte[indexcm.getMapSize()];
                        indexcm.getAlphas(alphas);
                        indexcm.getReds(reds);
                        indexcm.getGreens(greens);
                        indexcm.getBlues(blues);
                        for (int i = 0;
                                i < indexcm.getMapSize();
                                i++) {
                            if ((alphas[i] & 0xFF) == 0) {
                                this.isTransparent = true;
                                this.transparentColor = new PDFColor(
                                                            (int)(reds[i] & 0xFF),
                                                            (int)(greens[i] & 0xFF),
                                                            (int)(blues[i] & 0xFF));
                                break;
                            }
                        }
                    } else {
                        // TRANSLUCENT
                        /*
                         * this.isTransparent = false;
                         * for (int i = 0; i < this.width * this.height; i++) {
                         * if (cm.getAlpha(tmpMap[i]) == 0) {
                         * this.isTransparent = true;
                         * this.transparentColor = new PDFColor(cm.getRed(tmpMap[i]), cm.getGreen(tmpMap[i]), cm.getBlue(tmpMap[i]));
                         * break;
                         * }
                         * }
                         */
                        // use special API...
                        this.isTransparent = false;
                    }
                } else {
                    this.isTransparent = false;
                }
            } else {
                this.isTransparent = false;
            }
        } catch (Exception ex) {
            ua.getLogger().error("Error while loading image "
                                         + "" + " : "
                                         + ex.getClass() + " - "
                                         + ex.getMessage(), ex);
            return false;
        }

        // Should take care of the ColorSpace and bitsPerPixel
        this.bitmapsSize = this.width * this.height * 3;
        this.bitmaps = new byte[this.bitmapsSize];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int p = tmpMap[i * this.width + j];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                this.bitmaps[3 * (i * this.width + j)] =
                  (byte)(r & 0xFF);
                this.bitmaps[3 * (i * this.width + j) + 1] =
                  (byte)(g & 0xFF);
                this.bitmaps[3 * (i * this.width + j) + 2] =
                  (byte)(b & 0xFF);
            }
        }
        return true;
    }

}

