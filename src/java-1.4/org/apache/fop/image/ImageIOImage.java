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
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;
import java.util.Iterator;

// ImageIO
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.fop.util.UnitConv;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * FopImage object using ImageIO.
 * @see AbstractFopImage
 * @see FopImage
 */
public class ImageIOImage extends AbstractFopImage {

    private byte[] softMask = null;

    /**
     * Creates a new ImageIOImage.
     * @param info the image info from the ImageReader
     */
    public ImageIOImage(FopImage.ImageInfo info) {
        super(info);
        if ("image/png".equals(info.mimeType)
                || "image/tiff".equals(info.mimeType)) {
            this.loaded = 0; //TODO The PNG and TIFF Readers cannot read the resolution, yet. 
        }
    }

    /**
     * @see org.apache.fop.image.AbstractFopImage#loadDimensions()
     */
    protected boolean loadDimensions() {
        if (this.bitmaps == null) {
            return loadBitmap();
        }
        return true;
    }
    
    private Element getChild(Element el, String name) {
        NodeList nodes = el.getElementsByTagName(name);
        if (nodes.getLength() > 0) {
            return (Element)nodes.item(0);
        } else {
            return null;
        }
    }
    
    /** @see org.apache.fop.image.AbstractFopImage#loadBitmap() */
    protected boolean loadBitmap() {
        if (this.bitmaps != null) {
            return true;
        }
        try {
            inputStream.reset();
            ImageInputStream imgStream = ImageIO.createImageInputStream(inputStream);
            Iterator iter = ImageIO.getImageReaders(imgStream);
            if (!iter.hasNext()) {
                log.error("No ImageReader found.");
                return false;
            }
            ImageReader reader = (ImageReader)iter.next();
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(imgStream, true, false);
            BufferedImage imageData = reader.read(0, param);
            
            //Read image resolution
            IIOMetadata iiometa = reader.getImageMetadata(0);
            if (iiometa != null && iiometa.isStandardMetadataFormatSupported()) {
                Element metanode = (Element)iiometa.getAsTree("javax_imageio_1.0");
                Element dim = getChild(metanode, "Dimension");
                if (dim != null) {
                    Element child;
                    child = getChild(dim, "HorizontalPixelSize");
                    if (child != null) {
                        this.dpiHorizontal = UnitConv.IN2MM
                                / Float.parseFloat(child.getAttribute("value"));
                    }
                    child = getChild(dim, "VerticalPixelSize");
                    if (child != null) {
                        this.dpiVertical = UnitConv.IN2MM
                                / Float.parseFloat(child.getAttribute("value"));
                    }
                }
            }
            imgStream.close();
            reader.dispose();
            
            this.height = imageData.getHeight();
            this.width = imageData.getWidth();

            ColorModel cm = imageData.getColorModel();
            this.bitsPerPixel = cm.getComponentSize(0); //only use first, we assume all are equal
            //this.colorSpace = cm.getColorSpace();
            //We currently force the image to sRGB
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

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
                        //TODO Is there another case?
                        this.isTransparent = false;
                    }
                } else {
                    // TRANSLUCENT
                    this.softMask = new byte[width * height];
                    imageData.getAlphaRaster().getDataElements(
                            0, 0, width, height, this.softMask);
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
                    this.bitmaps[3 * (i * this.width + j)] 
                        = (byte)(r & 0xFF);
                    this.bitmaps[3 * (i * this.width + j) + 1] 
                        = (byte)(g & 0xFF);
                    this.bitmaps[3 * (i * this.width + j) + 2] 
                        = (byte)(b & 0xFF);
                }
            }

        } catch (Exception ex) {
            log.error("Error while loading image: " + ex.getMessage(), ex);
            return false; 
        } finally {
            IOUtils.closeQuietly(inputStream);
            inputStream = null;
        }
        return true;
    }

    /** @see org.apache.fop.image.AbstractFopImage#loadOriginalData() */
    protected boolean loadOriginalData() {
        if (inputStream == null && getBitmaps() != null) {
            return false;
        } else {
            return loadDefaultOriginalData();
        }
    }
    
    /** @see org.apache.fop.image.FopImage#hasSoftMask() */
    public boolean hasSoftMask() {
        if (this.bitmaps == null && this.raw == null) {
            loadBitmap();
        }

        return (this.softMask != null);
    }

    /** @see org.apache.fop.image.FopImage#getSoftMask() */
    public byte[] getSoftMask() {
        if (this.bitmaps == null) {
            loadBitmap();
        }

        return this.softMask;
    }

}

