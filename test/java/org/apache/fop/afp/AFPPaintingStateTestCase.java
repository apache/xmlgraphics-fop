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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link AFPPaintingState}.
 */
public class AFPPaintingStateTestCase {
    private AFPPaintingState sut;

    /**
     * Set up the system under test
     */
    @Before
    public void setUp() {
        sut = new AFPPaintingState();
    }

    /**
     * Test {get,set}BitmapEncodingQuality()
     */
    @Test
    public void testGetSetBitmapEncodingQuality() {
        sut.setBitmapEncodingQuality(0.5f);
        assertEquals(0.5f, sut.getBitmapEncodingQuality(), 0.01f);

        sut.setBitmapEncodingQuality(0.9f);
        assertEquals(0.9f, sut.getBitmapEncodingQuality(), 0.01f);
    }

    /**
     * Test {,set}CanEmbedJpeg
     */
    public void testGetSetCanEmbedJpeg() {
        assertEquals(false, sut.canEmbedJpeg());
        sut.setCanEmbedJpeg(true);
        assertEquals(true, sut.canEmbedJpeg());
    }
}
