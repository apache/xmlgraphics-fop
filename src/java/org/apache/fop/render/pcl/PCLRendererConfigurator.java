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

/* $Id: $ */

package org.apache.fop.render.pcl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;

/**
 * PCL Renderer configurator 
 */
public class PCLRendererConfigurator extends PrintRendererConfigurator {
    
    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PCLRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Configure the TIFF renderer. Get the configuration to be used for
     * compression
     * @param renderer PCL renderer
     * @throws FOPException fop exception
     * @see org.apache.fop.render.PrintRendererConfigurator#configure(Renderer)
     */
    public void configure(Renderer renderer) throws FOPException {
        Configuration cfg = super.getRendererConfig(renderer);
        if (cfg != null) {
            PCLRenderer pclRenderer = (PCLRenderer)renderer;
            String rendering = cfg.getChild("rendering").getValue(null);
            if ("quality".equalsIgnoreCase(rendering)) {
                pclRenderer.setQualityBeforeSpeed(true);
            } else if ("speed".equalsIgnoreCase(rendering)) {
                pclRenderer.setQualityBeforeSpeed(false);
            } else if (rendering != null) {
                throw new FOPException(
                        "Valid values for 'rendering' are 'quality' and 'speed'. Value found: " 
                            + rendering);
            }
            String textRendering = cfg.getChild("text-rendering").getValue(null);
            if ("bitmap".equalsIgnoreCase(textRendering)) {
                pclRenderer.setAllTextAsBitmaps(true);
            } else if ("auto".equalsIgnoreCase(textRendering)) {
                pclRenderer.setAllTextAsBitmaps(false);
            } else if (textRendering != null) {
                throw new FOPException(
                        "Valid values for 'text-rendering' are 'auto' and 'bitmap'. Value found: " 
                            + textRendering);
            }
        }
    }
}
