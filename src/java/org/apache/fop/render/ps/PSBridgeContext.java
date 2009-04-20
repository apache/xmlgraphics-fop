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

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.TextPainter;

import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPBridgeContext;

/**
 * BridgeContext which registers the custom bridges for PostScript output.
 */
public class PSBridgeContext extends AbstractFOPBridgeContext {

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param documentLoader the Document Loader to use for referenced documents.
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param imageManager an image manager
     * @param imageSessionContext an image session context
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     */
    public PSBridgeContext(UserAgent userAgent, DocumentLoader documentLoader,
            FontInfo fontInfo, ImageManager imageManager,
            ImageSessionContext imageSessionContext,
            AffineTransform linkTransform) {
        super(userAgent, documentLoader, fontInfo,
                imageManager, imageSessionContext, linkTransform);
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param imageManager an image manager
     * @param imageSessionContext an image session context
     */
    public PSBridgeContext(UserAgent userAgent, FontInfo fontInfo,
            ImageManager imageManager, ImageSessionContext imageSessionContext) {
        super(userAgent, fontInfo, imageManager, imageSessionContext);
    }

    /** {@inheritDoc} */
    public void registerSVGBridges() {
        super.registerSVGBridges();

        if (fontInfo != null) {
            TextPainter textPainter = new PSTextPainter(fontInfo);
            SVGTextElementBridge textElementBridge = new PSTextElementBridge(textPainter);
            putBridge(textElementBridge);

            //Batik flow text extension (may not always be available)
            //putBridge(new PDFBatikFlowTextElementBridge(fontInfo);
            putElementBridgeConditional(
                    "org.apache.fop.render.ps.PSBatikFlowTextElementBridge",
                    "org.apache.batik.extension.svg.BatikFlowTextElementBridge");

            //SVG 1.2 flow text support
            //putBridge(new PDFSVG12TextElementBridge(fontInfo)); //-->Batik 1.7
            putElementBridgeConditional(
                    "org.apache.fop.render.ps.PSSVG12TextElementBridge",
                    "org.apache.batik.bridge.svg12.SVG12TextElementBridge");

            //putBridge(new PDFSVGFlowRootElementBridge(fontInfo));
            putElementBridgeConditional(
                    "org.apache.fop.render.ps.PSSVGFlowRootElementBridge",
                    "org.apache.batik.bridge.svg12.SVGFlowRootElementBridge");
        }

        //putBridge(new PSImageElementBridge()); //TODO uncomment when implemented
    }

    // Make sure any 'sub bridge contexts' also have our bridges.
    //TODO There's no matching method in the super-class here
    public BridgeContext createBridgeContext() {
        return new PSBridgeContext(getUserAgent(), getDocumentLoader(),
                                    fontInfo,
                                    getImageManager(),
                                    getImageSessionContext(),
                                    linkTransform);
    }

}
