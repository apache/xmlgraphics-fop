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
package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;

public class Graphics2DImagePainterImplTestCase {
    @Test
    public void testScale() {
        GraphicsNode graphicsNode = mock(GraphicsNode.class);
        BridgeContext bridgeContext = mock(BridgeContext.class);
        when(bridgeContext.getDocumentSize()).thenReturn(new Dimension(1010, 1010));
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        Graphics2DImagePainterImpl graphics2DImagePainter =
                new Graphics2DImagePainterImpl(graphicsNode, bridgeContext, null);
        graphics2DImagePainter.paint(graphics2D, new Rectangle(0, 0, 1000, 1000));
        Assert.assertEquals(graphics2D.getTransform().getScaleX(), 0.99, 0);
    }
}
