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

import java.awt.image.BufferedImage;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;

/**
 * TIFF Renderer configurator 
 */
public class TIFFRendererConfigurator extends PrintRendererConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public TIFFRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Configure the TIFF renderer. Get the configuration to be used for
     * compression
     * @param renderer tiff renderer
     * @throws FOPException fop exception
     * {@inheritDoc}
     */
    public void configure(Renderer renderer) throws FOPException {        
        Configuration cfg = super.getRendererConfig(renderer);
        if (cfg != null) {
            TIFFRenderer tiffRenderer = (TIFFRenderer)renderer;
            //set compression
            String name = cfg.getChild("compression").getValue(TIFFRenderer.COMPRESSION_PACKBITS);
            //Some compression formats need a special image format:
            if (name.equalsIgnoreCase(TIFFRenderer.COMPRESSION_CCITT_T6)) {
                tiffRenderer.setBufferedImageType(BufferedImage.TYPE_BYTE_BINARY);
            } else if (name.equalsIgnoreCase(TIFFRenderer.COMPRESSION_CCITT_T4)) {
                tiffRenderer.setBufferedImageType(BufferedImage.TYPE_BYTE_BINARY);
            } else {
                tiffRenderer.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
            }
            if (!"NONE".equalsIgnoreCase(name)) {
                tiffRenderer.getWriterParams().setCompressionMethod(name);
            }
            if (log.isInfoEnabled()) {
                log.info("TIFF compression set to " + name);
            }
        }
        super.configure(renderer);
    }
}
