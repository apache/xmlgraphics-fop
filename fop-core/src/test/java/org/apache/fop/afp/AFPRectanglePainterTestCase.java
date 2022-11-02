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
package org.apache.fop.afp;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.util.bitmap.BitmapImageUtil;
import org.apache.fop.util.bitmap.DefaultMonochromeBitmapConverter;

public class AFPRectanglePainterTestCase {
    @Test
    public void testCMYKTransparencyMask() throws Exception {
        float cyan = 0.5f;
        Color deviceColor = new Color(ColorSpaces.getDeviceCMYKColorSpace(), new float[]{cyan, 0, 0, 0}, 0.4f);
        ByteArrayInputStream bis = buildAFP(new ColorWithAlternatives(0, 0, 0, 128, new Color[]{deviceColor}));
        bis.skip(59);
        Assert.assertEquals(bis.read(), 0x8E); //start
        bis.skip(20);
        if (BitmapImageUtil.createDefaultMonochromeBitmapConverter() instanceof DefaultMonochromeBitmapConverter) {
            Assert.assertEquals(bis.read(), 255); //mask data
        } else {
            Assert.assertEquals(bis.read(), 170); //mask data
        }
        bis.skip(1299);
        Assert.assertEquals(bis.read(), 0x8F); //end
        bis.skip(89);
        Assert.assertEquals(bis.read(), 127); //cyan byte
    }

    @Test
    public void testRGBTransparencyMask() throws Exception {
        ByteArrayInputStream bis = buildAFP(new Color(0, 0, 0, 128));
        Assert.assertNull(bis);
    }

    private ByteArrayInputStream buildAFP(Color color) throws Exception {
        FOUserAgent foUserAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setColor(color);
        AFPResourceManager resourceManager = new AFPResourceManager(foUserAgent.getResourceResolver());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataStream stream = resourceManager.createDataStream(paintingState, bos);
        stream.startDocument();
        stream.startPage(100, 100, 0, 10, 10);
        AFPRectanglePainter painter = new AFPRectanglePainter(paintingState, stream, resourceManager);
        painter.paint(new RectanglePaintingInfo(0, 0, 100, 100));
        stream.endPage();
        stream.endDocument();
        resourceManager.writeToStream();

        MODCAParser parser = new MODCAParser(new ByteArrayInputStream(bos.toByteArray()));
        UnparsedStructuredField structuredField;
        while ((structuredField = parser.readNextStructuredField()) != null) {
            if (structuredField.toString().endsWith("Data Image")) {
                return new ByteArrayInputStream(structuredField.getData());
            }
        }
        return null;
    }
}
