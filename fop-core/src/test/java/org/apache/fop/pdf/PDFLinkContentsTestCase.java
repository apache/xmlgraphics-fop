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

package org.apache.fop.pdf;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests that {@link PDFLink} writes the {@code /Contents} alternate description
 * required by PDF/UA-1 (ISO 14289-1 §7.18.5) for internal (GoTo) links, not
 * just external (URI) links.
 */
public class PDFLinkContentsTestCase {

    private PDFLink newRegisteredLink(PDFDocument doc, PDFAction action) {
        PDFLink link = new PDFLink(new Rectangle2D.Double(0, 0, 100, 20));
        link.setAction(action);
        doc.registerObject(action);
        doc.registerObject(link);
        return link;
    }

    /**
     * An internal link (PDFGoTo action) carries no alternate text of its own, so
     * previously no {@code /Contents} was written. With the alternate text set
     * explicitly (as the renderer now does from the Link structure element's
     * /Alt), the annotation dictionary must contain it.
     */
    @Test
    public void testInternalLinkWritesContents() {
        PDFDocument doc = new PDFDocument("test");
        PDFLink link = newRegisteredLink(doc, new PDFGoTo("1 0 R"));
        link.setContents("Jump to target section");

        assertTrue(link.toPDFString().contains("/Contents (Jump to target section)"));
    }

    /**
     * Without an explicit description, an internal link still emits no
     * {@code /Contents} (unchanged behaviour for documents that are not
     * accessibility-enabled).
     */
    @Test
    public void testInternalLinkWithoutContents() {
        PDFDocument doc = new PDFDocument("test");
        PDFLink link = newRegisteredLink(doc, new PDFGoTo("1 0 R"));

        assertFalse(link.toPDFString().contains("/Contents"));
    }

    /**
     * For external links the {@code /Contents} value still falls back to the URI
     * action's alternate text when none is set explicitly.
     */
    @Test
    public void testExternalLinkFallsBackToActionAltText() {
        PDFDocument doc = new PDFDocument("test");
        PDFLink link = newRegisteredLink(doc, new PDFUri("https://example.com", "Example website"));

        assertTrue(link.toPDFString().contains("/Contents (Example website)"));
    }

    /**
     * An explicitly set description takes precedence over the URI action's
     * alternate text and is written only once.
     */
    @Test
    public void testExplicitContentsTakesPrecedence() {
        PDFDocument doc = new PDFDocument("test");
        PDFLink link = newRegisteredLink(doc, new PDFUri("https://example.com", "Action alt text"));
        link.setContents("Explicit contents");

        String pdf = link.toPDFString();
        assertTrue(pdf.contains("/Contents (Explicit contents)"));
        assertFalse(pdf.contains("Action alt text"));
    }
}
