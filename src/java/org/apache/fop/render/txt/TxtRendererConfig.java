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

package org.apache.fop.render.txt;

import java.util.EnumMap;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfigOption;

/**
 * The Text renderer configuration data object.
 */
public final class TxtRendererConfig implements RendererConfig {

    public enum TxtRendererOption implements RendererConfigOption {
        ENCODING("encoding", "UTF-8");

        private final String name;
        private final Object defaultValue;

        private TxtRendererOption(String name, Object defaultValue) {
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

    private final EnumMap<TxtRendererOption, Object> params
            = new EnumMap<TxtRendererOption, Object>(TxtRendererOption.class);

    private final DefaultFontConfig fontConfig;

    private TxtRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public String getEncoding() {
        return (String) params.get(TxtRendererOption.ENCODING);
    }

    /**
     * The Text renderer configuration data parser.
     */
    public static final class TxtRendererConfigParser implements RendererConfigParser {

        /** {@inheritDoc} */
        public TxtRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            TxtRendererConfig config = new TxtRendererConfig(new DefaultFontConfigParser().parse(cfg,
                    userAgent.validateStrictly()));
            if (cfg != null) {
                TxtRendererOption option = TxtRendererOption.ENCODING;
                String value = cfg.getChild(option.getName(), true).getValue(null);
                config.params.put(option, value != null ? value : option.getDefaultValue());
            }
            return config;
        }

        /** {@inheritDoc} */
        public String getMimeType() {
            return MimeConstants.MIME_PLAIN_TEXT;
        }
    }

}
