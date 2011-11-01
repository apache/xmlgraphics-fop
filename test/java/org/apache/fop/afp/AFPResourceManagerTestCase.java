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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Test case for {@link AFPResourceManager}.
 */
public class AFPResourceManagerTestCase {

    private AFPResourceManager sut;

    @Before
    public void setUp() throws IOException {
        sut = new AFPResourceManager();
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
}
