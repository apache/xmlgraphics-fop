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

package org.apache.fop.render.bitmap;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;

import org.apache.fop.render.java2d.Java2DRenderingSettings;

import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.ANTI_ALIASING;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.COLOR_MODE;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.RENDERING_QUALITY;

/**
 * This class holds settings used when rendering to bitmaps.
 */
public class BitmapRenderingSettings extends Java2DRenderingSettings {

    /** ImageWriter parameters */
    private ImageWriterParams writerParams;

    /** Image Type as parameter for the BufferedImage constructor (see BufferedImage.TYPE_*) */
    private int bufferedImageType = (Integer) COLOR_MODE.getDefaultValue();

    /** true if anti-aliasing is set */
    private boolean antialiasing = (Boolean) ANTI_ALIASING.getDefaultValue();

    /** true if qualityRendering is set */
    private boolean qualityRendering = (Boolean) RENDERING_QUALITY.getDefaultValue();

    /**
     * Default constructor. Initializes the settings to their default values.
     */
    public BitmapRenderingSettings() {
        writerParams = new ImageWriterParams();
    }

    /**
     * Returns the image writer parameters used for encoding the bitmap images.
     * @return the image writer parameters
     */
    public ImageWriterParams getWriterParams() {
        return this.writerParams;
    }

    /**
     * Returns the BufferedImage type.
     * @return one of BufferedImage.TYPE_*
     */
    public int getBufferedImageType() {
        return this.bufferedImageType;
    }

    /**
     * Sets the type of the BufferedImage to use when preparing a new instance.
     * @param bufferedImageType a BufferImage.TYPE_* value
     */
    public void setBufferedImageType(int bufferedImageType) {
        this.bufferedImageType = bufferedImageType;
    }

    /**
     * Enables or disables anti-aliasing.
     * @param value true to enable anti-aliasing
     */
    public void setAntiAliasing(boolean value) {
        this.antialiasing = value;
    }

    /**
     * Indicates whether anti-aliasing is enabled.
     * @return true if anti-aliasing is enabled
     */
    public boolean isAntiAliasingEnabled() {
        return this.antialiasing;
    }

    /**
     * Controls whether to optimize rendering for speed or for quality.
     * @param quality true to optimize for quality, false to optimize for speed
     */
    public void setQualityRendering(boolean quality) {
        this.qualityRendering = quality;
    }

    /**
     * Indicates whether quality rendering is enabled.
     * @return true indicates optimization for quality, false indicates optimization for speed
     */
    public boolean isQualityRenderingEnabled() {
        return this.qualityRendering;
    }

}
