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

package org.apache.fop.render.intermediate;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.traits.BorderProps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractIFPainterTestCase {

    private AbstractIFPainter<?> sut;
    private IFDocumentHandler handler;

    @Before
    public void setUp() {
        handler = mock(IFDocumentHandler.class);
        sut = new AbstractIFPainter<IFDocumentHandler>(handler) {
            public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
                    throws IFException {
            }

            public void endViewport() throws IFException {
            }

            public void startGroup(AffineTransform transform) throws IFException {
            }

            public void endGroup() throws IFException {
            }

            public void clipRect(Rectangle rect) throws IFException {
            }

            public void fillRect(Rectangle rect, Paint fill) throws IFException {
            }

            public void drawImage(String uri, Rectangle rect) throws IFException {
            }

            public void drawImage(Document doc, Rectangle rect) throws IFException {
            }

            @Override
            protected RenderingContext createRenderingContext() {
                return null;
            }

            public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp,
                    String text) throws IFException {
            }

            public void clipBackground(Rectangle rect, BorderProps bpsBefore,
                    BorderProps bpsAfter, BorderProps bpsStart,
                    BorderProps bpsEnd) throws IFException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException();
            }
        };
        FontInfo fontInfo = mock(FontInfo.class);
        when(handler.getFontInfo()).thenReturn(fontInfo);
    }

    @Test
    public void testGetFontKey() throws IFException {
        String expected = "the expected string";
        FontTriplet triplet = mock(FontTriplet.class);
        FontInfo fontInfo = handler.getFontInfo();
        when(fontInfo.getInternalFontKey(triplet)).thenReturn(expected);
        assertEquals(expected, sut.getFontKey(triplet));
    }

    @Test(expected = IFException.class)
    public void testGetFontKeyMissingFont() throws IFException {
        FontTriplet triplet = mock(FontTriplet.class);
        when(handler.getFontInfo().getInternalFontKey(triplet)).thenReturn(null);
        sut.getFontKey(triplet);
    }
}
