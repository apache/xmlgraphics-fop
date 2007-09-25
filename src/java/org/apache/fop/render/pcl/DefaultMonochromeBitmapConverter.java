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

package org.apache.fop.render.pcl;

import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;

/**
 * Default implementation of the MonochromeBitmapConverter which uses the Java Class Library
 * to convert grayscale bitmaps to monochrome bitmaps.
 */
public class DefaultMonochromeBitmapConverter implements
        MonochromeBitmapConverter {

    /** {@inheritDoc} */
    public void setHint(String name, String value) {
        //ignore, not supported
    }
    
    /** {@inheritDoc} */
    public RenderedImage convertToMonochrome(BufferedImage img) {
        BufferedImage buf = new BufferedImage(img.getWidth(), img.getHeight(), 
                BufferedImage.TYPE_BYTE_BINARY);
        RenderingHints hints = new RenderingHints(null);
        //This hint doesn't seem to make a difference :-(
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), hints);
        op.filter(img, buf);
        return buf;
    }

}
