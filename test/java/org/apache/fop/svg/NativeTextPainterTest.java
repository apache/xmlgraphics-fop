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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;

abstract class NativeTextPainterTest {

    protected final void runTest(String testcase, OperatorValidator validator) throws Exception {
        FontInfo fontInfo = createFontInfo();
        BridgeContext bridgeContext = createBridgeContext(fontInfo);
        GraphicsNode svg = loadSVG(bridgeContext, testcase);
        Graphics2D g2d = createGraphics2D(fontInfo, validator);
        svg.paint(g2d);
        validator.end();
    }

    private FontInfo createFontInfo() {
        FontInfo fontInfo = new FontInfo();
        new Base14FontCollection(true).setup(0, fontInfo);
        return fontInfo;
    }

    private BridgeContext createBridgeContext(FontInfo fontInfo) {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        SVGUserAgent svgUserAgent = new SVGUserAgent(userAgent, new FOPFontFamilyResolverImpl(fontInfo),
                new AffineTransform());
        BridgeContext bridgeContext = new BridgeContext(svgUserAgent);
        bridgeContext.setTextPainter(createTextPainter(fontInfo));
        return bridgeContext;
    }

    protected abstract TextPainter createTextPainter(FontInfo fontInfo);

    private GraphicsNode loadSVG(BridgeContext bridgeContext, String resourceName) throws IOException {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(null);
        Document svg = factory.createDocument(null, getClass().getResourceAsStream(resourceName));
        GVTBuilder builder = new GVTBuilder();
        return builder.build(bridgeContext, svg);
    }

    protected abstract Graphics2D createGraphics2D(FontInfo fontInfo, OperatorValidator validator);

}
