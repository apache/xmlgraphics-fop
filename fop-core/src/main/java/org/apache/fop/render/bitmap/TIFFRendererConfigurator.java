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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.writer.Endianness;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.bitmap.TIFFRendererConfig.TIFFRendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;

import static org.apache.fop.render.bitmap.TIFFCompressionValue.NONE;

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
            setCompressionMethod(config.getCompressionType(), tiffRenderer.getRenderingSettings());
        }
        super.configure(renderer);
    }

    private void setCompressionMethod(TIFFCompressionValue compression,
            BitmapRenderingSettings settings) throws FOPException {
        if (compression != null) {
            if (compression != NONE) {
                settings.setCompressionMethod(compression.getName());
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("TIFF compression set to " + compression.getName());
            }
            if (compression.hasCCITTCompression()) {
                settings.setBufferedImageType(compression.getImageType());
            }
        }
    }

    private boolean isSingleStrip(TIFFRendererConfig config) {
        Boolean singleRowPerStrip = config.isSingleStrip();
        return singleRowPerStrip == null ? false : singleRowPerStrip;
    }

    private Endianness getEndianness(TIFFRendererConfig config) {
        Endianness endianMode = config.getEndianness();
        return endianMode == null ? Endianness.DEFAULT : endianMode;
    }

    @Override
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        final TIFFRendererConfig config = (TIFFRendererConfig) getRendererConfig(documentHandler);
        if (config != null) {
            TIFFDocumentHandler tiffHandler = (TIFFDocumentHandler) documentHandler;
            BitmapRenderingSettings settings = tiffHandler.getSettings();
            configure(documentHandler, settings, new TIFFRendererConfigParser());
            setCompressionMethod(config.getCompressionType(), settings);
            settings.getWriterParams().setSingleStrip(isSingleStrip(config));
            settings.getWriterParams().setEndianness(getEndianness(config));
        }
    }

}
