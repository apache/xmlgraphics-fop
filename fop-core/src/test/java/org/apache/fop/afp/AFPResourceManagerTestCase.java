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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.render.afp.AFPParser;

/**
 * Test case for {@link AFPResourceManager}.
 */
public class AFPResourceManagerTestCase {

    private AFPResourceManager sut;

    @Before
    public void setUp() throws IOException {
        sut = new AFPResourceManager(ResourceResolverFactory.createDefaultInternalResourceResolver(
                                                            new File(".").toURI()));
        AFPPaintingState paintingState = new AFPPaintingState();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataStream stream = sut.createDataStream(paintingState, outStream);
        stream.startPage(0, 0, 0, 10, 10);
    }

    /**
     * Ensures that if tryIncludeObject() is called with a new object, it returns false suggesting
     * that we have to create said object. However, if it is called with an object that has already
     * been created, it returns true suggesting that we don't have to create that object again.
     * Page-segment is false.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testTryIncludeObjectWithPageSegFalse() throws IOException {
        AFPDataObjectInfo dataInfo = createAFPDataObjectInfo();
        // An empty object needs to be created every time!
        assertFalse(sut.tryIncludeObject(dataInfo));
        sut.createObject(dataInfo);
        assertTrue(sut.tryIncludeObject(dataInfo));
    }

    /**
     * {@code testTryIncludeObjectWithPageSegFalse()} but with page-segment true.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testTryIncludeObjectWithPageSegTrue() throws IOException {
        AFPDataObjectInfo dataInfo = createAFPDataObjectInfo();
        dataInfo.setCreatePageSegment(true);
        // An empty object needs to be created every time!
        assertFalse(sut.tryIncludeObject(dataInfo));
        sut.createObject(dataInfo);
        assertTrue(sut.tryIncludeObject(dataInfo));
    }

    private AFPDataObjectInfo createAFPDataObjectInfo() {
        AFPDataObjectInfo dataInfo = new AFPDataObjectInfo();
        dataInfo.setMimeType(MimeConstants.MIME_TIFF);
        dataInfo.setData(new byte[1]);
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo(0, 0, 10, 10, 1, 0);
        dataInfo.setObjectAreaInfo(objectAreaInfo);
        return dataInfo;
    }

    @Test
    public void testIncludeObject() throws IOException {
        sut.createObject(createAFPGraphicsObjectInfo());
        sut.createObject(createAFPGraphicsObjectInfo());
        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sut.getDataStream().getCurrentPage().writeToStream(bos);
        new AFPParser(true).read(new ByteArrayInputStream(bos.toByteArray()), sb);
        assertEquals(sb.toString(), "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "INCLUDE DATA_RESOURCE\n"
                + "INCLUDE DATA_RESOURCE\n"
                + "INCLUDE DATA_RESOURCE\n"
                + "INCLUDE DATA_RESOURCE\n");
    }

    private AFPGraphicsObjectInfo createAFPGraphicsObjectInfo() {
        final AFPGraphicsObjectInfo dataInfo = new AFPGraphicsObjectInfo();
        final String uri = "test";
        dataInfo.setUri(uri);
        AFPGraphics2D graphics2D = new AFPGraphics2D(false, new AFPPaintingState(), null, null, null);
        graphics2D.setGraphicContext(new GraphicContext());
        dataInfo.setGraphics2D(graphics2D);
        dataInfo.setPainter(new Graphics2DImagePainter() {
            public void paint(Graphics2D g2d, Rectangle2D area) {
                try {
                    AFPDataObjectInfo dataObjectInfo = createAFPDataObjectInfo();
                    dataObjectInfo.setUri(uri);
                    sut.createObject(dataObjectInfo);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            public Dimension getImageSize() {
                return null;
            }
        });
        dataInfo.setObjectAreaInfo(new AFPObjectAreaInfo(0, 0, 0, 0, 0, 0));
        return dataInfo;
    }
}
