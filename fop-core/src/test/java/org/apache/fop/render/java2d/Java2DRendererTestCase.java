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

/* $Id: Java2DRenderer.java 1827168 2018-03-19 08:49:57Z ssteiner $ */
package org.apache.fop.render.java2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.traits.BorderStyle;

public class Java2DRendererTestCase {

    private static final int SPACE_WIDTH = 14000;

    private static final String DEFAULT_MESSAGE = "Must use space width from style if available";

    private static final String ABOVE_ZERO_MESSAGE = "Style space width is only to be used if higher than zero";

    private static final String MESSAGE_NO_SPACE_WIDTH = "Must use default spacing is space width not availabe";

    @Test
    public void testPrint() throws Exception {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Java2DRenderer java2DRenderer = new Java2DRenderer(userAgent) {
            public String getMimeType() {
                return null;
            }
        };
        PageViewport pageViewport = new PageViewport(new Rectangle(), 0, null, null, true);
        pageViewport.setPageIndex(0);
        Page page = new Page();
        RegionViewport regionViewport = new RegionViewport(new Rectangle());
        BodyRegion bodyRegion = new BodyRegion(Constants.FO_REGION_BODY, null, regionViewport, 0, 0);
        bodyRegion.setCTM(new CTM());
        bodyRegion.getMainReference().createSpan(true);
        regionViewport.setRegionReference(bodyRegion);
        page.setRegionViewport(Constants.FO_REGION_BODY, regionViewport);
        pageViewport.setPage(page);
        java2DRenderer.renderPage(pageViewport);
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Assert.assertEquals(java2DRenderer.print(image.createGraphics(), null, 0), 0);
    }

    @Test
    public void testDrawBorderLineDotted() throws IOException {
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.DOTTED.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.DOTTED, true, MESSAGE_NO_SPACE_WIDTH, 2);
        checkDashArrayValue(BorderStyle.DOTTED, false, MESSAGE_NO_SPACE_WIDTH, 1);
    }

    @Test
    public void testDrawBorderLineSquare() throws IOException {
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.SQUARE.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.SQUARE, true, MESSAGE_NO_SPACE_WIDTH, 2);
        checkDashArrayValue(BorderStyle.SQUARE, false, MESSAGE_NO_SPACE_WIDTH, 1);
    }

    @Test
    public void testDrawBorderLineDashed() throws IOException {
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE, null);
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE, 2);
        checkDashArrayValue(BorderStyle.DASHED.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE, 1);
        checkDashArrayValue(BorderStyle.DASHED, true, MESSAGE_NO_SPACE_WIDTH, 2);
        checkDashArrayValue(BorderStyle.DASHED, false, MESSAGE_NO_SPACE_WIDTH, 1);
    }

    private void checkDashArrayValue(BorderStyle style, boolean horizontal, String assertionMessage,
                                     Integer expectedValue) {
        Graphics2D mockGraphics2D = mock(Graphics2D.class);
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Java2DRenderer java2DRenderer = new Java2DRenderer(userAgent) {
            public String getMimeType() {
                return null;
            }
        };
        java2DRenderer.state = new Java2DGraphicsState(mockGraphics2D, null, null);

        java2DRenderer.drawBorderLine(0f, 0f, 30f, 1f, horizontal, true, style, Color.BLACK);

        ArgumentCaptor<BasicStroke> captor = ArgumentCaptor.forClass(BasicStroke.class);
        verify(mockGraphics2D).setStroke(captor.capture());

        int spaceWidthIndex = 0;
        int cap = BasicStroke.CAP_BUTT;
        if (style.getEnumValue() != Constants.EN_DASHED) {
            if (style.getEnumValue() == Constants.EN_DOTTED) {
                cap = BasicStroke.CAP_ROUND;
            } else {
                cap = BasicStroke.CAP_SQUARE;
            }
            spaceWidthIndex = 1;
        }

        BasicStroke stroke = captor.getValue();
        if (expectedValue == null) {
            assertEquals(assertionMessage, SPACE_WIDTH, stroke.getDashArray()[spaceWidthIndex], 0.0f);
        } else {
            assertEquals(assertionMessage, expectedValue, stroke.getDashArray()[spaceWidthIndex], 0.0f);
        }
        assertEquals("Cap must match style", cap, stroke.getEndCap());
    }
}
