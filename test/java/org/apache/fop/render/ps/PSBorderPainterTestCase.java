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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fo.Constants;

public class PSBorderPainterTestCase {

    private PSGenerator generator;
    private ByteArrayOutputStream outStream;
    private PSGraphicsPainter borderPainter;

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        generator = new PSGenerator(outStream);
        borderPainter = new PSGraphicsPainter(generator);
    }

    /**
     * This test will fail if either of the below statements isn't true:
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_SPACE_RATIO = 0.5f:q
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_LENGTH_FACTOR = 4.0f.
     */
    @Test
    public void testDrawBorderLine() throws Exception {
        borderPainter.drawBorderLine(0, 0, 40000, 1000, true, true,
                Constants.EN_DASHED, Color.BLACK);
        assertTrue(outStream.toString().contains("[2.1052632 1.0526316] 0 setdash"));
    }

    public void tearDown() {
        generator = null;
        outStream = null;
    }
}
