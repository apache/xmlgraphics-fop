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

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.java2d.Base14FontCollection;
import org.apache.fop.render.java2d.ConfiguredFontCollection;
import org.apache.fop.render.java2d.InstalledFontCollection;
import org.apache.fop.render.java2d.Java2DFontMetrics;

/**
 * PCL Renderer configurator
 */
public class PCLRendererConfigurator extends PrintRendererConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PCLRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        PCLRendererConfig pdfConfig = (PCLRendererConfig) getRendererConfig(documentHandler);
        if (pdfConfig != null) {
            PCLDocumentHandler pclDocumentHandler = (PCLDocumentHandler) documentHandler;
            PCLRenderingUtil pclUtil = pclDocumentHandler.getPCLUtil();
            configure(pdfConfig, pclUtil);
        }
    }

    private void configure(PCLRendererConfig config, PCLRenderingUtil pclUtil) throws FOPException {
        if (config.getRenderingMode() != null) {
            pclUtil.setRenderingMode(config.getRenderingMode());
        }
        if (config.isDisablePjl() != null) {
            pclUtil.setPJLDisabled(config.isDisablePjl());
        }
        if (config.isTextRendering() != null) {
            pclUtil.setAllTextAsBitmaps(config.isTextRendering());
        }

    }

    @Override
    protected List<FontCollection> getDefaultFontCollection() {
        final List<FontCollection> fontCollections = new java.util.ArrayList<FontCollection>();
        final Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
        fontCollections.add(new Base14FontCollection(java2DFontMetrics));
        fontCollections.add(new InstalledFontCollection(java2DFontMetrics));
        return fontCollections;
    }

    @Override
    protected FontCollection createCollectionFromFontList(InternalResourceResolver resourceResolver,
            List<EmbedFontInfo> fontList) {
        return new ConfiguredFontCollection(resourceResolver, fontList,
                userAgent.isComplexScriptFeaturesEnabled());
    }

}
