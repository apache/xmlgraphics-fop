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

package org.apache.fop.afp;

import java.awt.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.ColorConverter;
import org.apache.xmlgraphics.java2d.color.DefaultColorConverter;

import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.util.AbstractPaintingState;

/**
 * This keeps information about the current painting state when writing to an
 * AFP datastream.
 */
public class AFPPaintingState extends org.apache.fop.util.AbstractPaintingState implements
        Cloneable {

    private static final long serialVersionUID = 8206711712452344473L;

    private static Log log = LogFactory.getLog("org.apache.xmlgraphics.afp");

    /** the portrait rotation */
    private int portraitRotation = 0;

    /** the landscape rotation */
    private int landscapeRotation = 270;

    /** color image support */
    private boolean colorImages = false;

    /** dithering quality setting (0.0f..1.0f) */
    private float ditheringQuality;

    /** image encoding quality setting (0.0f..1.0f) */
    private float bitmapEncodingQuality;

    /** color image handler */
    private ColorConverter colorConverter = GrayScaleColorConverter.getInstance();

    /**
     * true if certain image formats may be embedded unchanged in their native
     * format.
     */
    private boolean nativeImagesSupported = false;

    private boolean canEmbedJpeg = false;

    /**
     * true if CMYK images (requires IOCA FS45 suppport on the target platform)
     * may be generated
     */
    private boolean cmykImagesSupported;

    /** default value for image depth */
    private int bitsPerPixel = 8;

    /** the output resolution */
    private int resolution = 240; // 240 dpi

    /** determines whether GOCA is enabled or disabled  */
    private boolean gocaEnabled = true;
    /** determines whether to stroke text in GOCA mode or to use text operators where possible */
    private boolean strokeGocaText = false;


    /** use page segment with F11 and F45 images*/
    private boolean pSeg;

    /** use FS45 images*/
    private boolean fs45;

    /** the current page */
    private transient AFPPagePaintingState pagePaintingState = new AFPPagePaintingState();

    // /** reference orientation */
    // private int orientation = 0;

    /** a unit converter */
    private final transient AFPUnitConverter unitConv = new AFPUnitConverter(this);

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setPortraitRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
            portraitRotation = rotation;
        } else {
            throw new IllegalArgumentException("The portrait rotation must be one"
                    + " of the values 0, 90, 180, 270");

        }
    }

    /**
     * Returns the rotation to be used for portrait pages
     *
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
    public void setLandscapeRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
            landscapeRotation = rotation;
        } else {
            throw new IllegalArgumentException("The landscape rotation must be one"
                    + " of the values 0, 90, 180, 270");
        }
    }

    /**
     * Returns the landscape rotation
     *
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
     * Returns the number of bits per pixel
     *
     * @return the number of bits per pixel
     */
    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /**
     * Sets whether images are color or not and instantiates a ColorHandler
     *
     * @param colorImages
     *            color image output
     */
    public void setColorImages(boolean colorImages) {
        this.colorImages = colorImages;

        if (colorImages) {
            this.colorConverter = DefaultColorConverter.getInstance();
        }

    }

    /**
     * Returns true if color images are to be used
     *
     * @return true if color images are to be used
     */
    public boolean isColorImages() {
        return this.colorImages;
    }

    /**
     * Used to convert color in respect of the colorImages flag
     *
     * @return the color converter
     */
    public ColorConverter getColorConverter() {
        return this.colorConverter;
    }

    /**
     * Sets whether images are natively supported or not in the AFP environment
     *
     * @param nativeImagesSupported
     *            true if images are natively supported in this AFP environment
     */
    public void setNativeImagesSupported(boolean nativeImagesSupported) {
        this.nativeImagesSupported = nativeImagesSupported;
    }

    /**
     * Returns true if images are supported natively in this AFP environment
     *
     * @return true if images are supported natively in this AFP environment
     */
    public boolean isNativeImagesSupported() {
        return this.nativeImagesSupported;
    }

    /**
     * Set whether or not JPEG images can be embedded within an AFP document.
     *
     * @param canEmbed true if the JPEG image can be embedded
     */
    public void setCanEmbedJpeg(boolean canEmbed) {
        canEmbedJpeg = canEmbed;
    }

    /**
     * Returns true if JPEGs can be embedded in an AFP document.
     *
     * @return true if JPEG embedding is allowed
     */
    public boolean canEmbedJpeg() {
        return canEmbedJpeg;
    }

    /**
     * Controls whether CMYK images (IOCA FS45) are enabled. By default, support
     * is disabled for wider compatibility. When disabled, any CMYK image is
     * converted to the selected color format.
     *
     * @param value
     *            true to enabled CMYK images
     */
    public void setCMYKImagesSupported(boolean value) {
        this.cmykImagesSupported = value;
    }

    /**
     * Indicates whether CMYK images (IOCA FS45) are enabled.
     *
     * @return true if IOCA FS45 is enabled
     */
    public boolean isCMYKImagesSupported() {
        return this.cmykImagesSupported;
    }

    /**
     * Gets the dithering quality setting to use when converting images to monochrome images.
     * @return the dithering quality (a value between 0.0f and 1.0f)
     */
    public float getDitheringQuality() {
        return this.ditheringQuality;
    }

    /**
     * Sets the dithering quality setting to use when converting images to monochrome images.
     * @param quality Defines the desired quality level for the conversion.
     *                  Valid values: a value between 0.0f (fastest) and 1.0f (best)
     */
    public void setDitheringQuality(float quality) {
        quality = Math.max(quality, 0.0f);
        quality = Math.min(quality, 1.0f);
        this.ditheringQuality = quality;
    }

    /**
     * Gets the image encoding quality setting to use when encoding bitmap images.
     * @return the encoding quality (a value between 0.0f and 1.0f, 1.0 meaning loss-less)
     */
    public float getBitmapEncodingQuality() {
        return this.bitmapEncodingQuality;
    }

    /**
     * Sets the image encoding quality setting to use when encoding bitmap images.
     * @param quality Defines the desired quality level for the conversion.
     *                  Valid values: a value between 0.0f (lowest) and 1.0f (best, loss-less)
     */
    public void setBitmapEncodingQuality(float quality) {
        quality = Math.max(quality, 0.0f);
        quality = Math.min(quality, 1.0f);
        this.bitmapEncodingQuality = quality;
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
    public int getResolution() {
        return this.resolution;
    }

    /**
     * Controls whether GOCA is enabled or disabled.
     * @param enabled true if GOCA is enabled, false if it is disabled
     */
    public void setGOCAEnabled(boolean enabled) {
        this.gocaEnabled = enabled;
    }

    /**
     * Indicates whether GOCA is enabled or disabled.
     * @return true if GOCA is enabled, false if GOCA is disabled
     */
    public boolean isGOCAEnabled() {
        return this.gocaEnabled;
    }

    /**
     * Controls whether to stroke text in GOCA mode or to use text operators where possible.
     * @param stroke true to stroke, false to paint with text operators where possible
     */
    public void setStrokeGOCAText(boolean stroke) {
        this.strokeGocaText = stroke;
    }

    /**
     * Indicates whether to stroke text in GOCA mode or to use text operators where possible.
     * @return true to stroke, false to paint with text operators where possible
     */
    public boolean isStrokeGOCAText() {
        return this.strokeGocaText;
    }

    /**
     * Whether FS11 and SF45 non-inline images should be wrapped in a page segment
     * @return true iff images should be wrapped
     */
    public boolean getWrapPSeg() {
        return pSeg;
    }

    /**
     * Sets whether FS11 and FS45 non-inline images should be wrapped in a page segment
     * @param pSeg true iff images should be wrapped
     */
    public void setWrapPSeg(boolean pSeg) {
        this.pSeg = pSeg;
    }


    /**
     * gets whether images should be FS45
     * @return true iff images should be FS45
     */
    public boolean getFS45() {
        return fs45;
    }

    /**
     * sets whether images should be FS45
     * @param fs45 true iff images should be FS45
     */
    public void setFS45(boolean fs45) {
        this.fs45 = fs45;
    }



    /** {@inheritDoc} */
    @Override
    protected AbstractData instantiateData() {
        return new AFPData();
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractPaintingState instantiate() {
        return new AFPPaintingState();
    }

    /**
     * Returns the painting state of the current page
     *
     * @return the painting state of the current page
     */
    protected AFPPagePaintingState getPagePaintingState() {
        return this.pagePaintingState;
    }

    /**
     * Gets the current page fonts
     *
     * @return the current page fonts
     */
    public AFPPageFonts getPageFonts() {
        return pagePaintingState.getFonts();
    }

    /**
     * Sets the page width
     *
     * @param pageWidth
     *            the page width
     */
    public void setPageWidth(int pageWidth) {
        pagePaintingState.setWidth(pageWidth);
    }

    /**
     * Returns the page width
     *
     * @return the page width
     */
    public int getPageWidth() {
        return pagePaintingState.getWidth();
    }

    /**
     * Sets the page height
     *
     * @param pageHeight
     *            the page height
     */
    public void setPageHeight(int pageHeight) {
        pagePaintingState.setHeight(pageHeight);
    }

    /**
     * Returns the page height
     *
     * @return the page height
     */
    public int getPageHeight() {
        return pagePaintingState.getHeight();
    }

    /**
     * Returns the page rotation
     *
     * @return the page rotation
     */
    public int getPageRotation() {
        return pagePaintingState.getOrientation();
    }

    /**
     * Sets the uri of the current image
     *
     * @param uri
     *            the uri of the current image
     */
    public void setImageUri(String uri) {
        ((AFPData) getData()).imageUri = uri;
    }

    /**
     * Gets the uri of the current image
     *
     * @return the uri of the current image
     */
    public String getImageUri() {
        return ((AFPData) getData()).imageUri;
    }

    /**
     * Returns the currently derived rotation
     *
     * @return the currently derived rotation
     */
    public int getRotation() {
        return getData().getDerivedRotation();
    }

    /**
     * Returns the unit converter
     *
     * @return the unit converter
     */
    public AFPUnitConverter getUnitConverter() {
        return this.unitConv;
    }

    /**
     * Returns a point on the current page, taking the current painting state
     * into account.
     *
     * @param x
     *            the X-coordinate
     * @param y
     *            the Y-coordinate
     * @return a point on the current page
     */
    public Point getPoint(int x, int y) {
        Point p = new Point();
        int rotation = getRotation();
        switch (rotation) {
        case 90:
            p.x = y;
            p.y = getPageWidth() - x;
            break;
        case 180:
            p.x = getPageWidth() - x;
            p.y = getPageHeight() - y;
            break;
        case 270:
            p.x = getPageHeight() - y;
            p.y = x;
            break;
        default:
            p.x = x;
            p.y = y;
            break;
        }
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        AFPPaintingState paintingState = (AFPPaintingState) super.clone();
        paintingState.pagePaintingState = (AFPPagePaintingState) this.pagePaintingState.clone();
        paintingState.portraitRotation = this.portraitRotation;
        paintingState.landscapeRotation = this.landscapeRotation;
        paintingState.bitsPerPixel = this.bitsPerPixel;
        paintingState.colorImages = this.colorImages;
        paintingState.colorConverter = this.colorConverter;
        paintingState.resolution = this.resolution;
        return paintingState;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "AFPPaintingState{" + "portraitRotation=" + portraitRotation
                + ", landscapeRotation=" + landscapeRotation + ", colorImages=" + colorImages
                + ", bitsPerPixel=" + bitsPerPixel + ", resolution=" + resolution + ", pageState="
                + pagePaintingState + super.toString() + "}";
    }

    /**
     * Page level state data
     */
    private class AFPPagePaintingState implements Cloneable {
        /** page width */
        private int width = 0;

        /** page height */
        private int height = 0;

        /** page fonts */
        private AFPPageFonts fonts = new AFPPageFonts();

        /** page font count */
        private int fontCount = 0;

        /** page orientation */
        private int orientation = 0;

        /**
         * Returns the page width
         *
         * @return the page width
         */
        protected int getWidth() {
            return width;
        }

        /**
         * Sets the page width
         *
         * @param width
         *            the page width
         */
        protected void setWidth(int width) {
            this.width = width;
        }

        /**
         * Returns the page height
         *
         * @return the page height
         */
        protected int getHeight() {
            return height;
        }

        /**
         * Sets the page height
         *
         * @param height
         *            the page height
         */
        protected void setHeight(int height) {
            this.height = height;
        }

        /**
         * Returns the page fonts
         *
         * @return the page fonts
         */
        protected AFPPageFonts getFonts() {
            return fonts;
        }

        /**
         * Sets the current page fonts
         *
         * @param fonts
         *            the current page fonts
         */
        protected void setFonts(AFPPageFonts fonts) {
            this.fonts = fonts;
        }

        /**
         * Increments and returns the current page font count
         *
         * @return increment and return the current page font count
         */
        protected int incrementFontCount() {
            return ++fontCount;
        }

        /**
         * Returns the current page orientation
         *
         * @return the current page orientation
         */
        protected int getOrientation() {
            return orientation;
        }

        /**
         * Sets the current page orientation
         *
         * @param orientation
         *            the current page orientation
         */
        protected void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        /** {@inheritDoc} */
        @Override
        public Object clone() {
            AFPPagePaintingState state = new AFPPagePaintingState();
            state.width = this.width;
            state.height = this.height;
            state.orientation = this.orientation;
            state.fonts = new AFPPageFonts(this.fonts);
            state.fontCount = this.fontCount;
            return state;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "AFPPagePaintingState{width=" + width + ", height=" + height + ", orientation="
                    + orientation + ", fonts=" + fonts + ", fontCount=" + fontCount + "}";
        }
    }

    /**
     * Block level state data
     */
    private class AFPData extends org.apache.fop.util.AbstractPaintingState.AbstractData {
        private static final long serialVersionUID = -1789481244175275686L;

        /** The current fill status */
        private boolean filled = false;

        private String imageUri = null;

        /** {@inheritDoc} */
        @Override
        public Object clone() {
            AFPData obj = (AFPData) super.clone();
            obj.filled = this.filled;
            obj.imageUri = this.imageUri;
            return obj;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "AFPData{" + super.toString() + ", filled=" + filled + ", imageUri=" + imageUri
                    + "}";
        }

        /** {@inheritDoc} */
        @Override
        protected AbstractData instantiate() {
            return new AFPData();
        }
    }

}
