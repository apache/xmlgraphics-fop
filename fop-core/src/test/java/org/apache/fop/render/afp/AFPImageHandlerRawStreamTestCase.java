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

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;

public class AFPImageHandlerRawStreamTestCase {
    @Test
    public void testIsCompatible() {
        AFPPaintingState state = new AFPPaintingState();
        state.setNativeImagesSupported(true);
        AFPRenderingContext context = new AFPRenderingContext(null, null, state, null, null);
        ImageRawStream stream = new ImageRawStream(null, ImageFlavor.RAW_PDF, (InputStream) null);
        Assert.assertTrue(new AFPImageHandlerRawStream().isCompatible(context, stream));
        Assert.assertFalse(Arrays.asList(
                new AFPImageHandlerRawStream().getSupportedImageFlavors()).contains(ImageFlavor.RAW));
    }

    @Test
    public void testPDFIsCompatible() {
        AFPPaintingState state = new AFPPaintingState();
        state.setNativePDFImagesSupported(true);
        AFPRenderingContext context = new AFPRenderingContext(null, null, state, null, null);
        ImageRawStream stream = new ImageRawStream(null, ImageFlavor.RAW_PDF, (InputStream) null);
        Assert.assertTrue(new AFPImageHandlerRawStream().isCompatible(context, stream));
        stream = new ImageRawStream(null, ImageFlavor.RAW_JPEG, (InputStream) null);
        Assert.assertFalse(new AFPImageHandlerRawStream().isCompatible(context, stream));
    }

    @Test
    public void testMetadataInObjectContainer() throws Exception {
        Map<String, byte[]> fields = drawPDF();
        Assert.assertEquals(8, fields.get("Object Environment Group (OEG)").length);
        Assert.assertEquals(123, fields.get("Data Resource").length);
    }

    private Map<String, byte[]> drawPDF() throws Exception {
        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setMetadataInObjectContainer(true);
        AFPImageHandlerRawStream handler = new AFPImageHandlerRawStream();
        FOUserAgent agent = FopFactory.newInstance(new URI(".")).newFOUserAgent();
        InternalResourceResolver resourceResolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new URI("."));
        AFPResourceManager manager = new AFPResourceManager(resourceResolver);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataStream dataStream = manager.createDataStream(paintingState, stream);
        dataStream.startDocument();
        dataStream.startPage(1, 1, 0, 1, 1);
        AFPRenderingContext context =
                new AFPRenderingContext(agent, manager, paintingState, null, new HashMap());
        ImageInfo info = new ImageInfo("x.pdf#page=1", MimeConstants.MIME_PDF);
        info.setSize(new ImageSize());
        ImageRawStream img = new ImageRawStream(info, ImageFlavor.RAW_PDF, new ByteArrayInputStream(new byte[0]));
        handler.handleImage(context, img, new Rectangle());
        dataStream.endPage();
        dataStream.endDocument();
        manager.writeToStream();
        MODCAParser parser = new MODCAParser(new ByteArrayInputStream(stream.toByteArray()));
        UnparsedStructuredField structuredField;
        Map<String, byte[]> fields = new HashMap<>();
        while ((structuredField = parser.readNextStructuredField()) != null) {
            fields.put(structuredField.getCategoryCodeAsString(), structuredField.getData());
        }
        return fields;
    }
}
