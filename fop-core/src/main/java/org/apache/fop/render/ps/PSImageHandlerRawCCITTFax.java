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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.ps.FormGenerator;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.ImageFormGenerator;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSImageUtils;

import org.apache.fop.render.RenderingContext;

/**
 * Image handler implementation which handles CCITT fax images for PostScript output.
 */
public class PSImageHandlerRawCCITTFax implements PSImageHandler {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_CCITTFAX
    };

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
                throws IOException {
        PSRenderingContext psContext = (PSRenderingContext)context;
        PSGenerator gen = psContext.getGenerator();
        ImageRawCCITTFax ccitt = (ImageRawCCITTFax)image;

        float x = (float)pos.getX() / 1000f;
        float y = (float)pos.getY() / 1000f;
        float w = (float)pos.getWidth() / 1000f;
        float h = (float)pos.getHeight() / 1000f;
        Rectangle2D targetRect = new Rectangle2D.Float(
                x, y, w, h);

        ImageInfo info = image.getInfo();

        ImageEncoder encoder = new ImageEncoderCCITTFax(ccitt);
        PSImageUtils.writeImage(encoder, info.getSize().getDimensionPx(),
                info.getOriginalURI(), targetRect,
                ccitt.getColorSpace(), 1, false, gen);
    }

    /** {@inheritDoc} */
    public void generateForm(RenderingContext context, Image image, PSImageFormResource form)
                throws IOException {
        PSRenderingContext psContext = (PSRenderingContext)context;
        PSGenerator gen = psContext.getGenerator();
        ImageRawCCITTFax ccitt = (ImageRawCCITTFax)image;
        ImageInfo info = image.getInfo();
        String imageDescription = info.getMimeType() + " " + info.getOriginalURI();

        ImageEncoder encoder = new ImageEncoderCCITTFax(ccitt);
        FormGenerator formGen = new ImageFormGenerator(
                form.getName(), imageDescription,
                info.getSize().getDimensionPt(),
                info.getSize().getDimensionPx(),
                encoder,
                ccitt.getColorSpace(), 1, false);
        formGen.generate(gen);
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 200;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRawCCITTFax.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        if (targetContext instanceof PSRenderingContext) {
            PSRenderingContext psContext = (PSRenderingContext)targetContext;
            //The filters required for this implementation need PS level 2 or higher
            if (psContext.getGenerator().getPSLevel() >= 2) {
                return (image == null || image instanceof ImageRawCCITTFax);
            }
        }
        return false;
    }

}
