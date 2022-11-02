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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.java2d.GraphicContext;

import org.apache.fop.afp.modca.GraphicsObject;
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
    public void testDrawGraphicsFillet() throws IOException {
        GraphicContext gc = new GraphicContext();
        gc.setClip(new Rectangle(0, 0, 2, 2));
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
}
