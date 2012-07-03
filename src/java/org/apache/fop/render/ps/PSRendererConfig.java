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

package org.apache.fop.render.ps;

import java.util.EnumMap;
import java.util.Locale;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.util.LogUtil;

import static org.apache.fop.render.ps.PSRendererOption.AUTO_ROTATE_LANDSCAPE;
import static org.apache.fop.render.ps.PSRendererOption.DSC_COMPLIANT;
import static org.apache.fop.render.ps.PSRendererOption.LANGUAGE_LEVEL;
import static org.apache.fop.render.ps.PSRendererOption.OPTIMIZE_RESOURCES;
import static org.apache.fop.render.ps.PSRendererOption.RENDERING_MODE;
import static org.apache.fop.render.ps.PSRendererOption.SAFE_SET_PAGE_DEVICE;

/**
 * The PostScript renderer configuration data object.
 */
public final class PSRendererConfig implements RendererConfig {

    private final EnumMap<PSRendererOption, Object> params
            = new EnumMap<PSRendererOption, Object>(PSRendererOption.class);

    private final DefaultFontConfig fontConfig;

    private PSRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public Boolean isAutoRotateLandscape() {
        return (Boolean) params.get(AUTO_ROTATE_LANDSCAPE);
    }

    public Integer getLanguageLevel() {
        return (Integer) params.get(LANGUAGE_LEVEL);
    }

    public Boolean isOptimizeResources() {
        return (Boolean) params.get(OPTIMIZE_RESOURCES);
    }

    public Boolean isSafeSetPageDevice() {
        return (Boolean) params.get(SAFE_SET_PAGE_DEVICE);
    }

    public Boolean isDscComplianceEnabled() {
        return (Boolean) params.get(DSC_COMPLIANT);
    }

    public PSRenderingMode getRenderingMode() {
        return (PSRenderingMode) params.get(RENDERING_MODE);
    }

    /**
     * The PostScript renderer configuration data parser.
     */
    public static final class PSRendererConfigParser implements RendererConfigParser {

        private static final Log LOG = LogFactory.getLog(PSRendererConfigParser.class);

        public PSRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            PSRendererConfig config = null;
            try {
                config = new ParserHelper(cfg, userAgent).config;
            } catch (ConfigurationException e) {
                LogUtil.handleException(LOG, e, false);
            }
            return config;
        }

        public String getMimeType() {
            return MimeConstants.MIME_POSTSCRIPT;
        }
    }

    private static final class ParserHelper {

        private PSRendererConfig config;

        private ParserHelper(Configuration cfg, FOUserAgent userAgent)
                throws ConfigurationException, FOPException {
            config = new PSRendererConfig(new DefaultFontConfigParser().parse(cfg,
                    userAgent.validateStrictly()));
            if (cfg != null) {
                setBoolConfigParam(cfg, AUTO_ROTATE_LANDSCAPE);
                setConfigParameter(LANGUAGE_LEVEL,
                        cfg.getChild(LANGUAGE_LEVEL.getName())
                           .getValueAsInteger((Integer) LANGUAGE_LEVEL.getDefaultValue()));
                setBoolConfigParam(cfg, OPTIMIZE_RESOURCES);
                setBoolConfigParam(cfg, SAFE_SET_PAGE_DEVICE);
                setBoolConfigParam(cfg, DSC_COMPLIANT);
                Configuration child = cfg.getChild("rendering");
                if (child != null) {
                    config.params.put(RENDERING_MODE,
                            PSRenderingMode.valueOf(child.getValue(
                                                   RENDERING_MODE.getDefaultValue().toString())
                                                .toUpperCase(Locale.ENGLISH)));
                }
            }
        }

        private void setConfigParameter(PSRendererOption option,
                Object value) {
            config.params.put(option, value != null ? value : option.getDefaultValue());
        }

        private void setBoolConfigParam(Configuration cfg, PSRendererOption option) {
            setConfigParameter(option, cfg.getChild(
                    option.getName()).getValueAsBoolean((Boolean) option.getDefaultValue()));
        }

    }

}
