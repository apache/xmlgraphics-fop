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
import java.awt.color.ColorSpace;

// Jimi
import com.sun.jimi.core.*;

// FOP
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

import org.apache.avalon.framework.logger.Logger;

/**
 * FopImage object for several images types, using Jimi.
 * See Jimi documentation for supported image types.
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public class JimiImage extends AbstractFopImage {
    public JimiImage(URL href, FopImage.ImageInfo imgReader) {
        super(href, imgReader);
        try {
            Class c = Class.forName("com.sun.jimi.core.Jimi");
        } catch (ClassNotFoundException e) {
            //throw new FopImageException("Jimi image library not available");
        }
    }

    protected boolean loadDimensions(FOUserAgent ua) {
        if(this.m_bitmaps == null) {
            loadImage(ua.getLogger());
        }

        return this.m_bitmaps != null;
    }

    protected boolean loadBitmap(FOUserAgent ua) {
        if(this.m_bitmaps == null) {
            loadImage(ua.getLogger());
        }

        return this.m_bitmaps != null;
    }

    protected void loadImage(Logger log) {
        int[] tmpMap = null;
        try {
            ImageProducer ip =
              Jimi.getImageProducer(this.m_href.openStream(),
                                    Jimi.SYNCHRONOUS | Jimi.IN_MEMORY);
            FopImageConsumer consumer = new FopImageConsumer(ip);
            ip.startProduction(consumer);

            while (!consumer.isImageReady()) {
                Thread.sleep(500);
            }
            this.m_height = consumer.getHeight();
            this.m_width = consumer.getWidth();

            try {
                tmpMap = consumer.getImage();
            } catch (Exception ex) {
                log.error("Image grabbing interrupted", ex);
                return;
            }

            ColorModel cm = consumer.getColorModel();
            this.m_bitsPerPixel = 8;
            // this.m_bitsPerPixel = cm.getPixelSize();
            this.m_colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
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
        } catch (Throwable ex) {
            log.error("Error while loading image "
                               + this.m_href.toString(), ex);
            return;
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
    }

}

