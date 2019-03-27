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

package org.apache.fop.pdf;

import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

import org.apache.fop.render.pdf.ImageRenderedAdapter;
import org.apache.fop.render.pdf.ImageRenderedAdapterTestCase;

public class PDFImageXObjectTestCase {

    /**
     * FOP-2847: tests whether images with index color model returns a valid color key mask</p>
     */
    @Test
    public void testPDFImageXObjectHasCorrectMaskForSemiTransparentIndexColorModel() {

        RenderedImage ri = ImageRenderedAdapterTestCase.createRenderedImageWithIndexColorModel(false);

        ImageRendered ir = mock(ImageRendered.class);
        when(ir.getRenderedImage()).thenReturn(ri);
        ImageInfo ii = mock(ImageInfo.class);
        when(ir.getInfo()).thenReturn(ii);
        ImageRenderedAdapter ira = new ImageRenderedAdapter(ir, "mock");

        PDFDocument doc = ImageRenderedAdapterTestCase.createPDFDocumentFromRenderedImage();
        ira.setup(doc);

        AbstractPDFStream pdfImageXObject = new PDFImageXObject(0, ira);
        pdfImageXObject.populateStreamDict(null);

        /*
         *  Currently FOP may generate a color key mask (/Mask) as well
         *  as the more flexible (/SMask) both for a single transparent image.
         *  That seems and actually is redundant, but it may help limited
         *  PDF viewers to show at least the fully transparent parts (/Mask),
         *  while omitting the translucent ones (/SMask).
         *
         *  If it contains a /Mask, then make sure it has only length 2.
         *  Length 2 actually means it holds the two bounds (min/max) as
         *  single 8-bit values see section 8.9.6.4 color key masking
         *  of PDF Spec 1.7.
         */
        assertTrue(ri.getColorModel() instanceof IndexColorModel);
        Object obj = pdfImageXObject.getDictionary().get("Mask");
        if (obj != null && obj instanceof PDFArray) {
            assertEquals(2, ((PDFArray) obj).length());
        }
    }

    /**
     * FOP-2847: tests whether images with index color model return a valid color key mask</p>
     */
    @Test
    public void testPDFImageXObjectHasCorrectMaskForFullyTransparentIndexColorModel() {

        RenderedImage ri = ImageRenderedAdapterTestCase.createRenderedImageWithIndexColorModel(true);

        ImageRendered ir = mock(ImageRendered.class);
        when(ir.getRenderedImage()).thenReturn(ri);
        ImageInfo ii = mock(ImageInfo.class);
        when(ir.getInfo()).thenReturn(ii);
        ImageRenderedAdapter ira = new ImageRenderedAdapter(ir, "mock");

        PDFDocument doc = ImageRenderedAdapterTestCase.createPDFDocumentFromRenderedImage();
        ira.setup(doc);

        AbstractPDFStream pdfImageXObject = new PDFImageXObject(0, ira);
        pdfImageXObject.populateStreamDict(null);

        assertTrue(ri.getColorModel() instanceof IndexColorModel);
        Object obj = pdfImageXObject.getDictionary().get("Mask");
        if (obj != null && obj instanceof PDFArray) {
            assertEquals(2, ((PDFArray) obj).length());
        }
    }
}
