/*
 * $Id$
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
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.util.Hashtable;

/**
 * Image consumer implementation for FopImage classes
 * @author Eric SCHAEFFER
 */
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
         * MessageHandler.error("Status ");
         * if (status == ImageConsumer.COMPLETESCANLINES) {
         * MessageHandler.errorln("CompleteScanLines");
         * } else if (status == ImageConsumer.IMAGEABORTED) {
         * MessageHandler.errorln("ImageAborted");
         * } else if (status == ImageConsumer.IMAGEERROR) {
         * MessageHandler.errorln("ImageError");
         * } else if (status == ImageConsumer.RANDOMPIXELORDER) {
         * MessageHandler.errorln("RandomPixelOrder");
         * } else if (status == ImageConsumer.SINGLEFRAME) {
         * MessageHandler.errorln("SingleFrame");
         * } else if (status == ImageConsumer.SINGLEFRAMEDONE) {
         * MessageHandler.errorln("SingleFrameDone");
         * } else if (status == ImageConsumer.SINGLEPASS) {
         * MessageHandler.errorln("SinglePass");
         * } else if (status == ImageConsumer.STATICIMAGEDONE) {
         * MessageHandler.errorln("StaticImageDone");
         * } else if (status == ImageConsumer.TOPDOWNLEFTRIGHT) {
         * MessageHandler.errorln("TopDownLeftRight");
         * }
         */
        synchronized (this.imageStatus) {
            // Need to stop status if image done
            if (imageStatus.intValue() != ImageConsumer.STATICIMAGEDONE
                && imageStatus.intValue() != ImageConsumer.SINGLEFRAMEDONE)
                this.imageStatus = new Integer(status);
        }
    }

    public void setColorModel(ColorModel model) {
        // MessageHandler.errorln("setColorModel: " + model);
        this.cm = model;
    }

    public void setDimensions(int width, int height) {
        // MessageHandler.errorln("setDimension: w=" + width + " h=" + height);
        this.width = width;
        this.height = height;
    }

    public void setHints(int hintflags) {
        // MessageHandler.errorln("setHints: " + hintflags);
        this.hints = hintflags;
    }

    public void setProperties(Hashtable props) {
        // MessageHandler.errorln("setProperties: " + props);
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

            if (imageStatus.intValue() == ImageConsumer.STATICIMAGEDONE
                || imageStatus.intValue() == ImageConsumer.SINGLEFRAMEDONE)
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
                                           this.height, tmpMap, 0,
                                           this.width);
        pg.setDimensions(this.width, this.height);
        pg.setColorModel(this.cm);
        pg.setHints(this.hints);
        pg.setProperties(this.properties);
        try {
            pg.grabPixels();
        } catch (InterruptedException intex) {
            throw new Exception("Image grabbing interrupted : "
                                + intex.getMessage());
        }
        return tmpMap;
    }

}
