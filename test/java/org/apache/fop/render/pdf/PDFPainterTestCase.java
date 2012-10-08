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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Rectangle;

import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.traits.BorderProps;

public class PDFPainterTestCase {

    @Test
    public void testDrawBorderRect() {
        // the goal of this test is to check that the drawing of rounded corners in PDF calls
        // PDFGraphicsPaiter.cubicBezierTo(); the check is done by verifying that a " c " command is written
        // to the PDFContentGenerator
        // mock
        PDFContentGenerator pdfContentGenerator = mock(PDFContentGenerator.class);
        // the next line is commented out because the format() method is static and not possible to mock
        // when(PDFContentGenerator.format(anyFloat())).thenReturn("20.0");
        // mock
        FOUserAgent foUserAgent = mock(FOUserAgent.class);
        when(foUserAgent.isAccessibilityEnabled()).thenReturn(false);
        // mock
        IFContext ifContext = mock(IFContext.class);
        when(ifContext.getUserAgent()).thenReturn(foUserAgent);
        // mock
        PDFDocumentHandler pdfDocumentHandler = mock(PDFDocumentHandler.class);
        when(pdfDocumentHandler.getGenerator()).thenReturn(pdfContentGenerator);
        when(pdfDocumentHandler.getContext()).thenReturn(ifContext);
        // mock
        PDFLogicalStructureHandler pdfLogicalStructureHandler = mock(PDFLogicalStructureHandler.class);
        // real, not mock
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, pdfLogicalStructureHandler);
        // build rectangle 200 x 50 (points, which are converted to milipoints)
        Rectangle rectangle = new Rectangle(0, 0, 200000, 50000);
        // build border properties: width 4pt, radius 30pt
        int style = Constants.EN_SOLID;
        BorderProps.Mode mode = BorderProps.Mode.SEPARATE;
        Color color = Color.BLACK;
        int borderWidth = 4000;
        int radiusStart = 30000;
        int radiusEnd = 30000;
        BorderProps border1 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border2 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border3 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border4 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        try {
            pdfPainter.drawBorderRect(rectangle, border1, border2, border3, border4, Color.WHITE);
            // since we cannot mock the PDFContentGenerator.format() static method we have to restrict the
            // verification to commands that end with " c ".
            verify(pdfContentGenerator, times(16)).add(endsWith(" c "));
        } catch (Exception e) {
            fail("something broke...");
        }
    }

}
