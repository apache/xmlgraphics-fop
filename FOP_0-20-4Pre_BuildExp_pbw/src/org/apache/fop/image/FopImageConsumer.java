/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Author:       Eric SCHAEFFER
// Description:  implement ImageConsumer for FopImage classes

package org.apache.fop.image;

// Java
import java.util.Hashtable;
import java.awt.image.*;
import java.awt.*;

import java.lang.reflect.Array;

// CONSUMER CLASS
public class FopImageConsumer implements ImageConsumer {
    protected int width = -1;
    protected int height = -1;
    protected Integer imageStatus = new Integer(-1);
    protected int hints = 0;
    protected Hashtable properties = null;
    protected ColorModel cm = null;
    protected ImageProducer ip = null;

    public FopImageConsumer(ImageProducer iprod) {
        this.ip = iprod;
    }

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
            if (imageStatus.intValue() != ImageConsumer.STATICIMAGEDONE &&
                    imageStatus.intValue() != ImageConsumer.SINGLEFRAMEDONE)
                this.imageStatus = new Integer(status);
        }
    }

    public void setColorModel(ColorModel model) {
        // log.error("setColorModel: " + model);
        this.cm = model;
    }

    public void setDimensions(int width, int height) {
        // log.error("setDimension: w=" + width + " h=" + height);
        this.width = width;
        this.height = height;
    }

    public void setHints(int hintflags) {
        // log.error("setHints: " + hintflags);
        this.hints = hintflags;
    }

    public void setProperties(Hashtable props) {
        // log.error("setProperties: " + props);
        this.properties = props;
    }

    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte[] pixels, int off, int scansize) {}

    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          int[] pixels, int off, int scansize) {}

    public boolean isImageReady() throws Exception {
        synchronized (this.imageStatus) {
            if (this.imageStatus.intValue() == ImageConsumer.IMAGEABORTED)
                throw new Exception("Image aborted");
            if (this.imageStatus.intValue() == ImageConsumer.IMAGEERROR)
                throw new Exception("Image error");

            if (imageStatus.intValue() == ImageConsumer.STATICIMAGEDONE ||
                    imageStatus.intValue() == ImageConsumer.SINGLEFRAMEDONE)
                return true;

            return false;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ColorModel getColorModel() {
        return this.cm;
    }

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
            throw new Exception("Image grabbing interrupted : " +
                                intex.getMessage());
        }
        return tmpMap;
    }

}
