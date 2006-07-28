/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.image;

// AWT
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;
import java.awt.Color;

// JAI
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.IOUtils;
// Sun codec
import com.sun.media.jai.codec.FileCacheSeekableStream;

/**
 * FopImage object using JAI.
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public class JAIImage extends AbstractFopImage {

    /**
     * Create a new JAI image.
     *
     * @param imgInfo the image info for this JAI image
     */
    public JAIImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * @see org.apache.fop.image.AbstractFopImage#loadDimensions()
     */
    protected boolean loadDimensions() {
        if (this.bitmaps == null) {
            loadImage();
        }
        return this.bitmaps != null;
    }
    
    /**
     * @see org.apache.fop.image.AbstractFopImage#loadBitmap()
     */
    protected boolean loadBitmap() {
        if (this.bitmaps == null) {
            loadImage();
        }

        return this.bitmaps != null;
    }

    /**
     * Loads the image from the inputstream
     */
    protected void loadImage() {
        com.sun.media.jai.codec.FileCacheSeekableStream seekableInput = null;
        RenderedOp imageOp = null;
        try {
            seekableInput = new FileCacheSeekableStream(inputStream);
            imageOp = JAI.create("stream", seekableInput);

            this.height = imageOp.getHeight();
            this.width = imageOp.getWidth();

            ColorModel cm = imageOp.getColorModel();
            //this.bitsPerPixel = 8;
            this.bitsPerPixel = cm.getPixelSize();
            
            // TODO: the getRGB() function converts the image into the RGB
            // colorspace. However, here we assume the image colorspace is kept.
            // It should be either one of them, but not both. Unfortunately
            // there are other hacks for images in the CMYK colorspace (e.g. in
            // the PDF output) that would need to be changed as well.
            
            //this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            this.colorSpace = cm.getColorSpace();
            
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
            this.bitmaps = new byte[this.width * this.height * 3];
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    int p = tmpMap[i * this.width + j];
                    int r = (p >> 16) & 0xFF;
                    int g = (p >> 8) & 0xFF;
                    int b = (p) & 0xFF;
                    this.bitmaps[3 * (i * this.width + j)] = (byte)(r & 0xFF);
                    this.bitmaps[3 * (i * this.width + j) + 1] = (byte)(g & 0xFF);
                    this.bitmaps[3 * (i * this.width + j) + 2] = (byte)(b & 0xFF);
                }
            }

        } catch (Exception ex) {
            log.error("Error while loading image (JAI): " + ex.getMessage(), ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
            inputStream = null;
            if (imageOp != null) {
                imageOp.dispose();
            }
            if (seekableInput != null) {
                IOUtils.closeQuietly(seekableInput);
            }
        }
    }

    /** @see org.apache.fop.image.AbstractFopImage#loadOriginalData() */
    protected boolean loadOriginalData() {
        return loadDefaultOriginalData();
    }
    
}

