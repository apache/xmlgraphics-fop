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

import java.util.EnumMap;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.render.RendererConfigOption;

/**
 * The renderer configuration object for the TIFF renderer.
 */
public final class TIFFRendererConfig extends BitmapRendererConfig {

    public enum TIFFRendererOption implements RendererConfigOption {
        COMPRESSION("compression", TIFFCompressionValue.PACKBITS);

        private final String name;
        private final Object defaultValue;

        private TIFFRendererOption(String name, Object defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    private final EnumMap<TIFFRendererOption, Object> params
            = new EnumMap<TIFFRendererOption, Object>(TIFFRendererOption.class);

    private TIFFRendererConfig(DefaultFontConfig fontConfig) {
        super(fontConfig);
    }

    public TIFFCompressionValue getCompressionType() {
        return (TIFFCompressionValue) params.get(TIFFRendererOption.COMPRESSION);
    }

    /**
     * The TIFF renderer configuration parser.
     */
    public static final class TIFFRendererConfigParser extends BitmapRendererConfigParser {

        public TIFFRendererConfigParser() {
            super(MimeConstants.MIME_TIFF);
        }

        private TIFFRendererConfig config;

        private void setParam(TIFFRendererOption option, Object value) {
            config.params.put(option, value != null ? value : option.getDefaultValue());
        }

        private String getValue(Configuration cfg, TIFFRendererOption option) {
            return cfg.getChild(option.getName()).getValue(null);
        }

        /** {@inheritDoc} */
        public TIFFRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            config = new TIFFRendererConfig(new DefaultFontConfigParser()
                    .parse(cfg, userAgent.validateStrictly()));
            super.build(config, userAgent, cfg);
            if (cfg != null) {
                setParam(TIFFRendererOption.COMPRESSION,
                        TIFFCompressionValue.getType(getValue(cfg, TIFFRendererOption.COMPRESSION)));
            }
            return config;
        }
    }
}
