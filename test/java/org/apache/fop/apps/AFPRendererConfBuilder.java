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

package org.apache.fop.apps;


import java.util.Map;

import org.w3c.dom.Element;

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.render.afp.AFPRendererConfig;
import org.apache.fop.render.afp.AFPRendererConfig.Options;
import org.apache.fop.render.afp.AFPShadingMode;

import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.DEFAULT_RESOURCE_LEVELS;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.IMAGES;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.IMAGES_DITHERING_QUALITY;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.IMAGES_MODE;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.IMAGES_NATIVE;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.RENDERER_RESOLUTION;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.LINE_WIDTH_CORRECTION;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.RESOURCE_GROUP_FILE;
import static org.apache.fop.render.afp.AFPRendererConfig.Options.SHADING;

/**
 * A config builder specific to a particular renderer for specific MIME type.
 */
public final class AFPRendererConfBuilder extends RendererConfBuilder {

    private ImagesBuilder images;

    public AFPRendererConfBuilder() {
        super(MimeConstants.MIME_AFP);
    }

    private AFPRendererConfBuilder createTextElement(Options option, String value) {
        createTextElement(option.getName(), value);
        return this;
    }

    public AFPRendererConfBuilder setShading(AFPShadingMode mode) {
        return createTextElement(SHADING, mode.getName());
    }

    public AFPRendererConfBuilder setRenderingResolution(int res) {
        return createTextElement(RENDERER_RESOLUTION, String.valueOf(res));
    }

    public AFPRendererConfBuilder setLineWidthCorrection(float value) {
        return createTextElement(LINE_WIDTH_CORRECTION, String.valueOf(value));
    }

    public ImagesBuilder startImages(AFPRendererConfig.ImagesModeOptions mode) {
        images = new ImagesBuilder(mode);
        return images;
    }

    public ImagesBuilder startImages() {
        return startImages(MODE_GRAYSCALE);
    }

    public AFPRendererConfBuilder endImages() {
        images = null;
        return this;
    }

    public AFPRendererConfBuilder setResourceGroupFile(String value) {
        createTextElement(RESOURCE_GROUP_FILE, value);
        return this;
    }

    public AFPRendererConfBuilder setResourceResourceLevels(Map<String, String> levels) {
        Element e = createElement(DEFAULT_RESOURCE_LEVELS.getName());
        for (String key : levels.keySet()) {
            e.setAttribute(key, levels.get(key));
        }
        return this;
    }

    public final class ImagesBuilder {

        private final Element el;

        private ImagesBuilder(AFPRendererConfig.ImagesModeOptions mode) {
            el = createElement(IMAGES.getName());
            setAttribute(IMAGES_MODE, mode.getName());
        }

        public ImagesBuilder setModeAttribute(String name, String value) {
            return setAttribute(name, value);
        }

        public ImagesBuilder setDitheringQuality(String value) {
            return setAttribute(IMAGES_DITHERING_QUALITY, value);
        }

        public ImagesBuilder setDitheringQuality(float value) {
            return setAttribute(IMAGES_DITHERING_QUALITY, String.valueOf(value));
        }

        public ImagesBuilder setNativeImageSupport(boolean value) {
            return setAttribute(IMAGES_NATIVE, String.valueOf(value));
        }

        public AFPRendererConfBuilder endImages() {
            return AFPRendererConfBuilder.this.endImages();
        }

        private ImagesBuilder setAttribute(Options options, String value) {
            return setAttribute(options.getName(), value);
        }

        private ImagesBuilder setAttribute(String name, String value) {
            el.setAttribute(name, value);
            return this;
        }
    }
}
