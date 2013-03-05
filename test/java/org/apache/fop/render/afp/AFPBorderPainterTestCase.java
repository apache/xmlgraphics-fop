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

package org.apache.fop.render.afp;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.fop.afp.AFPBorderPainter;
import org.apache.fop.afp.AFPLineDataInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.BorderPaintingInfo;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.Factory;
import org.apache.fop.fo.Constants;
import org.junit.Before;
import org.junit.Test;

public class AFPBorderPainterTestCase {
    private ByteArrayOutputStream outStream;
    private AFPBorderPainter borderPainter;
    private DataStream ds;
    private AFPLineDataInfo line;

    @Before
    public void setUp() throws Exception {
        outStream = new ByteArrayOutputStream();
        ds = new MyDataStream(new Factory(), null, outStream);
        ds.startDocument();
        ds.startPage(1000, 1000, 90, 72, 72);
        borderPainter = new AFPBorderPainter(new AFPPaintingState(), ds);
    }

    /**
     * This test will fail if either of the below statements isn't true:
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_SPACE_RATIO = 0.5f:q
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_LENGTH_FACTOR = 4.0f.
     */
    @Test
    public void testDrawBorderLine() throws Exception {
        BorderPaintingInfo paintInfo = new BorderPaintingInfo(0f, 0f, 1000f, 1000f, true,
                Constants.EN_DASHED, Color.BLACK);
        borderPainter.paint(paintInfo);
        ds.endDocument();
        assertTrue(line.getX1() == 4999 && line.getX2() == 8332);
    }
    
    class MyDataStream extends DataStream {
        public MyDataStream(Factory factory, AFPPaintingState paintingState, OutputStream outputStream) {
            super(factory, paintingState, outputStream);
        }
        
        public void createLine(AFPLineDataInfo lineDataInfo) {
            line = lineDataInfo;
        }
    }
}
