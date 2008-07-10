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

package org.apache.fop.render.afp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This keeps information about the current state when writing to an AFP datastream.
 */
public class AFPState extends org.apache.fop.render.AbstractState {

    private static Log log = LogFactory.getLog("org.apache.fop.render.afp.AFPState");

    /**
     * The portrait rotation
     */
    private int portraitRotation = 0;

    /**
     * The landscape rotation
     */
    private int landscapeRotation = 270;

    /**
     * Flag to the set the output object type for images
     */
    private boolean colorImages = false;

    /**
     * Default value for image depth
     */
    private int bitsPerPixel = 8;

    /**
     * The output resolution
     */
    private int resolution = 240; // 240 dpi

    /**
     * The current page
     */
    private AFPPageState pageState = new AFPPageState();

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param rotation
     *            The rotation in degrees.
     */
    protected void setPortraitRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180
                || rotation == 270) {
            portraitRotation = rotation;
        } else {
            throw new IllegalArgumentException(
                    "The portrait rotation must be one"
                            + " of the values 0, 90, 180, 270");

        }
    }

    /**
     * @return the rotation to be used for portrait pages
     */
    protected int getPortraitRotation() {
        return this.portraitRotation;
    }

    /**
     * Sets the rotation to be used for landscape pages, valid values are 0, 90,
     * 180, 270 (default).
     *
     * @param rotation
     *            The rotation in degrees.
     */
    protected void setLandscapeRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180
                || rotation == 270) {
            landscapeRotation = rotation;
        } else {
            throw new IllegalArgumentException(
                    "The landscape rotation must be one"
                            + " of the values 0, 90, 180, 270");
        }
    }

    /**
     * @return the landscape rotation
     */
    protected int getLandscapeRotation() {
        return this.landscapeRotation;
    }

    /**
     * Sets the number of bits used per pixel
     *
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        switch (bitsPerPixel) {
        case 1:
        case 4:
        case 8:
            this.bitsPerPixel = bitsPerPixel;
            break;
        default:
            log.warn("Invalid bits_per_pixel value, must be 1, 4 or 8.");
            this.bitsPerPixel = 8;
            break;
        }
    }

    /**
     * @return the number of bits per pixel
     */
    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /**
     * Sets whether images are color or not
     *
     * @param colorImages
     *            color image output
     */
    public void setColorImages(boolean colorImages) {
        this.colorImages = colorImages;
    }

    /**
     * @return true if color images are to be used
     */
    protected boolean isColorImages() {
        return this.colorImages;
    }

    /**
     * Sets the output/device resolution
     *
     * @param resolution
     *            the output resolution (dpi)
     */
    public void setResolution(int resolution) {
        if (log.isDebugEnabled()) {
            log.debug("renderer-resolution set to: " + resolution + "dpi");
        }
        this.resolution = resolution;
    }

    /**
     * Returns the output/device resolution.
     *
     * @return the resolution in dpi
     */
    protected int getResolution() {
        return this.resolution;
    }

    /** {@inheritDoc} */
    protected AbstractData instantiateData() {
        return new AFPData();
    }

    /**
     * @return the state of the current page
     */
    protected AFPPageState getPageState() {
        return this.pageState;
    }

    /**
     * Sets if the current painted shape is to be filled
     * @param fill true if the current painted shape is to be filled
     * @return true if the fill value has changed
     */
    protected boolean setFill(boolean fill) {
        if (fill != ((AFPData)getData()).filled) {
            ((AFPData)getData()).filled = fill;
            return true;
        }
        return false;
    }

    /**
     * Gets the current page fonts
     * @return the current page fonts
     */
    protected AFPPageFonts getPageFonts() {
        return pageState.getFonts();
    }

    /**
     * Increments and returns the page font count
     * @return the page font count
     */
    public int incrementPageFontCount() {
        return pageState.incrementFontCount();
    }

    /**
     * Sets the page width
     * @param pageWidth the page width
     */
    public void setPageWidth(int pageWidth) {
        pageState.setWidth(pageWidth);
    }

    /**
     * @return the page width
     */
    public int getPageWidth() {
        return pageState.getWidth();
    }

    /**
     * Sets the page height
     * @param pageHeight the page height
     */
    public void setPageHeight(int pageHeight) {
        pageState.setHeight(pageHeight);
    }

    /**
     * @return the page height
     */
    public int getPageHeight() {
        return pageState.getHeight();
    }

    /**
     * Sets the uri of the current image
     * @param uri the uri of the current image
     */
    protected void setImageUri(String uri) {
        ((AFPData)getData()).imageUri = uri;
    }

    /**
     * Gets the uri of the current image
     * @return the uri of the current image
     */
    public String getImageUri() {
        return ((AFPData)getData()).imageUri;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPState{portraitRotation=" + portraitRotation
        + ", landscapeRotation=" + landscapeRotation
        + ", colorImages=" + colorImages
        + ", bitsPerPixel=" + bitsPerPixel
        + ", resolution=" + resolution
        + ", pageState=" + pageState
        + "}";
    }

    /**
     * Page level state data
     */
    private class AFPPageState {
        /** The current page width */
        private int width = 0;

        /** The current page height */
        private int height = 0;

        /** The current page fonts */
        private AFPPageFonts fonts = new AFPPageFonts();

        /** The current page font count */
        private int fontCount = 0;

        /**
         * @return the page width
         */
        protected int getWidth() {
            return width;
        }

        /**
         * Sets the page width
         * @param width the page width
         */
        protected void setWidth(int width) {
            this.width = width;
        }

        /**
         * @return the page height
         */
        protected int getHeight() {
            return height;
        }

        /**
         * Sets the page height
         * @param height the page height
         */
        protected void setHeight(int height) {
            this.height = height;
        }

        /**
         * @return the page fonts
         */
        protected AFPPageFonts getFonts() {
            return fonts;
        }

        /**
         * Sets the current page fonts
         * @param fonts the current page fonts
         */
        protected void setFonts(AFPPageFonts fonts) {
            this.fonts = fonts;
        }

        /**
         * @return increment and return the current page font count
         */
        protected int incrementFontCount() {
            return ++fontCount;
        }

        /** {@inheritDoc} */
        public String toString() {
            return "AFPPageState{width=" + width
            + ", height=" + height
            + ", fonts=" + fonts
            + ", fontCount=" + fontCount
            + "}";
        }
    }

    /**
     * Block level data
     */
    private class AFPData extends org.apache.fop.render.AbstractState.AbstractData {
        private static final long serialVersionUID = -1789481244175275686L;

        /** The current fill status */
        private boolean filled = false;

        private String imageUri = null;

        /** {@inheritDoc} */
        public Object clone() throws CloneNotSupportedException {
            AFPData obj = (AFPData)super.clone();
            obj.filled = this.filled;
            obj.imageUri = this.imageUri;
            return obj;
        }

        /** {@inheritDoc} */
        public String toString() {
            return "AFPData{" + super.toString()
            + ", filled=" + filled
            + ", imageUri=" + imageUri
            + "}";
        }
    }
}