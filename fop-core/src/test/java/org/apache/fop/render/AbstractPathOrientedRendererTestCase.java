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

package org.apache.fop.render;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Trait;

public class AbstractPathOrientedRendererTestCase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDrawBackgroundWithTargetImageSizes() {
        FOUserAgent userAgent = mock(FOUserAgent.class);
        MyAPOR myAPOR = new MyAPOR(userAgent);
        ImageSize imgSize = new ImageSize(300, 300, 300);
        imgSize.setSizeInMillipoints(72000, 72000);
        ImageInfo imgInfo = new ImageInfo(null, null);
        imgInfo.setSize(imgSize);
        Trait.Background background = new Trait.Background();
        background.setImageTargetWidth(300000);
        background.setImageTargetHeight(300000);
        background.setImageInfo(imgInfo);
        myAPOR.drawBackground(0, 0, 600, 900, background, null, null, null, null);
        String expected = "[x=0.0,y=0.0,w=3.0,h=3.0][x=0.0,y=3.0,w=3.0,h=3.0][x=0.0,y=6.0,w=3.0,h=3.0]"
                + "[x=0.0,y=9.0,w=3.0,h=3.0][x=3.0,y=0.0,w=3.0,h=3.0][x=3.0,y=3.0,w=3.0,h=3.0]"
                + "[x=3.0,y=6.0,w=3.0,h=3.0][x=3.0,y=9.0,w=3.0,h=3.0][x=6.0,y=0.0,w=3.0,h=3.0]"
                + "[x=6.0,y=3.0,w=3.0,h=3.0][x=6.0,y=6.0,w=3.0,h=3.0][x=6.0,y=9.0,w=3.0,h=3.0]";
        assertEquals(expected, myAPOR.getActual().replaceAll("00000", ""));
        myAPOR.resetActual();
        background.setImageTargetWidth(0);
        myAPOR.drawBackground(0, 0, 600, 900, background, null, null, null, null);
        assertEquals(expected, myAPOR.getActual().replaceAll("00000", ""));
        myAPOR.resetActual();
        background.setImageTargetWidth(300000);
        background.setImageTargetHeight(0);
        myAPOR.drawBackground(0, 0, 600, 900, background, null, null, null, null);
        assertEquals(expected, myAPOR.getActual().replaceAll("00000", ""));
    }

    private class MyAPOR extends AbstractPathOrientedRenderer {

        private String actual = "";

        public MyAPOR(FOUserAgent userAgent) {
            super(userAgent);
        }

        public String getActual() {
            return actual;
        }

        public void resetActual() {
            actual = "";
        }

        public String getMimeType() {
            return null;
        }

        protected void concatenateTransformationMatrix(AffineTransform at) {
        }

        protected void restoreStateStackAfterBreakOut(List breakOutList) {
        }

        protected List breakOutOfStateStack() {
            return null;
        }

        protected void saveGraphicsState() {
        }

        protected void restoreGraphicsState() {
        }

        protected void beginTextObject() {
        }

        protected void endTextObject() {
        }

        protected void clip() {
        }

        protected void clipRect(float x, float y, float width, float height) {
        }

        protected void moveTo(float x, float y) {
        }

        protected void lineTo(float x, float y) {
        }

        protected void closePath() {
        }

        protected void fillRect(float x, float y, float width, float height) {
        }

        protected void updateColor(Color col, boolean fill) {
        }

        protected void drawImage(String url, Rectangle2D pos, Map foreignAttributes) {
            String s = pos.toString();
            actual += s.substring(s.indexOf('['));
        }

        protected void drawBorderLine(float x1, float y1, float x2, float y2, boolean horz,
                boolean startOrBefore, int style, Color col) {
        }

        protected void startVParea(CTM ctm, Rectangle clippingRect) {
        }

        protected void endVParea() {
        }

        protected void startLayer(String layer) {
        }

        protected void endLayer() {
        }

    }
}
