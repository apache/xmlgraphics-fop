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
import org.apache.fop.render.afp.AFPRendererOption;
import org.apache.fop.render.afp.AFPShadingMode;

import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE;
import static org.apache.fop.render.afp.AFPRendererOption.DEFAULT_RESOURCE_LEVELS;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_DITHERING_QUALITY;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_FS45;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_JPEG;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_MAPPING_OPTION;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_MODE;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_NATIVE;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_WRAP_PSEG;
import static org.apache.fop.render.afp.AFPRendererOption.JPEG_ALLOW_JPEG_EMBEDDING;
import static org.apache.fop.render.afp.AFPRendererOption.JPEG_BITMAP_ENCODING_QUALITY;
import static org.apache.fop.render.afp.AFPRendererOption.LINE_WIDTH_CORRECTION;
import static org.apache.fop.render.afp.AFPRendererOption.RENDERER_RESOLUTION;
import static org.apache.fop.render.afp.AFPRendererOption.RESOURCE_GROUP_URI;
import static org.apache.fop.render.afp.AFPRendererOption.SHADING;

/**
 * A config builder specific to a particular renderer for specific MIME type.
 */
public final class AFPRendererConfBuilder extends RendererConfBuilder {

    private ImagesBuilder images;

    public AFPRendererConfBuilder() {
        super(MimeConstants.MIME_AFP);
    }

    private AFPRendererConfBuilder createTextElement(AFPRendererOption option, String value) {
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

    public AFPRendererConfBuilder setResourceGroupUri(String uri) {
        createTextElement(RESOURCE_GROUP_URI, uri);
        return this;
    }

    public AFPRendererConfBuilder setDefaultResourceLevels(Map<String, String> levels) {
        Element e = createElement(DEFAULT_RESOURCE_LEVELS.getName());
        for (String key : levels.keySet()) {
            e.setAttribute(key, levels.get(key));
        }
        return this;
    }

    public final class ImagesBuilder {

        private final Element el;

        private Element jpeg;

        private ImagesBuilder(AFPRendererConfig.ImagesModeOptions mode) {
            el = createElement(IMAGES.getName());
            setAttribute(IMAGES_MODE, mode.getName());
        }

        public ImagesBuilder setModeAttribute(String name, String value) {
            return setAttribute(name, value);
        }

        public ImagesBuilder setAllowJpegEmbedding(boolean value) {
            getJpeg().setAttribute(JPEG_ALLOW_JPEG_EMBEDDING.getName(), String.valueOf(value));
            return this;
        }

        public ImagesBuilder setBitmapEncodingQuality(float value) {
            getJpeg().setAttribute(JPEG_BITMAP_ENCODING_QUALITY.getName(), String.valueOf(value));
            return this;
        }

        public ImagesBuilder setDitheringQuality(String value) {
            return setAttribute(IMAGES_DITHERING_QUALITY, value);
        }

        public ImagesBuilder setDitheringQuality(float value) {
            return setAttribute(IMAGES_DITHERING_QUALITY, value);
        }

        public ImagesBuilder setFs45(boolean value) {
            return setAttribute(IMAGES_FS45, value);
        }

        public ImagesBuilder setMappingOption(String value) {
            return setAttribute(IMAGES_MAPPING_OPTION, value);
        }

        public ImagesBuilder setWrapPseg(boolean value) {
            return setAttribute(IMAGES_WRAP_PSEG, value);
        }

        public ImagesBuilder setNativeImageSupport(boolean value) {
            return setAttribute(IMAGES_NATIVE, value);
        }

        public AFPRendererConfBuilder endImages() {
            return AFPRendererConfBuilder.this.endImages();
        }

        private ImagesBuilder setAttribute(AFPRendererOption options, String value) {
            return setAttribute(options.getName(), value);
        }

        private ImagesBuilder setAttribute(AFPRendererOption options, Object value) {
            return setAttribute(options.getName(), value);
        }

        private ImagesBuilder setAttribute(String name, Object value) {
            el.setAttribute(name, String.valueOf(value));
            return this;
        }

        private Element getJpeg() {
            if (jpeg == null) {
                jpeg = createElement(IMAGES_JPEG.getName(), el);
            }
            return jpeg;
        }
    }
}
