/*
 * $Id: JAIImage.java,v 1.10 2003/03/06 21:25:44 jeremias Exp $
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

// AWT
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;
import java.awt.Color;

// JAI
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
// Sun codec
import com.sun.media.jai.codec.FileCacheSeekableStream;

/**
 * FopImage object using JAI.
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public class JAIImage extends AbstractFopImage {

    public JAIImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
    }

    protected void loadImage() {
        try {
            com.sun.media.jai.codec.FileCacheSeekableStream seekableInput =
              new FileCacheSeekableStream(inputStream);
            RenderedOp imageOp = JAI.create("stream", seekableInput);
            inputStream.close();
            inputStream = null;

            this.height = imageOp.getHeight();
            this.width = imageOp.getWidth();

            ColorModel cm = imageOp.getColorModel();
            this.bitsPerPixel = 8;
            // this.bitsPerPixel = cm.getPixelSize();
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

            BufferedImage imageData = imageOp.getAsBufferedImage();
            int[] tmpMap = imageData.getRGB(0, 0, this.width,
                                            this.height, null, 0, this.width);

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
                         * cm.getGreen(tmpMap[i]), cm.getBlue(tmpMap[i]));
                         * break;
                         * }
                         * }
                         * // or use special API...
                         */
                        this.isTransparent = false;
                    }
                } else {
                    this.isTransparent = false;
                }
            } else {
                this.isTransparent = false;
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

        } catch (Exception ex) {
            /*throw new FopImageException("Error while loading image "
                                         + "" + " : "
                                         + ex.getClass() + " - "
                                         + ex.getMessage());
             */}
    }

}

