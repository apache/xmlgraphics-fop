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

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig.RendererConfigParser;

/**
 * TXT Renderer configurator
 */
public class TXTRendererConfigurator extends DefaultRendererConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public TXTRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    /**
     * Configure the PS renderer.
     * @param renderer TXT renderer
     * @throws FOPException fop exception
     */
    public void configure(Renderer renderer) throws FOPException {
        TxtRendererConfig config = (TxtRendererConfig) getRendererConfig(renderer);
        if (config != null) {
            TXTRenderer txtRenderer = (TXTRenderer) renderer;
            txtRenderer.setEncoding(config.getEncoding());
        }
    }

}
