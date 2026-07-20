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

package org.apache.fop.render.java2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.fo.Constants;
import org.apache.fop.traits.BorderStyle;

public class Java2DGraphicsPainterTestCase {

    private static final int SPACE_WIDTH = 14000;

    private static final String DEFAULT_MESSAGE = "Must use space width from style if available";

    private static final String ABOVE_ZERO_MESSAGE = "Style space width is only to be used if higher than zero";

    private static final String MESSAGE_NO_SPACE_WIDTH = "Must use default spacing is space width not availabe";

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
                                     Integer expectedValue) throws IOException {
        Graphics2D mockGraphics2D = mock(Graphics2D.class);
        Java2DPainter java2DPainter = new Java2DPainter(mockGraphics2D, null, null);
        Java2DGraphicsPainter graphicsPainter = new Java2DGraphicsPainter(java2DPainter);

        ArgumentCaptor<BasicStroke> captor = ArgumentCaptor.forClass(BasicStroke.class);

        graphicsPainter.drawBorderLine(0, 0, 30, 1, horizontal, true, style, Color.BLACK);

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
