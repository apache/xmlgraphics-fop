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
package org.apache.fop.render.ps;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.traits.BorderStyle;

public class PSGraphicsPainterTestCase {

    private static final int SPACE_WIDTH = 14000;

    private static final String DEFAULT_MESSAGE = "Must use space width from style if available";

    private static final String ABOVE_ZERO_MESSAGE = "Style space width is only to be used if higher than zero";

    private static final String MESSAGE_NO_SPACE_WIDTH = "Must use default spacing is space width not availabe";

    @Test
    public void testDrawBorderLineDashed() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PSGenerator generator = new PSGenerator(bos);
        PSGraphicsPainter sut = new PSGraphicsPainter(generator);
        sut.drawBorderLine(0, 0, 0, 0, true, true, BorderStyle.DASHED, Color.BLACK);
        Assert.assertEquals(bos.toString(), "0 LW\n0 0 M 0 0 L S N\n");
    }

    @Test
    public void testDrawLineDotted() throws IOException {
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE,
                "1 setlinecap\n[0 14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE,
                "1 setlinecap\n[0 14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE,
                "1 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE,
                "1 setlinecap\n[0 0.001] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE,
                "1 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE,
                "1 setlinecap\n[0 0.001] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED, true, MESSAGE_NO_SPACE_WIDTH,
                "1 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DOTTED, false, MESSAGE_NO_SPACE_WIDTH,
                "1 setlinecap\n[0 0.001] 0 setdash");
    }


    @Test
    public void testDrawLineSquare() throws IOException {
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE,
                "2 setlinecap\n[0 14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE,
                "2 setlinecap\n[0 14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE,
                "2 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE,
                "2 setlinecap\n[0 0.001] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE,
                "2 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE,
                "2 setlinecap\n[0 0.001] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE, true, MESSAGE_NO_SPACE_WIDTH,
                "2 setlinecap\n[0 0.002004008] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.SQUARE, false, MESSAGE_NO_SPACE_WIDTH,
                "2 setlinecap\n[0 0.001] 0 setdash");
    }

    @Test
    public void testDrawLineDashed() throws IOException {
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(SPACE_WIDTH), true, DEFAULT_MESSAGE,
                "14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(SPACE_WIDTH), false, DEFAULT_MESSAGE,
                "14.0] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(0), true, ABOVE_ZERO_MESSAGE,
                "0.003012048] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(0), false, ABOVE_ZERO_MESSAGE,
                "[0.001 5.0E-4] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(-1), true, ABOVE_ZERO_MESSAGE,
                "0.003012048] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED.withSpaceWidth(-1), false, ABOVE_ZERO_MESSAGE,
                "[0.001 5.0E-4] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED, true, MESSAGE_NO_SPACE_WIDTH,
                "0.003012048] 0 setdash");
        checkSpaceWidthUnit(BorderStyle.DASHED, false, MESSAGE_NO_SPACE_WIDTH,
                "[0.001 5.0E-4] 0 setdash");
    }

    private void checkSpaceWidthUnit(BorderStyle style, boolean horizontal, String message, String expectedValue)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PSGenerator generator = new PSGenerator(bos);
        PSGraphicsPainter sut = new PSGraphicsPainter(generator);

        sut.drawBorderLine(0, 0, 1000, 1, horizontal, true, style, Color.BLACK);

        assertTrue(message, bos.toString().contains(expectedValue));
    }
}
