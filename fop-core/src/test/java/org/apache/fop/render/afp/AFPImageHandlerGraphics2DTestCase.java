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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.DataStream;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;

public class AFPImageHandlerGraphics2DTestCase {

    @Test
    public void testWrapGocaPSeg() throws IOException {
        ImageInfo info = new ImageInfo(null, null);
        info.setSize(new ImageSize(100, 100, 72));
        ImageGraphics2D imageGraphics2D = new ImageGraphics2D(info, new Graphics2DImagePainter() {
            public void paint(Graphics2D g2d, Rectangle2D area) {
            }
            public Dimension getImageSize() {
                return null;
            }
        });

        AFPPaintingState paintingState = new AFPPaintingState();
        paintingState.setWrapGocaPSeg(true);

        FOUserAgent foUserAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPResourceManager resourceManager = new AFPResourceManager(foUserAgent.getResourceResolver());

        AFPResourceLevelDefaults resourceLevelDefaults = new AFPResourceLevelDefaults();
        resourceLevelDefaults.setDefaultResourceLevel("goca", AFPResourceLevel.valueOf("print-file"));
        resourceManager.setResourceLevelDefaults(resourceLevelDefaults);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataStream dataStream = resourceManager.createDataStream(null, bos);
        dataStream.startPage(0, 0, 0, 0, 0);
        AFPRenderingContext afpRenderingContext =
                new AFPRenderingContext(null, resourceManager, paintingState, null, null);
        AFPImageHandlerGraphics2D imageHandlerGraphics2D = new AFPImageHandlerGraphics2D();
        imageHandlerGraphics2D.handleImage(afpRenderingContext, imageGraphics2D, new Rectangle());

        StringBuilder sb = new StringBuilder();
        new AFPParser(true).read(new ByteArrayInputStream(bos.toByteArray()), sb);
        Assert.assertEquals(sb.toString(), "BEGIN RESOURCE_GROUP RG000001\n"
                + "BEGIN NAME_RESOURCE RES00001 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION,\n"
                + "BEGIN PAGE_SEGMENT S1000001\n"
                + "BEGIN GRAPHICS S1000001\n"
                + "BEGIN OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                + "DESCRIPTOR OBJECT_AREA Triplets: DESCRIPTOR_POSITION,MEASUREMENT_UNITS,OBJECT_AREA_SIZE,\n"
                + "POSITION OBJECT_AREA\n"
                + "DESCRIPTOR GRAPHICS\n"
                + "END OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                + "END GRAPHICS S1000001\n"
                + "END PAGE_SEGMENT S1000001\n"
                + "END NAME_RESOURCE RES00001\n");
    }
}
