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

package org.apache.fop.render.afp;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.fop.image.loader.batik.GenericGraphics2DImagePainter;
import org.apache.fop.image.loader.batik.ImageConverterSVG2G2D;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

public class AFPImageConverterSVG2G2D extends ImageConverterSVG2G2D {

    /** {@inheritDoc} */
    protected Graphics2DImagePainter createPainter(
            final ImageXMLDOM svg, final BridgeContext ctx, final GraphicsNode root) {
        return new AFPGraphics2DImagePainter(svg, ctx, root);
    }

    private class AFPGraphics2DImagePainter extends GenericGraphics2DImagePainter {
        /**
         * Constructor
         *
         * @param svg the svg image dom
         * @param ctx the bridge context
         * @param root the graphics node root
         */
        public AFPGraphics2DImagePainter(ImageXMLDOM svg, BridgeContext ctx,
                GraphicsNode root) {
            super(svg, ctx, root);
        }

        /** {@inheritDoc} */
        protected void init(Graphics2D g2d, Rectangle2D area) {
            float iw = (float) ctx.getDocumentSize().getWidth();
            float ih = (float) ctx.getDocumentSize().getHeight();
            float w = (float) area.getWidth();
            float h = (float) area.getHeight();
            float sx = w / iw;
            float sy = -(h / ih);
            if (sx != 1.0 || sy != 1.0) {
                g2d.scale(sx, sy);
            }

            double tx = area.getX();
            double ty = area.getY() - area.getHeight();
            if (tx != 0 || ty != 0) {
                g2d.translate(tx, ty);
            }
        }

    }

}
