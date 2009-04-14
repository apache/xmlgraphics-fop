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

package org.apache.fop.render;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFRenderer;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.render.rtf.RTFHandler;

/**
 * Tests for {@link RendererFactory}.
 */
public class RendererFactoryTest extends TestCase {

    public void testDocumentHandlerLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        IFDocumentHandler handler;
        IFDocumentHandler overrideHandler;

        ua = fopFactory.newFOUserAgent();
        handler = factory.createDocumentHandler(ua, MimeConstants.MIME_PDF);
        assertTrue(handler instanceof PDFDocumentHandler);

        ua = fopFactory.newFOUserAgent();
        overrideHandler = new PDFDocumentHandler();
        overrideHandler.setContext(new IFContext(ua));
        ua.setDocumentHandlerOverride(overrideHandler);
        handler = factory.createDocumentHandler(ua, null);
        assertTrue(handler == overrideHandler);

        ua = fopFactory.newFOUserAgent();
        try {
            handler = factory.createDocumentHandler(ua, "invalid/format");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            //expected
        }
    }

    public void testRendererLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        Renderer renderer;
        Renderer overrideRenderer;

        ua = fopFactory.newFOUserAgent();
        renderer = factory.createRenderer(ua, MimeConstants.MIME_PDF);
        assertTrue(renderer instanceof IFRenderer);

        factory.setRendererPreferred(true); //Test legacy setting
        ua = fopFactory.newFOUserAgent();
        renderer = factory.createRenderer(ua, MimeConstants.MIME_PDF);
        assertTrue(renderer instanceof PDFRenderer);

        ua = fopFactory.newFOUserAgent();
        renderer = factory.createRenderer(ua, MimeConstants.MIME_FOP_IF);
        assertTrue(renderer instanceof IFRenderer);

        factory.setRendererPreferred(false);
        ua = fopFactory.newFOUserAgent();
        overrideRenderer = new PDFRenderer();
        overrideRenderer.setUserAgent(ua);
        ua.setRendererOverride(overrideRenderer);
        renderer = factory.createRenderer(ua, null);
        assertTrue(renderer == overrideRenderer);

        ua = fopFactory.newFOUserAgent();
        IFDocumentHandler overrideHandler;
        overrideHandler = new PDFDocumentHandler();
        overrideHandler.setContext(new IFContext(ua));
        ua.setDocumentHandlerOverride(overrideHandler);
        renderer = factory.createRenderer(ua, null);
        assertTrue(renderer instanceof IFRenderer);

        ua = fopFactory.newFOUserAgent();
        try {
            renderer = factory.createRenderer(ua, "invalid/format");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            //expected
        }
    }

    public void testFOEventHandlerLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        FOEventHandler foEventHandler;
        FOEventHandler overrideFOEventHandler;

        ua = fopFactory.newFOUserAgent();
        foEventHandler = factory.createFOEventHandler(
                ua, MimeConstants.MIME_PDF, new NullOutputStream());
        assertTrue(foEventHandler instanceof AreaTreeHandler);

        ua = fopFactory.newFOUserAgent();
        foEventHandler = factory.createFOEventHandler(
                ua, MimeConstants.MIME_RTF, new NullOutputStream());
        assertTrue(foEventHandler instanceof RTFHandler);

        ua = fopFactory.newFOUserAgent();
        try {
            foEventHandler = factory.createFOEventHandler(
                    ua, "invalid/format", new NullOutputStream());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            //expected
        }

        ua = fopFactory.newFOUserAgent();
        try {
            foEventHandler = factory.createFOEventHandler(
                    ua, MimeConstants.MIME_PDF, null);
            fail("Expected FOPException because of missing OutputStream");
        } catch (FOPException fe) {
            //expected
        }

        ua = fopFactory.newFOUserAgent();
        overrideFOEventHandler = new RTFHandler(ua, new NullOutputStream());
        ua.setFOEventHandlerOverride(overrideFOEventHandler);
        foEventHandler = factory.createFOEventHandler(
                ua, null, null);
        assertTrue(foEventHandler == overrideFOEventHandler);
    }

}
