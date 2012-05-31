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

package org.apache.fop.render.java2d;

import java.util.EnumMap;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.render.RendererConfig;

import static org.apache.fop.render.java2d.Java2DRendererOptions.JAVA2D_TRANSPARENT_PAGE_BACKGROUND;

public final class Java2DRendererConfig implements RendererConfig {

    private final EnumMap<Java2DRendererOptions, Object> params
            = new EnumMap<Java2DRendererOptions, Object>(Java2DRendererOptions.class);

    private final DefaultFontConfig fontConfig;

    private Java2DRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public Boolean isPageBackgroundTransparent() {
        return Boolean.class.cast(params.get(JAVA2D_TRANSPARENT_PAGE_BACKGROUND));
    }

    public static class Java2DRendererConfigParser implements RendererConfigParser {

        private final String mimeType;

        public Java2DRendererConfigParser(String mimeType) {
            this.mimeType = mimeType;
        }

        public Java2DRendererConfig build(FOUserAgent userAgent, Configuration cfg)
                throws FOPException {
            Java2DRendererConfig config = new Java2DRendererConfig(new DefaultFontConfigParser()
                    .parse(cfg, userAgent.validateStrictly()));
            boolean value = cfg.getChild(
                    JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getName(), true).getValueAsBoolean(false);
            config.params.put(JAVA2D_TRANSPARENT_PAGE_BACKGROUND, value);
            return config;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
