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

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

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
import org.apache.fop.render.rtf.RTFHandler;

/**
 * Tests for {@link RendererFactory}.
 */
public class RendererFactoryTestCase {

    @Test
    public void testDocumentHandlerLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        IFDocumentHandler handler;
        IFDocumentHandler overrideHandler;

        ua = fopFactory.newFOUserAgent();
        handler = factory.createDocumentHandler(ua, MimeConstants.MIME_PDF);

        ua = fopFactory.newFOUserAgent();
        overrideHandler = new PDFDocumentHandler(new IFContext(ua));
        ua.setDocumentHandlerOverride(overrideHandler);
        handler = factory.createDocumentHandler(ua, null);
        assertSame(overrideHandler, handler);

        final FOUserAgent invalidFormatUa = fopFactory.newFOUserAgent();
        assertThrows(UnsupportedOperationException.class,
                () -> factory.createDocumentHandler(invalidFormatUa, "invalid/format"));
    }

    @Test
    public void testRendererLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        Renderer renderer;

        ua = fopFactory.newFOUserAgent();
        renderer = factory.createRenderer(ua, MimeConstants.MIME_PDF);
        assertTrue(renderer instanceof IFRenderer);

        ua = fopFactory.newFOUserAgent();
        renderer = factory.createRenderer(ua, MimeConstants.MIME_FOP_IF);
        assertTrue(renderer instanceof IFRenderer);

        ua = fopFactory.newFOUserAgent();
        IFDocumentHandler overrideHandler;
        overrideHandler = new PDFDocumentHandler(new IFContext(ua));
        ua.setDocumentHandlerOverride(overrideHandler);
        renderer = factory.createRenderer(ua, null);
        assertTrue(renderer instanceof IFRenderer);

        final FOUserAgent invalidFormatUa = fopFactory.newFOUserAgent();
        assertThrows(UnsupportedOperationException.class,
                () -> factory.createRenderer(invalidFormatUa, "invalid/format"));
    }

    @Test
    public void testFOEventHandlerLevel() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        RendererFactory factory = fopFactory.getRendererFactory();
        FOUserAgent ua;
        FOEventHandler foEventHandler;
        FOEventHandler overrideFOEventHandler;

        ua = fopFactory.newFOUserAgent();
        foEventHandler = factory.createFOEventHandler(
                ua, MimeConstants.MIME_PDF, NullOutputStream.INSTANCE);
        assertTrue(foEventHandler instanceof AreaTreeHandler);

        ua = fopFactory.newFOUserAgent();
        foEventHandler = factory.createFOEventHandler(
                ua, MimeConstants.MIME_RTF, NullOutputStream.INSTANCE);
        assertTrue(foEventHandler instanceof RTFHandler);

        final FOUserAgent invalidFormatUa = fopFactory.newFOUserAgent();
        assertThrows(UnsupportedOperationException.class, () -> factory.createFOEventHandler(
                invalidFormatUa, "invalid/format", NullOutputStream.INSTANCE));

        final FOUserAgent noOutputStreamUa = fopFactory.newFOUserAgent();
        assertThrows("Expected FOPException because of missing OutputStream", FOPException.class,
                () -> factory.createFOEventHandler(noOutputStreamUa, MimeConstants.MIME_PDF, null));

        ua = fopFactory.newFOUserAgent();
        overrideFOEventHandler = new RTFHandler(ua, NullOutputStream.INSTANCE);
        ua.setFOEventHandlerOverride(overrideFOEventHandler);
        foEventHandler = factory.createFOEventHandler(
                ua, null, null);
        assertSame(overrideFOEventHandler, foEventHandler);
    }

}
