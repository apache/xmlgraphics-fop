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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.fop.image.loader.batik.Graphics2DImagePainterImpl;
import org.apache.xmlgraphics.java2d.Graphics2DPainterPreparator;

/**
 * Graphics2DImagePainter implementation for GOCA
 */
public class Graphics2DImagePainterGOCA extends Graphics2DImagePainterImpl {

    /**
     * Main Constructor
     *
     * @param root the graphics node root
     * @param ctx the bridge context
     * @param imageSize the image size
     */
    public Graphics2DImagePainterGOCA(GraphicsNode root, BridgeContext ctx, Dimension imageSize) {
        super(root, ctx, imageSize);
    }

    /** {@inheritDoc} */
    protected Graphics2DPainterPreparator getPreparator() {
        return new Graphics2DPainterPreparator() {

            /** {@inheritdoc} */
            public void prepare(Graphics2D g2d, Rectangle2D area) {
                double tx = area.getX();
                double ty = area.getHeight() + area.getY();
                if (tx != 0 || ty != 0) {
                    g2d.translate(tx, ty);
                }

                float iw = (float) ctx.getDocumentSize().getWidth();
                float ih = (float) ctx.getDocumentSize().getHeight();
                float w = (float) area.getWidth();
                float h = (float) area.getHeight();
                float sx = w / iw;
                float sy = -(h / ih);
                if (sx != 1.0 || sy != 1.0) {
                    g2d.scale(sx, sy);
                }
            }
        };
    }
}