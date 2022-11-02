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
package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.java2d.GeneralGraphics2DImagePainter;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fonts.FontInfo;

public class PSGraphics2DAdapterTestCase {
    @Test
    public void testFontFallback() throws IOException {
        PSGenerator gen = new PSGenerator(new ByteArrayOutputStream());
        FontInfo fi = new FontInfo();
        fi.addFontProperties("a", "b", "c", 400);
        PSGraphics2DAdapter psGraphics2DAdapter = new PSGraphics2DAdapter(gen, true, fi);
        MyPainter painter = new MyPainter();
        psGraphics2DAdapter.paintImage(painter, null, 0, 0, 0, 0);
        Assert.assertEquals(painter.font, "b");
    }

    static class MyPainter implements GeneralGraphics2DImagePainter {
        String font;
        public Graphics2D getGraphics(boolean textAsShapes, PSGenerator gen) {
            return new PSGraphics2D(true);
        }

        public void addFallbackFont(String name, Object font) {
            this.font = name;
        }

        public void paint(Graphics2D g2d, Rectangle2D area) {
        }

        public Dimension getImageSize() {
            return new Dimension();
        }
    }
}
