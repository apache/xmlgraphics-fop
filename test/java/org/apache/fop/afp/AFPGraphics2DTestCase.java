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

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

}
