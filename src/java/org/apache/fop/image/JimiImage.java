/*
 * $Id: JimiImage.java,v 1.18 2003/03/06 21:25:44 jeremias Exp $
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
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.color.ColorSpace;
import java.awt.Color;

// Jimi
import com.sun.jimi.core.Jimi;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// FOP
import org.apache.fop.apps.FOUserAgent;

/**
 * FopImage object for several images types, using Jimi.
 * See Jimi documentation for supported image types.
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public class JimiImage extends AbstractFopImage {

    public JimiImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
        try {
            Class c = Class.forName("com.sun.jimi.core.Jimi");
        } catch (ClassNotFoundException e) {
            //throw new FopImageException("Jimi image library not available");
        }
    }

    protected boolean loadDimensions(FOUserAgent ua) {
        if (this.bitmaps == null) {
            loadImage(ua.getLogger());
        }

        return this.bitmaps != null;
    }

    /**
     * @see org.apache.fop.image.AbstractFopImage#loadBitmap(FOUserAgent)
     */
    protected boolean loadBitmap(FOUserAgent ua) {
        if (this.bitmaps == null) {
            loadImage(ua.getLogger());
        }

        return this.bitmaps != null;
    }

    protected void loadImage(Logger log) {
        int[] tmpMap = null;
        try {
            ImageProducer ip =
              Jimi.getImageProducer(inputStream,
                                    Jimi.SYNCHRONOUS | Jimi.IN_MEMORY);
            FopImageConsumer consumer = new FopImageConsumer(ip);
            ip.startProduction(consumer);

            while (!consumer.isImageReady()) {
                Thread.sleep(500);
            }
            this.height = consumer.getHeight();
            this.width = consumer.getWidth();

            inputStream.close();
            inputStream = null;

            try {
                tmpMap = consumer.getImage();
            } catch (Exception ex) {
                log.error("Image grabbing interrupted", ex);
                return;
            }

            ColorModel cm = consumer.getColorModel();
            this.bitsPerPixel = 8;
            // this.bitsPerPixel = cm.getPixelSize();
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            if (cm.hasAlpha()) {
                // java.awt.Transparency. BITMASK or OPAQUE or TRANSLUCENT
                int transparencyType = cm.getTransparency();
                if (transparencyType == java.awt.Transparency.OPAQUE) {
                    this.isTransparent = false;
                } else if (transparencyType == java.awt.Transparency.BITMASK) {
                    if (cm instanceof IndexColorModel) {
                        this.isTransparent = false;
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
                                this.isTransparent = true;
                                this.transparentColor = new Color(
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
                         * this.transparentColor = new PDFColor(cm.getRed(tmpMap[i]),
                         *      cm.getGreen(tmpMap[i]), cm.getBlue(tmpMap[i]));
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
        } catch (Throwable ex) {
            log.error("Error while loading image "
                               + "", ex);
            return;
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
    }

}

