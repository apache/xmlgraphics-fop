/*
 * Copyright 2004 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.batik.ext.awt.image.codec.SeekableStream;
import org.apache.batik.ext.awt.image.codec.MemoryCacheSeekableStream;
import org.apache.batik.ext.awt.image.codec.FileCacheSeekableStream;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;

/**
 * FopImage object using TIFF
 * @author Eric SCHAEFFER
 * @see AbstractFopImage
 * @see FopImage
 */
public abstract class BatikImage extends AbstractFopImage {

    private byte[] softMask = null;

    /**
     * Constructs a new BatikImage instance.
     * @param imgReader basic metadata for the image
     */
    public BatikImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
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
     * @see org.apache.fop.image.FopImage#hasSoftMask()
     */
    public boolean hasSoftMask() {
        if (this.bitmaps == null) {
            loadImage();
        }

        return (this.softMask != null);
    }

    /**
     * @see org.apache.fop.image.FopImage#getSoftMask()
     */
    public byte[] getSoftMask() {
        if (this.bitmaps == null) {
            loadImage();
        }

        return this.softMask;
    }

    /**
     * Decodes the image from the stream.
     * @param stream the stream to read the image from
     * @return the decoded image
     * @throws IOException in case an I/O problem occurs
     */
    protected abstract CachableRed decodeImage(SeekableStream stream) throws IOException;
    
    /**
     * Loads the image from the InputStream.
     */
    protected void loadImage() {
        try {
            SeekableStream seekableInput;
            try { seekableInput = new FileCacheSeekableStream(inputStream);
            } catch (IOException ioe) {
                seekableInput = new MemoryCacheSeekableStream(inputStream);
            }

            CachableRed cr = decodeImage(seekableInput);
            ColorModel cm = cr.getColorModel();

            this.height = cr.getHeight();
            this.width  = cr.getWidth();
            this.isTransparent = false;
            this.softMask = null;
            this.bitmapsSize = this.width * this.height * 3;
            this.bitmaps = new byte[this.bitmapsSize];
            this.bitsPerPixel = 8;

            int transparencyType = cm.getTransparency();
            if (cm instanceof IndexColorModel)  {
                if (transparencyType == Transparency.BITMASK) {
                // Use 'transparent color'.
                IndexColorModel icm = (IndexColorModel)cm;
                int numColor = icm.getMapSize();
                byte [] alpha = new byte[numColor];
                icm.getAlphas(alpha);
                for (int i = 0; i < numColor; i++) {
                    if ((alpha[i] & 0xFF) == 0) {
                        this.isTransparent = true;
                        int red = (icm.getRed  (i)) & 0xFF;
                        int grn = (icm.getGreen(i)) & 0xFF;
                        int blu = (icm.getBlue (i)) & 0xFF;
                        this.transparentColor = new Color(red, grn, blu);
                        break;
                    }
                }
            }
            } else {
                cr = new Any2sRGBRed(cr);
            }

            // Get our current ColorModel
            cm = cr.getColorModel();

            // It has an alpha channel so generate a soft mask.
            if (!this.isTransparent && cm.hasAlpha())
                this.softMask = new byte[this.width * this.height];

            this.colorSpace = cm.getColorSpace();
            WritableRaster wr = (WritableRaster)cr.getData();
            BufferedImage bi = new BufferedImage
                (cm, wr.createWritableTranslatedChild(0, 0), 
                 cm.isAlphaPremultiplied(), null);
            int [] tmpMap = new int[this.width];
            int idx = 0;
            int sfIdx = 0;
            for (int y = 0; y < this.height; y++) {
                tmpMap = bi.getRGB(0, y, this.width, 1, tmpMap, 0, this.width);
                if (softMask != null) {
                    for (int x = 0; x < this.width; x++) {
                        int pix = tmpMap[x];
                        this.softMask[sfIdx++] = (byte)(pix >>> 24);
                        this.bitmaps[idx++]    = (byte)((pix >>> 16) & 0xFF);
                        this.bitmaps[idx++]    = (byte)((pix >>> 8)  & 0xFF);
                        this.bitmaps[idx++]    = (byte)((pix)        & 0xFF);
                    }
                } else {
                    for (int x = 0; x < this.width; x++) {
                        int pix = tmpMap[x];
                        this.bitmaps[idx++] = (byte)((pix >> 16) & 0xFF);
                        this.bitmaps[idx++] = (byte)((pix >> 8)  & 0xFF);
                        this.bitmaps[idx++] = (byte)((pix)       & 0xFF);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error loading an image" , ex);
            /*throw new FopImageException("Error while loading image "
                                         + "" + " : "
                                         + ex.getClass() + " - "
                                         + ex.getMessage());
            */
        }
    }
};
