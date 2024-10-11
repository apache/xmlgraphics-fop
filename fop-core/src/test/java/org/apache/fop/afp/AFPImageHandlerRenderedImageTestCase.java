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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;

import org.apache.fop.render.afp.AFPImageHandlerRenderedImage;
import org.apache.fop.render.afp.AFPParser;
import org.apache.fop.render.afp.AFPRenderingContext;

/**
 * A test class for testing AFP events.
 */
public class AFPImageHandlerRenderedImageTestCase {

    class MyAFPResourceManager extends AFPResourceManager {
        AFPDataObjectInfo dataObjectInfo;

        MyAFPResourceManager() {
            super(null);
        }

        public void createObject(AFPDataObjectInfo dataObjectInfo) {
            this.dataObjectInfo = dataObjectInfo;
        }
    }

    @Test
    public void testAfpUsesObjectContainerUseIocaImagesFalse() throws IOException {
        runAfpImageTest(false,
                "Must use an object container when use IOCA images is false",
                "BEGIN RESOURCE_GROUP RG000001\n"
                        + "BEGIN NAME_RESOURCE RES00001 Triplets: "
                        + "OBJECT_FUNCTION_SET_SPECIFICATION,OBJECT_CLASSIFICATION,\n"
                        + "BEGIN OBJECT_CONTAINER OC000001 Triplets: 0x01,0x00,0x00,\n"
                        + "DATA OBJECT_CONTAINER\n"
                        + "DATA OBJECT_CONTAINER\n"
                        + "END OBJECT_CONTAINER OC000001\n"
                        + "END NAME_RESOURCE RES00001\n");
    }

    @Test
    public void testAfpUsesImageByDefault() throws IOException {
        runAfpImageTest(true, "Must use an IOCA image structure",
                "BEGIN RESOURCE_GROUP RG000001\n"
                        + "BEGIN NAME_RESOURCE RES00001 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION,\n"
                        + "BEGIN IMAGE IMG00001\n"
                        + "BEGIN OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                        + "DESCRIPTOR OBJECT_AREA Triplets: DESCRIPTOR_POSITION,MEASUREMENT_UNITS,OBJECT_AREA_SIZE,\n"
                        + "POSITION OBJECT_AREA\n"
                        + "MAP IMAGE Triplets: MAPPING_OPTION,\n"
                        + "DESCRIPTOR IMAGE\n"
                        + "END OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                        + "DATA IMAGE\n"
                        + "DATA IMAGE\n"
                        + "END IMAGE IMG00001\n"
                        + "END NAME_RESOURCE RES00001\n");
    }

    private void runAfpImageTest(boolean useIocaImages, String assertionMessage, String afpContent) throws IOException {
        InternalResourceResolver rr =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        AFPResourceManager afpResourceManager = new AFPResourceManager(rr);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        AFPPaintingState paintingState = new AFPPaintingState();
        assertTrue("Use IOCA images must be true by default", paintingState.isUseIocaImages());
        paintingState.setUseIocaImages(useIocaImages);

        DataStream ds = afpResourceManager.createDataStream(null, bos);
        ds.startPage(0, 0, 0, 0, 0);

        handleImage(BufferedImage.TYPE_INT_ARGB, afpResourceManager, paintingState);

        StringBuilder sb = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        new AFPParser(false).read(bis, sb);

        assertEquals(assertionMessage, afpContent, sb.toString());
    }

    @Test
    public void checkMimeTypeTrueUseIocaImages() throws IOException {
        MyAFPResourceManager afpResourceManager = new MyAFPResourceManager();

        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setUseIocaImages(true);
        paintingState.setBitsPerPixel(24);

        handleImage(BufferedImage.TYPE_BYTE_GRAY, afpResourceManager, paintingState);
        assertEquals("Must not use image/jpeg as it will be set as an ioca image",
                "image/x-afp+fs11", afpResourceManager.dataObjectInfo.getMimeType());
    }

    @Test
    public void checkMimeTypeFalseUseIocaImages() throws IOException {
        MyAFPResourceManager afpResourceManager = new MyAFPResourceManager();

        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setBitsPerPixel(8);

        paintingState.setUseIocaImages(false);
        handleImage(BufferedImage.TYPE_BYTE_GRAY, afpResourceManager, paintingState);
        assertEquals("Must use image/jpeg and the image will be stored using an object container",
                "image/jpeg", afpResourceManager.dataObjectInfo.getMimeType());
    }

    private void handleImage(int type, AFPResourceManager afpResourceManager, AFPPaintingState paintingState)
            throws IOException {
        BufferedImage img = new BufferedImage(100, 100, type);
        ImageInfo info = new ImageInfo("a", null);
        info.setSize(new ImageSize(100, 100, 72));
        ImageRendered imageRendered = new ImageRendered(info, img, null);
        AFPImageHandlerRenderedImage imageHandlerRenderedImage = new AFPImageHandlerRenderedImage();
        AFPRenderingContext afpRenderingContext = new AFPRenderingContext(null, afpResourceManager,
                paintingState, null, null);
        imageHandlerRenderedImage.handleImage(afpRenderingContext, imageRendered, new Rectangle());
    }
}
