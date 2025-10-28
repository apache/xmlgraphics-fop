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

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.java2d.GraphicContext;

import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.afp.modca.ObjectAreaDescriptor;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;

public class AFPGraphics2DTestCase {

    private final float lineWidth = 1.0f;
    private final float correction = 2.5f;
    private final BasicStroke stroke = mock(BasicStroke.class);
    private final GraphicsObject gObject = mock(GraphicsObject.class);
    private final AFPPaintingState paintingState = mock(AFPPaintingState.class);
    private final AFPResourceManager resourceManager = mock(AFPResourceManager.class);
    private final AFPResourceInfo resourceInfo = mock(AFPResourceInfo.class);
    private final FontInfo fontInfo = mock(FontInfo.class);
    private AFPGraphics2D graphics2D = new AFPGraphics2D(false, paintingState, resourceManager, resourceInfo,
            fontInfo);

    @Test
    public void testApplyStroke() {
        // note: this only tests the setLineWidth in the GraphicsObject
        float correctedLineWidth = lineWidth * correction;
        when(stroke.getLineWidth()).thenReturn(lineWidth);
        when(paintingState.getLineWidthCorrection()).thenReturn(correction);
        graphics2D.setGraphicsObject(gObject);
        graphics2D.applyStroke(stroke);
        verify(gObject).setLineWidth(correctedLineWidth);
    }

    @Test
    public void testDrawImageMask() throws IOException {
        BufferedImage image = ImageIO.read(new File("test/resources/images/fop-logo-color-palette-8bit.png"));
        byte[] data = getAFPField("Data Image", image, true);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        bis.skip(56);
        Assert.assertEquals(bis.read(), 0x8E); //start mask
        bis.skip(20);
        byte[] firstchunk = new byte[5272];
        bis.read(firstchunk);
        int maskbytes = 0;
        for (byte b : firstchunk) {
            if (b != 0) {
                maskbytes++;
            }
        }
        Assert.assertEquals(maskbytes, 333);
        bis.skip(38117 - 57 - 20 - firstchunk.length);
        Assert.assertEquals(bis.read(), 0x8F); //end mask
        Assert.assertEquals(bis.available(), 302498);
    }

    private byte[] getAFPField(String field, BufferedImage image, boolean maskEnabled) throws IOException {
        FOUserAgent foUserAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPResourceManager afpResourceManager = new AFPResourceManager(foUserAgent.getResourceResolver());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataStream dataStream = afpResourceManager.createDataStream(null, byteArrayOutputStream);
        dataStream.startDocument();
        dataStream.startPage(1, 1, 0, 1, 1);
        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setMaskEnabled(maskEnabled);
        graphics2D = new AFPGraphics2D(false, paintingState, afpResourceManager, resourceInfo, fontInfo);
        graphics2D.setGraphicContext(new GraphicContext());
        GraphicsObject graphicsObject = new GraphicsObject(new Factory(), null);
        graphics2D.setGraphicsObject(graphicsObject);
        graphicsObject.getObjectEnvironmentGroup().setObjectAreaDescriptor(new ObjectAreaDescriptor(1, 1, 1, 1));
        graphics2D.drawRenderedImage(image, AffineTransform.getTranslateInstance(1000, 1000));
        dataStream.endPage();
        dataStream.endDocument();
        afpResourceManager.writeToStream();
        MODCAParser parser = new MODCAParser(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UnparsedStructuredField structuredField;
        while ((structuredField = parser.readNextStructuredField()) != null) {
            if (structuredField.toString().contains(field)) {
                bos.write(structuredField.getData());
            }
        }
        return bos.toByteArray();
    }

    @Test
    public void testDrawGraphicsFillet() throws IOException {
        GraphicContext gc = new GraphicContext();
        gc.setClip(new Rectangle(0, 0, 100, 100));
        graphics2D.setGraphicContext(gc);
        GraphicsObject go = new GraphicsObject(new Factory(), "test");
        graphics2D.setGraphicsObject(go);
        graphics2D.draw(new Area(new Ellipse2D.Double(0, 0, 100, 100)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        go.writeToStream(bos);
        ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray());
        is.skip(17 + 9 + 14 + 6);
        int graphicsFilletMarker = 0x85;
        Assert.assertEquals(is.read(), graphicsFilletMarker);
        int sizeOfGraphicsFillet = 128;
        Assert.assertEquals(is.read(), sizeOfGraphicsFillet);
    }

    @Test
    public void testDrawGraphicsFilletClipped() throws IOException {
        GraphicContext gc = new GraphicContext();
        gc.setClip(new Rectangle(50, 50, 100, 100));
        graphics2D.setGraphicContext(gc);
        GraphicsObject go = new GraphicsObject(new Factory(), "test");
        graphics2D.setGraphicsObject(go);
        graphics2D.draw(new Ellipse2D.Double(0, 0, 50, 50));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        go.writeToStream(bos);
        Assert.assertEquals(bos.size(), 17 + 17);
    }
}
