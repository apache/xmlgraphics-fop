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

package org.apache.fop.svg;

import java.awt.geom.AffineTransform;
import java.lang.reflect.Constructor;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;

import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.fonts.FontInfo;

/**
 * BridgeContext which registers the custom bridges for PDF output.
 */
public class PDFBridgeContext extends BridgeContext {

    /** The font list. */
    private final FontInfo fontInfo;

    private final ImageManager imageManager;
    private final ImageSessionContext imageSessionContext;

    private AffineTransform linkTransform;

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param loader the Document Loader to use for referenced documents.
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     */
    public PDFBridgeContext(UserAgent userAgent,
                            DocumentLoader loader,
                            FontInfo fontInfo,
                            ImageManager imageManager,
                            ImageSessionContext imageSessionContext,
                            AffineTransform linkTransform) {
        super(userAgent, loader);
        this.fontInfo = fontInfo;
        this.imageManager = imageManager;
        this.imageSessionContext = imageSessionContext;
        this.linkTransform = linkTransform;
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     */
    public PDFBridgeContext(UserAgent userAgent,
                            FontInfo fontInfo,
                            ImageManager imageManager,
                            ImageSessionContext imageSessionContext,
                            AffineTransform linkTransform) {
        super(userAgent);
        this.fontInfo = fontInfo;
        this.imageManager = imageManager;
        this.imageSessionContext = imageSessionContext;
        this.linkTransform = linkTransform;
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     */
    public PDFBridgeContext(UserAgent userAgent,
                            FontInfo fontInfo,
                            ImageManager imageManager,
                            ImageSessionContext imageSessionContext) {
        this(userAgent, fontInfo, imageManager, imageSessionContext, null);
    }

    /**
     * Returns the ImageManager to be used by the ImageElementBridge.
     * @return the image manager
     */
    public ImageManager getImageManager() {
        return this.imageManager;
    }

    /**
     * Returns the ImageSessionContext to be used by the ImageElementBridge.
     * @return the image session context
     */
    public ImageSessionContext getImageSessionContext() {
        return this.imageSessionContext;
    }

    private void putPDFElementBridgeConditional(String className, String testFor) {
        try {
            Class.forName(testFor);
            //if we get here the test class is available

            Class clazz = Class.forName(className);
            Constructor constructor = clazz.getConstructor(new Class[] {FontInfo.class});
            putBridge((Bridge)constructor.newInstance(new Object[] {fontInfo}));
        } catch (Throwable t) {
            //simply ignore (bridges instantiated over this method are optional)
        }
    }

    /** {@inheritDoc} */
    public void registerSVGBridges() {
        super.registerSVGBridges();

        if (fontInfo != null) {
            PDFTextElementBridge textElementBridge = new PDFTextElementBridge(fontInfo);
            putBridge(textElementBridge);

            //Batik flow text extension (may not always be available)
            //putBridge(new PDFBatikFlowTextElementBridge(fontInfo);
            putPDFElementBridgeConditional(
                    "org.apache.fop.svg.PDFBatikFlowTextElementBridge",
                    "org.apache.batik.extension.svg.BatikFlowTextElementBridge");

            //SVG 1.2 flow text support
            //putBridge(new PDFSVG12TextElementBridge(fontInfo)); //-->Batik 1.7
            putPDFElementBridgeConditional(
                    "org.apache.fop.svg.PDFSVG12TextElementBridge",
                    "org.apache.batik.bridge.svg12.SVG12TextElementBridge");

            //putBridge(new PDFSVGFlowRootElementBridge(fontInfo));
            putPDFElementBridgeConditional(
                    "org.apache.fop.svg.PDFSVGFlowRootElementBridge",
                    "org.apache.batik.bridge.svg12.SVGFlowRootElementBridge");
        }

        PDFAElementBridge pdfAElementBridge = new PDFAElementBridge();
        if (linkTransform != null) {
            pdfAElementBridge.setCurrentTransform(linkTransform);
        } else {
            pdfAElementBridge.setCurrentTransform(new AffineTransform());
        }
        putBridge(pdfAElementBridge);

        putBridge(new PDFImageElementBridge());
    }

    // Make sure any 'sub bridge contexts' also have our bridges.
    //TODO There's no matching method in the super-class here
    public BridgeContext createBridgeContext() {
        return new PDFBridgeContext(getUserAgent(), getDocumentLoader(),
                                    fontInfo,
                                    getImageManager(),
                                    getImageSessionContext(),
                                    linkTransform);
    }

}
