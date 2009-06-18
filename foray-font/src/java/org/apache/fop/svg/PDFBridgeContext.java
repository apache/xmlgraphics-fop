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

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;

import org.axsl.font.FontConsumer;

/**
 * BridgeContext which registers the custom bridges for PDF output.
 */
public class PDFBridgeContext extends BridgeContext {
    
    /** The font consumer. */
    private final FontConsumer fontConsumer;

    private AffineTransform linkTransform;
    
    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param loader the Document Loader to use for referenced documents.
     * @param fontConsumer the font consumer, may be null
     *                 in which case text is painted as shapes
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     */
    public PDFBridgeContext(UserAgent userAgent,
                            DocumentLoader loader,
                            FontConsumer fontConsumer,
                            AffineTransform linkTransform) {
        super(userAgent, loader);
        this.fontConsumer = fontConsumer;
        this.linkTransform = linkTransform;
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param fontConsumer the font consumer, may be null
     *                 in which case text is painted as shapes
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     */
    public PDFBridgeContext(UserAgent userAgent, FontConsumer fontConsumer, 
                AffineTransform linkTransform) {
        super(userAgent);
        this.fontConsumer = fontConsumer;
        this.linkTransform = linkTransform;
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param fontConsumer the font consumer, may be null
     *                 in which case text is painted as shapes
     */
    public PDFBridgeContext(UserAgent userAgent, FontConsumer fontConsumer) {
        this(userAgent, fontConsumer, null);
    }

    /** @see org.apache.batik.bridge.BridgeContext#registerSVGBridges() */
    public void registerSVGBridges() {
        super.registerSVGBridges();

        if (fontConsumer != null) {
            putBridge(new PDFTextElementBridge(fontConsumer));
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
    public BridgeContext createBridgeContext() {
        return new PDFBridgeContext(getUserAgent(), getDocumentLoader(),
                                    fontConsumer, linkTransform);
    }
}
