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

package org.apache.fop.render.intermediate;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.fonts.FontConfig;
import org.apache.fop.render.RendererConfig;

/**
 * The Intermediate Format renderer configuration data object.
 */
public final class IFRendererConfig implements RendererConfig {

    private final DefaultFontConfig fontConfig;

    private IFRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public FontConfig getFontInfoConfig() {
        return fontConfig;
    }

    /**
     * The Intermediate Format configuration data parser.
     */
    public static final class IFRendererConfigParser implements RendererConfigParser {

        /** {@inheritDoc} */
        public RendererConfig build(FOUserAgent userAgent, Configuration cfg)
                throws FOPException {
            return new IFRendererConfig(new DefaultFontConfigParser().parse(cfg,
                    userAgent.validateStrictly()));
        }

        /** {@inheritDoc} */
        public String getMimeType() {
            return "application/X-fop-intermediate-format";
        }
    }
}
