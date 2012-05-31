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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.java2d.Java2DRendererConfig;
import org.apache.fop.render.java2d.Java2DRendererConfig.Java2DRendererConfigParser;
import org.apache.fop.util.ColorUtil;

import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.ANTI_ALIASING;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.BACKGROUND_COLOR;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.COLOR_MODE;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.JAVA2D_TRANSPARENT_PAGE_BACKGROUND;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.RENDERING_QUALITY;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.RENDERING_QUALITY_ELEMENT;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOptions.RENDERING_SPEED;

public class BitmapRendererConfig implements RendererConfig {

    private final EnumMap<BitmapRendererConfigOptions, Object> params
            = new EnumMap<BitmapRendererConfigOptions, Object>(BitmapRendererConfigOptions.class);

    private final DefaultFontConfig fontConfig;

    BitmapRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public Color getBackgroundColor() {
        return (Color) get(BACKGROUND_COLOR);
    }

    public Boolean hasAntiAliasing() {
        return (Boolean) get(ANTI_ALIASING);
    }

    public Boolean isRenderHighQuality() {
        return (Boolean) get(RENDERING_QUALITY);
    }

    public Integer getColorMode() {
        return (Integer) get(COLOR_MODE);
    }

    public boolean hasTransparentBackround() {
        Object result = get(JAVA2D_TRANSPARENT_PAGE_BACKGROUND);
        return (Boolean) (result != null ? result
                : JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getDefaultValue());
    }

    private Object get(BitmapRendererConfigOptions option) {
        return params.get(option);
    }

    public static class BitmapRendererConfigParser implements RendererConfigParser {

        private final String mimeType;

        public BitmapRendererConfigParser(String mimeType) {
            this.mimeType = mimeType;
        }

        private void setParam(BitmapRendererConfig config, BitmapRendererConfigOptions option,
                Object value) {
            config.params.put(option, value != null ? value : option.getDefaultValue());
        }

        protected void build(BitmapRendererConfig config, FOUserAgent userAgent,
                Configuration cfg) throws FOPException {
            if (cfg != null) {
                Java2DRendererConfig j2dConfig = new Java2DRendererConfigParser(null).build(
                        userAgent, cfg);
                Boolean isTransparent = j2dConfig.isPageBackgroundTransparent();
                isTransparent = isTransparent == null
                        ? (Boolean) JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getDefaultValue()
                        : isTransparent;
                setParam(config, JAVA2D_TRANSPARENT_PAGE_BACKGROUND, isTransparent);

                String background = getValue(cfg, BACKGROUND_COLOR);
                if (isTransparent) {
                    // We don't use setParam here because we want to force a null value
                    config.params.put(BACKGROUND_COLOR, null);
                } else {
                    setParam(config, BACKGROUND_COLOR,
                            ColorUtil.parseColorString(userAgent, background));
                }

                setParam(config, BitmapRendererConfigOptions.ANTI_ALIASING,
                        getChild(cfg, ANTI_ALIASING).getValueAsBoolean(
                                (Boolean) ANTI_ALIASING.getDefaultValue()));

                String optimization = getValue(cfg, RENDERING_QUALITY_ELEMENT);
                setParam(config, RENDERING_QUALITY,
                        !(BitmapRendererConfigOptions.getValue(optimization) == RENDERING_SPEED));

                String color = getValue(cfg, COLOR_MODE);
                setParam(config, COLOR_MODE,
                        getBufferedImageIntegerFromColor(BitmapRendererConfigOptions.getValue(color)));
            }
        }

        public BitmapRendererConfig build(FOUserAgent userAgent, Configuration cfg)
                throws FOPException {
            BitmapRendererConfig config = new BitmapRendererConfig(new DefaultFontConfigParser()
                    .parse(cfg, userAgent.validateStrictly()));
            build(config, userAgent, cfg);
            return config;
        }

        private Integer getBufferedImageIntegerFromColor(BitmapRendererConfigOptions option) {
            if (option == null) {
                return null;
            }
            switch (option) {
            case COLOR_MODE_RGBA:
                return BufferedImage.TYPE_INT_ARGB;
            case COLOR_MODE_RGB:
                return BufferedImage.TYPE_INT_RGB;
            case COLOR_MODE_GRAY:
                return BufferedImage.TYPE_BYTE_GRAY;
            case COLOR_MODE_BINARY:
            case COLOR_MODE_BILEVEL:
                return BufferedImage.TYPE_BYTE_BINARY;
            default:
                return null;
            }
        }

        private Configuration getChild(Configuration cfg, BitmapRendererConfigOptions option) {
            return cfg.getChild(option.getName());
        }

        private String getValue(Configuration cfg, BitmapRendererConfigOptions option) {
            return cfg.getChild(option.getName()).getValue(null);
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
