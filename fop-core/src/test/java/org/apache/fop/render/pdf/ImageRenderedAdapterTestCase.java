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

import java.awt.Color;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFImageXObject;
import org.apache.fop.pdf.PDFProfile;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;

public class ImageRenderedAdapterTestCase {

    /**
     * tests whether ARGB images return a soft mask
     */
    @Test
    public void testSetupWithARGBReturnsSoftMask() {
        RenderedImage ri = createRenderedImageWithRGBA();

        ImageRendered ir = mock(ImageRendered.class);
        when(ir.getRenderedImage()).thenReturn(ri);
        ImageInfo ii = mock(ImageInfo.class);
        when(ir.getInfo()).thenReturn(ii);
        ImageRenderedAdapter ira = new ImageRenderedAdapter(ir, "mock");

        PDFDocument doc = createPDFDocumentFromRenderedImage();
        PDFDictionary dict = new PDFDictionary();
        ira.setup(doc);
        ira.populateXObjectDictionary(dict);
        assertNotNull(ira.getSoftMaskReference());
    }

    /**
     * FOP-2847: tests whether images with index color model return a soft mask</p>
     */
    @Test
    public void testSetupWithIndexColorModelSemiTransparentReturnsSoftMask() {
        RenderedImage ri = createRenderedImageWithIndexColorModel(false);

        ImageRendered ir = mock(ImageRendered.class);
        when(ir.getRenderedImage()).thenReturn(ri);
        ImageInfo ii = mock(ImageInfo.class);
        when(ir.getInfo()).thenReturn(ii);
        ImageRenderedAdapter ira = new ImageRenderedAdapter(ir, "mock");

        PDFDocument doc = createPDFDocumentFromRenderedImage();
        PDFDictionary dict = new PDFDictionary();
        ira.setup(doc);
        ira.populateXObjectDictionary(dict);
        assertNotNull(ira.getSoftMaskReference());
    }

    /**
     * FOP-2847: tests whether images with index color model return a soft mask</p>
     */
    @Test
    public void testSetupWithIndexColorModelFullyTransparentReturnsSoftMask() {
        RenderedImage ri = createRenderedImageWithIndexColorModel(true);

        ImageRendered ir = mock(ImageRendered.class);
        when(ir.getRenderedImage()).thenReturn(ri);
        ImageInfo ii = mock(ImageInfo.class);
        when(ir.getInfo()).thenReturn(ii);
        ImageRenderedAdapter ira = new ImageRenderedAdapter(ir, "mock");

        PDFDocument doc = createPDFDocumentFromRenderedImage();
        PDFDictionary dict = new PDFDictionary();
        ira.setup(doc);
        ira.populateXObjectDictionary(dict);
        assertNotNull(ira.getSoftMaskReference());
    }

    /**
     * Creates a semi transparent 4x4 image in index color space.
     *
     * @param fullyTransparent true if image is supposed to have a fully
     *        transparent color
     * @return RenderedImage
     */
    public static RenderedImage createRenderedImageWithIndexColorModel(boolean fullyTransparent) {
        /*
         *  Define an index color model with just four colors. For reasons of
         *  simplicity colors will be gray.
         */
        IndexColorModel cm;
        if (fullyTransparent) {
            byte[] i = {(byte)0x00, (byte)0x80, (byte)0xB0, (byte)0xF0};
            cm = new IndexColorModel(8, 4, i, i, i, i);
        } else {
            byte[] i = {(byte)0x10, (byte)0x80, (byte)0xB0, (byte)0xF0};
            cm = new IndexColorModel(8, 4, i, i, i, i);
        }

        // create a 4x4 image with just one uniform color
        BufferedImage ri = new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_INDEXED, cm);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                Color c =  new Color(128, 128, 128, 128);
                ri.setRGB(x, y, c.getRGB());
            }
        }
        return ri;
    }

    /**
     * creates a semi transparent 4x4 image in ABGR color space
     *
     * @return RenderedImage
     */
    static RenderedImage createRenderedImageWithRGBA() {
        // create a 4x4 image
        BufferedImage ri = new BufferedImage(4, 4, BufferedImage.TYPE_4BYTE_ABGR);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                Color c =  new Color(128, 128, 128, 128);
                ri.setRGB(x, y, c.getRGB());
            }
        }
        return ri;
    }

    /**
     * Create a mocked PDF document from RenderedImage.
     *
     * @return
     */
    public static PDFDocument createPDFDocumentFromRenderedImage() {
        // mock PDFDocument
        PDFDocument doc = mock(PDFDocument.class);
        PDFResources resources = mock(PDFResources.class);
        when(doc.getResources()).thenReturn(resources);

        PDFProfile profile = mock(PDFProfile.class);
        when(profile.getPDFAMode()).thenReturn(PDFAMode.PDFA_2A);

        PDFImageXObject pio = new PDFImageXObject(0, null);
        pio.setObjectNumber(0);
        when(doc.getProfile()).thenReturn(profile);
        when(doc.addImage(any(PDFResourceContext.class), any(PDFImage.class))).thenReturn(pio);

        // ICC Color info
        PDFFactory factory = mock(PDFFactory.class);
        PDFICCStream iccStream = mock(PDFICCStream.class);
        ICC_Profile iccProfile = mock(ICC_Profile.class);
        when(iccProfile.getNumComponents()).thenReturn(4);
        when(iccStream.getICCProfile()).thenReturn(iccProfile);
        when(factory.makePDFICCStream()).thenReturn(iccStream);
        PDFICCBasedColorSpace iccbcs = new PDFICCBasedColorSpace(null, iccStream);
        when(factory.makeICCBasedColorSpace(null, null, iccStream)).thenReturn(iccbcs);
        when(doc.getFactory()).thenReturn(factory);

        return doc;
    }
}
