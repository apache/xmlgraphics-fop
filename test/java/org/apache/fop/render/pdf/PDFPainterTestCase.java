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
    public void testDrawBorderRect() throws Exception {
        // the goal of this test is to check that the drawing of rounded corners in PDF calls
        // PDFGraphicsPaiter.cubicBezierTo(); the check is done by verifying that a " c " command is written
        // to the PDFContentGenerator
        PDFContentGenerator pdfContentGenerator = mock(PDFContentGenerator.class);
        FOUserAgent foUserAgent = mock(FOUserAgent.class);
        when(foUserAgent.isAccessibilityEnabled()).thenReturn(false);
        IFContext ifContext = mock(IFContext.class);
        when(ifContext.getUserAgent()).thenReturn(foUserAgent);
        PDFDocumentHandler pdfDocumentHandler = mock(PDFDocumentHandler.class);
        when(pdfDocumentHandler.getGenerator()).thenReturn(pdfContentGenerator);
        when(pdfDocumentHandler.getContext()).thenReturn(ifContext);
        PDFLogicalStructureHandler pdfLogicalStructureHandler = mock(PDFLogicalStructureHandler.class);
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, pdfLogicalStructureHandler);
        // build rectangle 200 x 50 (points, which are converted to milipoints)
        Rectangle rectangle = new Rectangle(0, 0, 200000, 50000);
        // build border properties: width 4pt, radius 30pt
        BorderProps border = new BorderProps(Constants.EN_SOLID, 4000, 30000, 30000, Color.BLACK,
                BorderProps.Mode.SEPARATE);
        pdfPainter.drawBorderRect(rectangle, border, border, border, border, Color.WHITE);
        // since we cannot mock the PDFContentGenerator.format() static method we have to restrict the
        // verification to commands that end with " c ".
        verify(pdfContentGenerator, times(16)).add(endsWith(" c "));
    }

}
