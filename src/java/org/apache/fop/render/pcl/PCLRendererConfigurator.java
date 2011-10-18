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

import java.awt.Graphics2D;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.render.DefaultFontResolver;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.java2d.Base14FontCollection;
import org.apache.fop.render.java2d.ConfiguredFontCollection;
import org.apache.fop.render.java2d.InstalledFontCollection;
import org.apache.fop.render.java2d.Java2DFontMetrics;

/**
 * PCL Renderer configurator
 */
public class PCLRendererConfigurator extends PrintRendererConfigurator
            implements IFDocumentHandlerConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PCLRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Throws an UnsupportedOperationException.
     *
     * @param renderer not used
     */
    public void configure(Renderer renderer) {
        throw new UnsupportedOperationException();
    }

    private void configure(Configuration cfg, PCLRenderingUtil pclUtil) throws FOPException {
        String rendering = cfg.getChild("rendering").getValue(null);
        if (rendering != null) {
            try {
                pclUtil.setRenderingMode(PCLRenderingMode.valueOf(rendering));
            } catch (IllegalArgumentException e) {
                throw new FOPException(
                    "Valid values for 'rendering' are 'quality', 'speed' and 'bitmap'."
                        + " Value found: " + rendering);
            }
        }

        String textRendering = cfg.getChild("text-rendering").getValue(null);
        if ("bitmap".equalsIgnoreCase(textRendering)) {
            pclUtil.setAllTextAsBitmaps(true);
        } else if ("auto".equalsIgnoreCase(textRendering)) {
            pclUtil.setAllTextAsBitmaps(false);
        } else if (textRendering != null) {
            throw new FOPException(
                    "Valid values for 'text-rendering' are 'auto' and 'bitmap'. Value found: "
                        + textRendering);
        }

        pclUtil.setPJLDisabled(cfg.getChild("disable-pjl").getValueAsBoolean(false));
    }

    // ---=== IFDocumentHandler configuration ===---

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            PCLDocumentHandler pclDocumentHandler = (PCLDocumentHandler)documentHandler;
            PCLRenderingUtil pclUtil = pclDocumentHandler.getPCLUtil();
            configure(cfg, pclUtil);
        }
    }

    /** {@inheritDoc} */
    public void setupFontInfo(IFDocumentHandler documentHandler, FontInfo fontInfo)
                throws FOPException {
        FontManager fontManager = userAgent.getFactory().getFontManager();

        final Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
        final List fontCollections = new java.util.ArrayList();
        fontCollections.add(new Base14FontCollection(java2DFontMetrics));
        fontCollections.add(new InstalledFontCollection(java2DFontMetrics));

        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            FontResolver fontResolver = new DefaultFontResolver(userAgent);
            FontEventListener listener = new FontEventAdapter(
                    userAgent.getEventBroadcaster());
            List fontList = buildFontList(cfg, fontResolver, listener);
            fontCollections.add(new ConfiguredFontCollection(fontResolver, fontList,
                                userAgent.isComplexScriptFeaturesEnabled()));
        }

        fontManager.setup(fontInfo,
                (FontCollection[])fontCollections.toArray(
                        new FontCollection[fontCollections.size()]));
        documentHandler.setFontInfo(fontInfo);
    }


}
