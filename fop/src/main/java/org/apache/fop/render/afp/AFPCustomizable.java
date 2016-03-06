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

import java.net.URI;

import org.apache.fop.afp.AFPResourceLevelDefaults;

/**
 * Interface used to customize the AFP renderer or document handler.
 */
public interface AFPCustomizable {

    /**
     * Sets the number of bits used per pixel
     *
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    void setBitsPerPixel(int bitsPerPixel);

    /**
     * Sets whether images are color or not
     *
     * @param colorImages
     *            color image output
     */
    void setColorImages(boolean colorImages);

    /**
     * Sets whether images are supported natively or not
     *
     * @param nativeImages
     *            native image support
     */
    void setNativeImagesSupported(boolean nativeImages);

    /**
     * Controls whether CMYK images (IOCA FS45) are enabled. By default, support is disabled
     * for wider compatibility. When disabled, any CMYK image is converted to the selected
     * color format.
     * @param value true to enabled CMYK images
     */
    void setCMYKImagesSupported(boolean value);

    /**
     * Sets the shading mode for painting filled rectangles.
     * @param shadingMode the shading mode
     */
    void setShadingMode(AFPShadingMode shadingMode);

    /**
     * Sets the dithering quality setting to use when converting images to monochrome images.
     * @param quality Defines the desired quality level for the conversion.
     *                  Valid values: a value between 0.0f (fastest) and 1.0f (best)
     */
    void setDitheringQuality(float quality);

    /**
     * Sets the image encoding quality setting to use when encoding bitmap images.
     * The default setting is 1.0 which means loss-less encoding. Settings of less than 1.0
     * allow loss-less encoding schemes like JPEG. The value serves as quality setting for
     * the encoders in that case.
     * @param quality Defines the desired quality level.
     *                  Valid values: a value between 0.0f (lowest) and 1.0f (best, loss-less)
     */
    void setBitmapEncodingQuality(float quality);

    /**
     * Sets the output/device resolution
     *
     * @param resolution
     *            the output resolution (dpi)
     */
    void setResolution(int resolution);

    /**
     * Sets the line width correction
     *
     * @param correction the line width multiplying factor correction
     */
    void setLineWidthCorrection(float correction);

    /**
     * Sets whether FS11 and FS45 non-inline images should be wrapped in a page segment
     * @param pSeg true iff images should be wrapped
     */
    void setWrapPSeg(boolean pSeg);

    /**
     * set true if images should be FS45
     * @param fs45 true iff images should be FS45
     */
    void setFS45(boolean fs45);

    /**
     * gets whether FS11 and FS45 non-inline images should be wrapped in a page segment
     * @return true iff images should be wrapped
     */
    boolean getWrapPSeg();

    /**
     * gets whether images should be FS45
     * @return true iff images should be FS45
     */
    boolean getFS45();

    /**
     * Returns the output/device resolution.
     *
     * @return the resolution in dpi
     */
    int getResolution();

    /**
     * Controls whether GOCA is enabled or disabled.
     * @param enabled true if GOCA is enabled, false if it is disabled
     */
     void setGOCAEnabled(boolean enabled);

    /**
     * Indicates whether GOCA is enabled or disabled.
     * @return true if GOCA is enabled, false if GOCA is disabled
     */
    boolean isGOCAEnabled();

    /**
     * Controls whether to stroke text in GOCA mode or to use text operators where possible.
     * @param stroke true to stroke, false to paint with text operators where possible
     */
    void setStrokeGOCAText(boolean stroke);

    /**
     * Indicates whether to stroke text in GOCA mode or to use text operators where possible.
     * @return true to stroke, false to paint with text operators where possible
     */
    boolean isStrokeGOCAText();

    /**
     * Sets the default resource group URI
     * @param uri the default resource group URI
     */
    void setDefaultResourceGroupUri(URI uri);

    /**
     * Sets the resource level defaults. The object passed in provides information which resource
     * level shall be used by default for various kinds of resources.
     * @param defaults the resource level defaults
     */
    void setResourceLevelDefaults(AFPResourceLevelDefaults defaults);

    /**
     * Sets whether or not to JPEG images can be embedded in the AFP document.
     *
     * @param canEmbed whether or not to embed JPEG image
     */
    void canEmbedJpeg(boolean canEmbed);

}
