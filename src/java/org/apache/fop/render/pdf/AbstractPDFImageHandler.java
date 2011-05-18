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

package org.apache.fop.render.pdf;

import java.awt.Rectangle;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;

import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.pdf.PDFLogicalStructureHandler.MarkedContentInfo;

/**
 * A partial implementation of a PDF-specific image handler, containing the code that is
 * common between image flavors.
 */
abstract class AbstractPDFImageHandler implements ImageHandler {

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        PDFRenderingContext pdfContext = (PDFRenderingContext)context;
        PDFContentGenerator generator = pdfContext.getGenerator();
        PDFImage pdfimage = createPDFImage(image, image.getInfo().getOriginalURI());
        PDFXObject xobj = generator.getDocument().addImage(
                generator.getResourceContext(), pdfimage);

        float x = (float)pos.getX() / 1000f;
        float y = (float)pos.getY() / 1000f;
        float w = (float)pos.getWidth() / 1000f;
        float h = (float)pos.getHeight() / 1000f;
        if (context.getUserAgent().isAccessibilityEnabled()) {
            MarkedContentInfo mci = pdfContext.getMarkedContentInfo();
            generator.placeImage(x, y, w, h, xobj, mci.tag, mci.mcid);
        } else {
            generator.placeImage(x, y, w, h, xobj);
        }
    }

    /**
     * Creates a PDF image object out of the given image.
     *
     * @param image an image
     * @param xobjectKey a key for retrieval of the image from the document's XObject collection
     * @return a suitable {@link PDFImage} implementation that can handle the flavour of
     * the given image
     */
    abstract PDFImage createPDFImage(Image image, String xobjectKey);
}
