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

package org.apache.fop.render.extensions.prepress;

import java.awt.Dimension;
import java.awt.Rectangle;

import junit.framework.TestCase;

/**
 * Tests for the fox:bleed, fox:crop-offset, fox:crop-box extension properties.
 */
public class PageBoundariesTest extends TestCase {

    private static final Dimension TEST_AREA_SIZE = new Dimension(20000, 15000);

    private static final Rectangle TEST_AREA = new Rectangle(TEST_AREA_SIZE);

    private static final String BLEED = "5pt";

    private static final String CROP_OFFSET = "8pt";

    /**
     * Default constructor.
     */
    public PageBoundariesTest() {
        throw new UnsupportedOperationException("Not implemented"); // TODO
    }

    /**
     * Creates a test case with the given name.
     *
     * @param name name for the test case
     */
    public PageBoundariesTest(String name) {
        super(name);
    }

    /** Test for page boundaries. */
    public void testBoundaries1() {
        PageBoundaries boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, null);
        assertEquals(TEST_AREA, boundaries.getTrimBox());

        Rectangle bleedBox = boundaries.getBleedBox();
        assertNotNull("Expected not null object", bleedBox);
        assertEquals(-5000, bleedBox.x);
        assertEquals(-5000, bleedBox.y);
        assertEquals(30000, bleedBox.width);
        assertEquals(25000, bleedBox.height);

        Rectangle mediaBox = boundaries.getMediaBox();
        assertNotNull("Expected not null object", mediaBox);
        assertEquals(-8000, mediaBox.x);
        assertEquals(-8000, mediaBox.y);
        assertEquals(36000, mediaBox.width);
        assertEquals(31000, mediaBox.height);
    }

    /** Test for page boundaries. */
    public void testBoundaries2() {
        PageBoundaries boundaries = new PageBoundaries(
                TEST_AREA_SIZE, BLEED, null, null);
        Rectangle bleedBox = boundaries.getBleedBox();
        assertNotNull("Expected not null object", bleedBox);
        assertEquals(-5000, bleedBox.x);
        assertEquals(-5000, bleedBox.y);
        assertEquals(30000, bleedBox.width);
        assertEquals(25000, bleedBox.height);
        assertEquals(bleedBox, boundaries.getMediaBox());
    }

    /** Test for the different values of crop-box. */
    public void testCropBox() {
        PageBoundaries boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, null);
        assertEquals(boundaries.getMediaBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, "");
        assertEquals(boundaries.getMediaBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, "trim-box");
        assertEquals(TEST_AREA, boundaries.getCropBox());

        boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, "bleed-box");
        assertEquals(boundaries.getBleedBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(TEST_AREA_SIZE, BLEED, CROP_OFFSET, "media-box");
        assertEquals(boundaries.getMediaBox(), boundaries.getCropBox());
    }

    /** Test for default values returned when properties are null. */
    public void testBoundariesNull() {
        PageBoundaries b = new PageBoundaries(TEST_AREA_SIZE, null, null, null);

        assertEquals("Result should be the same as TEST_AREA object", b.getTrimBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getBleedBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getMediaBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getCropBox(), TEST_AREA);
    }

    /** Units must be specified. */
    public void testBoundariesFail() {
        try {
            new PageBoundaries(TEST_AREA_SIZE, "0", null, null);
            fail("Expected IllegalArgumentException. Box should have units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }
}
