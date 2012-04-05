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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link AFPObjectAreaInfo}.
 */
public class AFPObjectAreaInfoTestCase {

    private AFPObjectAreaInfo sut;

    /**
     * Instantiate the system under test
     */
    @Before
    public void setUp() {
        sut = new AFPObjectAreaInfo(1, 2, 3, 4, 5, 6);
    }

    /**
     * Test the getter functions with arbitrary data.
     */
    @Test
    public void testGetters() {
        assertEquals(1, sut.getX());
        assertEquals(2, sut.getY());
        assertEquals(3, sut.getWidth());
        assertEquals(4, sut.getHeight());
        assertEquals(5, sut.getWidthRes());
        assertEquals(5, sut.getHeightRes());
        assertEquals(6, sut.getRotation());
    }

    /**
     * Test the resolution setters with arbitrary data.
     */
    @Test
    public void testSetters() {
        assertEquals(5, sut.getWidthRes());
        assertEquals(5, sut.getHeightRes());

        sut.setResolution(20);
        assertEquals(20, sut.getWidthRes());
        assertEquals(20, sut.getHeightRes());

        sut.setHeightRes(10);
        assertEquals(20, sut.getWidthRes());
        assertEquals(10, sut.getHeightRes());

        sut.setWidthRes(9);
        assertEquals(9, sut.getWidthRes());
        assertEquals(10, sut.getHeightRes());
    }
}
