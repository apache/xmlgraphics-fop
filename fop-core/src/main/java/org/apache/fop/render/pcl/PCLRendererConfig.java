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

package org.apache.fop.render.pcl;

import java.util.EnumMap;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;

import static org.apache.fop.render.pcl.Java2DRendererOption.DISABLE_PJL;
import static org.apache.fop.render.pcl.Java2DRendererOption.MODE_COLOR;
import static org.apache.fop.render.pcl.Java2DRendererOption.OPTIMIZE_RESOURCES;
import static org.apache.fop.render.pcl.Java2DRendererOption.RENDERING_MODE;
import static org.apache.fop.render.pcl.Java2DRendererOption.TEXT_RENDERING;

/**
 * The PCL renderer configuration data object.
 */
public final class PCLRendererConfig implements RendererConfig {

    private final Map<Java2DRendererOption, Object> params
        = new EnumMap<Java2DRendererOption, Object>(Java2DRendererOption.class);

    private final DefaultFontConfig fontConfig;

    private PCLRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public PCLRenderingMode getRenderingMode() {
        return getParam(RENDERING_MODE, PCLRenderingMode.class);
    }

    public Boolean isTextRendering() {
        return getParam(TEXT_RENDERING, Boolean.class);
    }

    public Boolean isDisablePjl() {
        return getParam(DISABLE_PJL, Boolean.class);
    }

    public Boolean isColorEnabled() {
        return getParam(MODE_COLOR, Boolean.class);
    }

    public Boolean isOptimizeResources() {
        return getParam(OPTIMIZE_RESOURCES, Boolean.class);
    }

    private <T> T getParam(Java2DRendererOption option, Class<T> type) {
        assert option.getType().equals(type);
        return type.cast(params.get(option));
    }

    private <T> void setParam(Java2DRendererOption option, T value) {
        assert option.getType().isInstance(value);
        params.put(option, value);
    }

    /**
     * The PCL renderer configuration data parser.
     */
    public static final class PCLRendererConfigParser implements RendererConfigParser {

        /** {@inheritDoc} */
        public PCLRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            PCLRendererConfig config = new PCLRendererConfig(new DefaultFontConfigParser()
                    .parse(cfg, userAgent.validateStrictly(),
                            new FontEventAdapter(userAgent.getEventBroadcaster())));
            configure(cfg, config);
            return config;
        }

        private void configure(Configuration cfg, PCLRendererConfig config) throws FOPException {
            if (cfg != null) {
                Configuration imagesCfg = cfg.getChild("images");
                String imageMode = imagesCfg.getAttribute("mode", null);
                if ("color".equalsIgnoreCase(imageMode)) {
                    config.setParam(MODE_COLOR, true);
                }

                String rendering = cfg.getChild(RENDERING_MODE.getName()).getValue(null);
                if (rendering != null) {
                    try {
                        config.setParam(RENDERING_MODE, PCLRenderingMode.getValueOf(rendering));
                    } catch (IllegalArgumentException e) {
                        throw new FOPException("Valid values for 'rendering' are 'quality', 'speed' and 'bitmap'."
                                + " Value found: " + rendering);
                    }
                }
                String textRendering = cfg.getChild(TEXT_RENDERING.getName()).getValue(null);
                if ("bitmap".equalsIgnoreCase(textRendering)) {
                    config.setParam(TEXT_RENDERING, true);
                } else if (textRendering == null || "auto".equalsIgnoreCase(textRendering)) {
                    config.setParam(TEXT_RENDERING, false);
                } else {
                    throw new FOPException(
                            "Valid values for 'text-rendering' are 'auto' and 'bitmap'. Value found: "
                                    + textRendering);
                }
                config.setParam(DISABLE_PJL,
                        cfg.getChild(DISABLE_PJL.getName()).getValueAsBoolean(false));
                config.setParam(OPTIMIZE_RESOURCES,
                        cfg.getChild(OPTIMIZE_RESOURCES.getName()).getValueAsBoolean(false));
            }
        }

        /** {@inheritDoc} */
        public String getMimeType() {
            return MimeConstants.MIME_PCL;
        }
    }

}
