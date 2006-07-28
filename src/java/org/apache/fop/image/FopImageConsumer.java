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

// Java
import java.util.Hashtable;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;

/**
 * ImageConsumer implementation for FopImage classes.
 * @author Eric SCHAEFFER
 */
public class FopImageConsumer implements ImageConsumer {
    
    /** Image width in pixels */
    protected int width = -1;
    /** Image height in pixels */
    protected int height = -1;
    /** Image status */
    protected Integer imageStatus = new Integer(-1);
    /** hints */
    protected int hints = 0;
    /** Image properties */
    protected Hashtable properties = null;
    /** Color model */
    protected ColorModel cm = null;
    /** Image producer */
    protected ImageProducer ip = null;

    /**
     * Main constructor
     * @param iprod ImageProducer to use
     */
    public FopImageConsumer(ImageProducer iprod) {
        this.ip = iprod;
    }

    /**
     * @see java.awt.image.ImageConsumer#imageComplete(int)
     */
    public void imageComplete(int status) {
        /*
         * log.error("Status ");
         * if (status == ImageConsumer.COMPLETESCANLINES) {
         * log.error("CompleteScanLines");
         * } else if (status == ImageConsumer.IMAGEABORTED) {
         * log.error("ImageAborted");
         * } else if (status == ImageConsumer.IMAGEERROR) {
         * log.error("ImageError");
         * } else if (status == ImageConsumer.RANDOMPIXELORDER) {
         * log.error("RandomPixelOrder");
         * } else if (status == ImageConsumer.SINGLEFRAME) {
         * log.error("SingleFrame");
         * } else if (status == ImageConsumer.SINGLEFRAMEDONE) {
         * log.error("SingleFrameDone");
         * } else if (status == ImageConsumer.SINGLEPASS) {
         * log.error("SinglePass");
         * } else if (status == ImageConsumer.STATICIMAGEDONE) {
         * log.error("StaticImageDone");
         * } else if (status == ImageConsumer.TOPDOWNLEFTRIGHT) {
         * log.error("TopDownLeftRight");
         * }
         */
        synchronized (this.imageStatus) {
            // Need to stop status if image done
            if (imageStatus.intValue() != ImageConsumer.STATICIMAGEDONE
                    && imageStatus.intValue() != ImageConsumer.SINGLEFRAMEDONE) {
                this.imageStatus = new Integer(status);
            }
        }
    }

    /**
     * @see java.awt.image.ImageConsumer#setColorModel(ColorModel)
     */
    public void setColorModel(ColorModel model) {
        // log.error("setColorModel: " + model);
        this.cm = model;
    }

    /**
     * @see java.awt.image.ImageConsumer#setDimensions(int, int)
     */
    public void setDimensions(int width, int height) {
        // log.error("setDimension: w=" + width + " h=" + height);
        this.width = width;
        this.height = height;
    }

    /**
     * @see java.awt.image.ImageConsumer#setHints(int)
     */
    public void setHints(int hintflags) {
        // log.error("setHints: " + hintflags);
        this.hints = hintflags;
    }

    /**
     * @see java.awt.image.ImageConsumer#setProperties(Hashtable)
     */
    public void setProperties(Hashtable props) {
        // log.error("setProperties: " + props);
        this.properties = props;
    }

    /**
     * @see java.awt.image.ImageConsumer#setPixels(int, int, int, int, ColorModel, byte[], int, int)
     */
    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte[] pixels, int off, int scansize) {
    }

    /**
     * @see java.awt.image.ImageConsumer#setPixels(int, int, int, int, ColorModel, int[], int, int)
     */
    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          int[] pixels, int off, int scansize) {
    }

    /**
     * Indicates whether the image is ready.
     * @return boolean True if the image is ready, false if it's still loading
     * @throws Exception If an error happened while loading the image
     */
    public boolean isImageReady() throws Exception {
        /**@todo Use a better exception than Exception */
        synchronized (this.imageStatus) {
            if (this.imageStatus.intValue() == ImageConsumer.IMAGEABORTED) {
                throw new Exception("Image aborted");
            }
            if (this.imageStatus.intValue() == ImageConsumer.IMAGEERROR) {
                throw new Exception("Image error");
            }

            if (imageStatus.intValue() == ImageConsumer.STATICIMAGEDONE
                    || imageStatus.intValue() == ImageConsumer.SINGLEFRAMEDONE) {
                return true;
            } 

            return false;
        }
    }

    /**
     * Returns the image width
     * @return the width in pixels
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the image height
     * @return the height in pixels
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the color model of the image
     * @return the color model
     */
    public ColorModel getColorModel() {
        return this.cm;
    }

    /**
     * Returns the bitmap as an array.
     * @return the bitmap as an array.
     * @throws Exception if an error occured while generating the array
     */
    public int[] getImage() throws Exception {
        int tmpMap[] = new int[this.width * this.height];
        PixelGrabber pg = new PixelGrabber(this.ip, 0, 0, this.width,
                                           this.height, tmpMap, 0, this.width);
        pg.setDimensions(this.width, this.height);
        pg.setColorModel(this.cm);
        pg.setHints(this.hints);
        pg.setProperties(this.properties);
        try {
            pg.grabPixels();
        } catch (InterruptedException intex) {
            /**@todo Use a better exception than Exception */
            throw new Exception("Image grabbing interrupted : "
                                + intex.getMessage());
        }
        return tmpMap;
    }

}

