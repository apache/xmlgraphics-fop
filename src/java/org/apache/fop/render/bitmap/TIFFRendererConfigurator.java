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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.bitmap.TIFFRendererConfig.TIFFRendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;

import static org.apache.fop.render.bitmap.TIFFCompressionValues.CCITT_T4;
import static org.apache.fop.render.bitmap.TIFFCompressionValues.CCITT_T6;
import static org.apache.fop.render.bitmap.TIFFCompressionValues.NONE;

/**
 * TIFF Renderer configurator
 */
public class TIFFRendererConfigurator extends BitmapRendererConfigurator {
    private static final Log LOG = LogFactory.getLog(TIFFRendererConfigurator.class);

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public TIFFRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    /**
     * Configure the TIFF renderer. Get the configuration to be used for
     * compression
     * @param renderer tiff renderer
     * @throws FOPException fop exception
     * {@inheritDoc}
     */
    public void configure(Renderer renderer) throws FOPException {
        final TIFFRendererConfig config = (TIFFRendererConfig) getRendererConfig(renderer);
        if (config != null) {
            TIFFRenderer tiffRenderer = (TIFFRenderer) renderer;
            //set compression
            tiffRenderer.setBufferedImageType(getCompressionType(config, tiffRenderer.getWriterParams()));
        }
        super.configure(renderer);
    }

    private int getCompressionType(TIFFRendererConfig config, ImageWriterParams writerParms)
            throws FOPException {
        //Some compression formats need a special image format:
        TIFFCompressionValues compression = config.getCompressionType();
        if (compression != null) {
            if (compression != NONE) {
                writerParms.setCompressionMethod(compression.getName());
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("TIFF compression set to " + compression.getName());
            }
        }
        return getBufferedImageTypeFor(compression);
    }

    private int getBufferedImageTypeFor(TIFFCompressionValues compressionType) {
        if (compressionType == CCITT_T6 || compressionType == CCITT_T4) {
            return BufferedImage.TYPE_BYTE_BINARY;
        } else {
            return BufferedImage.TYPE_INT_ARGB;
        }
    }

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        final TIFFRendererConfig tiffConfig = (TIFFRendererConfig) getRendererConfig(documentHandler);
        if (tiffConfig != null) {
            TIFFDocumentHandler tiffHandler = (TIFFDocumentHandler) documentHandler;
            BitmapRenderingSettings settings = tiffHandler.getSettings();
            configure(documentHandler, settings, new TIFFRendererConfigParser());
            settings.setBufferedImageType(getCompressionType(tiffConfig, settings.getWriterParams()));
        }
    }

}
