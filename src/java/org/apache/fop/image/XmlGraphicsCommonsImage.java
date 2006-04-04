/*
 * Copyright 2004-2006 The Apache Software Foundation 
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
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;
import org.apache.xmlgraphics.image.codec.util.MemoryCacheSeekableStream;
import org.apache.xmlgraphics.image.codec.util.FileCacheSeekableStream;
import org.apache.xmlgraphics.image.rendered.CachableRed;

import org.apache.commons.io.IOUtils;

/**
 * Abstract FopImage implementation which uses the internal codecs from XML Graphics Commons.
 * @see AbstractFopImage
 * @see FopImage
 */
public abstract class XmlGraphicsCommonsImage extends AbstractFopImage {

    private byte[] softMask = null;

    /**
     * The InputStream wrapped into a SeekableStream for decoding.
     */
    protected SeekableStream seekableInput = null;

    /**
     * The Batik representation of the image
     */
    protected CachableRed cr = null;
    
    /**
     * Constructs a new BatikImage instance.
     * @param imgReader basic metadata for the image
     */
    public XmlGraphicsCommonsImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
    }

    /**
     * @see org.apache.fop.image.AbstractFopImage#loadDimensions()
     */
    protected boolean loadDimensions() {
        if (seekableInput == null && inputStream != null) {
            try { 
                seekableInput = new FileCacheSeekableStream(inputStream);
            } catch (IOException ioe) {
                seekableInput = new MemoryCacheSeekableStream(inputStream);
            }
            try {
                this.bitsPerPixel = 8;
                cr = decodeImage(seekableInput);
                this.height = cr.getHeight();
                this.width  = cr.getWidth();
                this.isTransparent = false;
                this.softMask = null;
                ColorModel cm = cr.getColorModel();

                this.height = cr.getHeight();
                this.width  = cr.getWidth();
                this.isTransparent = false;
                this.softMask = null;

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
                    cr = GraphicsUtil.convertTosRGB(cr);
                }

                // Get our current ColorModel
                cm = cr.getColorModel();
                if (this.colorSpace == null) {
                    this.colorSpace = cm.getColorSpace();
                }
            } catch (IOException ioe) {
                log.error("Error while loading image (Batik): " + ioe.getMessage(), ioe);
                IOUtils.closeQuietly(seekableInput);
                IOUtils.closeQuietly(inputStream);
                seekableInput = null;
                inputStream = null;
                return false;
            }
        }
        return this.height != -1;
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
        if (this.bitmaps == null && this.raw == null) {
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
        if (loadDimensions()) {
            try {
                if (cr == null) {
                    throw new IllegalStateException(
                            "Can't load the bitmaps data without the CachableRed instance");
                }
                
                // Get our current ColorModel
                ColorModel cm = cr.getColorModel();

                // It has an alpha channel so generate a soft mask.
                if (!this.isTransparent && cm.hasAlpha()) {
                    this.softMask = new byte[this.width * this.height];
                }

                this.bitmaps = new byte[this.width * this.height * 3];

                constructBitmaps(cr, this.bitmaps, this.softMask);
            } catch (Exception ex) {
                log.error("Error while loading image (Batik): " + ex.getMessage(), ex);
            } finally {
                // Make sure we clean up
                IOUtils.closeQuietly(seekableInput);
                IOUtils.closeQuietly(inputStream);
                seekableInput = null;
                inputStream = null;
                cr = null;
            }
        }
    }

    private static void constructBitmaps(RenderedImage red, byte[] bitmaps, byte[] softMask) {
        WritableRaster wr = (WritableRaster)red.getData();
        ColorModel cm = red.getColorModel();
        BufferedImage bi = new BufferedImage
            (cm, wr.createWritableTranslatedChild(0, 0), 
             cm.isAlphaPremultiplied(), null);
        int width = red.getWidth();
        int height = red.getHeight();
        int [] tmpMap = new int[width];
        int idx = 0;
        int sfIdx = 0;
        for (int y = 0; y < height; y++) {
            tmpMap = bi.getRGB(0, y, width, 1, tmpMap, 0, width);
            if (softMask != null) {
                for (int x = 0; x < width; x++) {
                    int pix = tmpMap[x];
                    softMask[sfIdx++] = (byte)(pix >>> 24);
                    bitmaps[idx++]    = (byte)((pix >>> 16) & 0xFF);
                    bitmaps[idx++]    = (byte)((pix >>> 8)  & 0xFF);
                    bitmaps[idx++]    = (byte)((pix)        & 0xFF);
                }
            } else {
                for (int x = 0; x < width; x++) {
                    int pix = tmpMap[x];
                    bitmaps[idx++] = (byte)((pix >> 16) & 0xFF);
                    bitmaps[idx++] = (byte)((pix >> 8)  & 0xFF);
                    bitmaps[idx++] = (byte)((pix)       & 0xFF);
                }
            }
        }
    }
    
}