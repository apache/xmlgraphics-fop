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
package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.util.bitmap.BitmapImageUtil;

public final class PSPainterUtil {
    private PSPainterUtil() {
    }

    public static void drawTransparency(PSGenerator generator, Rectangle rect, Paint fill) {
        PSGraphics2D graphics = new PSGraphics2D(true, generator);
        graphics.setGraphicContext(new GraphicContext());
        BufferedImage image = buildImage((Color) fill, rect.width / 1000, rect.height / 1000);
        RenderedImage mask = buildMaskImage(image, rect.width / 1000, rect.height / 1000);
        graphics.drawImage(image, rect.x / 1000, rect.y / 1000, null, null, mask);
    }

    private static BufferedImage buildImage(Color color, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        Color alpha = new Color(color.getAlpha(), color.getAlpha(), color.getAlpha());
        graphics.setColor(alpha);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return bufferedImage;
    }

    private static RenderedImage buildMaskImage(BufferedImage image, int width, int height) {
        return BitmapImageUtil.convertToMonochrome(image, new Dimension(width, height), 1);
    }
}
