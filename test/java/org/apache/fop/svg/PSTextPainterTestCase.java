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

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.io.IOException;

import org.junit.Test;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.batik.bridge.TextPainter;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.ps.PSTextPainter;

public class PSTextPainterTestCase extends NativeTextPainterTest {

    private static class OperatorCheckingPSGraphics2D extends PSGraphics2D {

        OperatorCheckingPSGraphics2D(FontInfo fontInfo, final OperatorValidator validator) {
            super(false, new PSGenerator(new NullOutputStream()) {

                @Override
                public void writeln(String cmd) throws IOException {
                    validator.check(cmd);
                }

            });
        }
    }

    @Override
    protected TextPainter createTextPainter(FontInfo fontInfo) {
        return new PSTextPainter(fontInfo);
    }

    @Override
    protected Graphics2D createGraphics2D(FontInfo fontInfo, OperatorValidator validator) {
        PSGraphics2D g2d = new OperatorCheckingPSGraphics2D(fontInfo, validator);
        g2d.setGraphicContext(new GraphicContext());
        return g2d;
    }

    @Test
    public void testRotatedGlyph() throws Exception {
        runTest("rotated-glyph.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "1 0 0 -1 40 110 Tm")
                .addOperatorMatch("xshow", "(A)\n[0] xshow")
                .addOperatorMatch("Tm", "0.70711 0.70711 0.70711 -0.70711 106.7 110 Tm")
                .addOperatorMatch("xshow", "(B)\n[0] xshow")
                .addOperatorMatch("Tm", "1 0 0 -1 173.39999 110 Tm")
                .addOperatorMatch("xshow", "(C)\n[0] xshow"));
    }

}
