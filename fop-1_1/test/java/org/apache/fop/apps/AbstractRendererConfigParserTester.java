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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.events.DefaultEventBroadcaster;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfig.RendererConfigParser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractRendererConfigParserTester<B extends RendererConfBuilder,
        C extends RendererConfig> {

    protected B builder;

    protected C conf;

    protected final RendererConfigParser configBuilder;

    private final Class<B> type;

    public AbstractRendererConfigParserTester(RendererConfigParser configBuilder, Class<B> type) {
        this.configBuilder = configBuilder;
        this.type = type;
    }

    protected void parseConfig() throws Exception {
        parseConfig(createRenderer());
    }

    protected void parseConfig(B rendererConfBuilder) throws Exception {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(rendererConfBuilder.endRendererConfig().build())
                .getChild("renderers")
                .getChild("renderer");
        FOUserAgent userAgent = mock(FOUserAgent.class);
        when(userAgent.validateStrictly()).thenReturn(true);
        FontManager fontManager = mock(FontManager.class);
        when(userAgent.getFontManager()).thenReturn(fontManager);
        when(userAgent.getEventBroadcaster()).thenReturn(new DefaultEventBroadcaster());
        conf = (C) configBuilder.build(userAgent, cfg);
    }

    protected B createRenderer() {
        return createRenderer(type);
    }

    protected B createRenderer(Class<B> type) {
        builder = new FopConfBuilder().setStrictValidation(true).startRendererConfig(type);
        return builder;
    }

    protected void dump() throws Exception {
        builder.dump();
    }
}
