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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.junit.Test;

/**
 * Tests for the fox:bleed, fox:crop-offset, fox:crop-box extension properties.
 */
public class PageBoundariesTestCase {

    private static final Dimension TEST_AREA_SIZE = new Dimension(20000, 15000);

    private static final Rectangle TEST_AREA = new Rectangle(TEST_AREA_SIZE);

    private static final String BLEED = "5pt";

    private static final String CROP_OFFSET = "8pt";

    /** Test for page boundaries. */
    @Test
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
    @Test
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

    /** Two values for the properties. */
    @Test
    public void testBoundaries2Values() {
        PageBoundaries boundaries = new PageBoundaries(
                TEST_AREA_SIZE, "5pt  10pt", "6pt \t 12pt", null);
        Rectangle bleedBox = boundaries.getBleedBox();
        assertEquals(-10000, bleedBox.x);
        assertEquals(-5000,  bleedBox.y);
        assertEquals(40000,  bleedBox.width);
        assertEquals(25000,  bleedBox.height);

        Rectangle mediaBox = boundaries.getMediaBox();
        assertEquals(-12000, mediaBox.x);
        assertEquals(-6000,  mediaBox.y);
        assertEquals(44000,  mediaBox.width);
        assertEquals(27000,  mediaBox.height);
    }

    /** Three values for the properties. */
    @Test
    public void testBoundaries3Values() {
        PageBoundaries boundaries = new PageBoundaries(
                TEST_AREA_SIZE, "5pt  10pt 7pt", "6pt \t 12pt 14pt", null);
        Rectangle bleedBox = boundaries.getBleedBox();
        assertEquals(-10000, bleedBox.x);
        assertEquals(-5000,  bleedBox.y);
        assertEquals(40000,  bleedBox.width);
        assertEquals(27000,  bleedBox.height);

        Rectangle mediaBox = boundaries.getMediaBox();
        assertEquals(-12000, mediaBox.x);
        assertEquals(-6000,  mediaBox.y);
        assertEquals(44000,  mediaBox.width);
        assertEquals(35000,  mediaBox.height);
    }

    /** Four values for the properties. */
    @Test
    public void testBoundaries4Values() {
        PageBoundaries boundaries = new PageBoundaries(
                TEST_AREA_SIZE, "5pt  6pt 7pt   8pt", "9pt 10pt  11pt 12pt", null);
        Rectangle bleedBox = boundaries.getBleedBox();
        assertEquals(-8000,  bleedBox.x);
        assertEquals(-5000,  bleedBox.y);
        assertEquals(34000,  bleedBox.width);
        assertEquals(27000,  bleedBox.height);

        Rectangle mediaBox = boundaries.getMediaBox();
        assertEquals(-12000, mediaBox.x);
        assertEquals(-9000,  mediaBox.y);
        assertEquals(42000,  mediaBox.width);
        assertEquals(35000,  mediaBox.height);
    }

    /** Test for the different values of crop-box. */
    @Test
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
    @Test
    public void testBoundariesNull() {
        PageBoundaries b = new PageBoundaries(TEST_AREA_SIZE, null, null, null);

        assertEquals("Result should be the same as TEST_AREA object", b.getTrimBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getBleedBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getMediaBox(), TEST_AREA);
        assertEquals("Result should be the same as TEST_AREA object", b.getCropBox(), TEST_AREA);
    }

    /** Units must be specified. */
    @Test
    public void testBoundariesFail() {
        try {
            new PageBoundaries(TEST_AREA_SIZE, "0", null, null);
            fail("Expected IllegalArgumentException. Box should have units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }
}
