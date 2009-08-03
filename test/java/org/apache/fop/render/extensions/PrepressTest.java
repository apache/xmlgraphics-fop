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

package org.apache.fop.render.extensions;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.apache.fop.render.extensions.prepress.PageBoundaries;
import org.apache.fop.render.extensions.prepress.PageScaleAttributes;

/**
 * Base class for automated tests for
 * {@link org.apache.fop.render.extensions.prepress.PageBoundariesAttributes}
 * and
 * {@link org.apache.fop.render.extensions.prepress.PageScaleAttributes}
 */
public class PrepressTest extends TestCase {

    private static final int W = 20000;
    private static final int H = 15000;
    private static final Rectangle TEST_AREA = new Rectangle(0, 0, W, H);
    private static final String BLEED1 = "5pt";
    private static final String CROP_OFFSET1 = "8pt";

    /**
     * Main constructor
     * @param name the name of the test case
     */
    public PrepressTest(String name) {
        super(name);
    }

    /**
     * Tests for 'scale' extension attribute
     */
    public void testScaleOk() throws Exception {
        Point2D res = PageScaleAttributes.getScaleAttributes("0.5");
        assertEquals("Points should be equal", res.getX(), res.getY(), 0);
    }

    public void testScaleFailIllArgExc() throws Exception {
        try {
            Point2D res = PageScaleAttributes.getScaleAttributes("0.5mm 0.5cm");
            fail("Expected IllegalArgumentException. Scale shouldn't contain units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }

    public void testScaleNotEqual() throws Exception {
        Point2D res = PageScaleAttributes.getScaleAttributes("0.5 0.6");
        assertFalse("Points shouldn't be equal", res.getX() == res.getY());
    }

    public void testScaleNull() throws Exception {
        Point2D res = PageScaleAttributes.getScaleAttributes(null);
        assertNull("Result shouldn't be null", res);
    }

    /**
     * Tests for page boundaries
     */
    public void testBoxOk1() throws Exception {
        Rectangle res = PageBoundaries.getBleedBoxRectangle(TEST_AREA, null);
        assertSame("Result should be the same as TEST_AREA object", res, TEST_AREA);

        res = PageBoundaries.getBleedBoxRectangle(null, BLEED1);
        assertNull(res);
    }

    public void testBoxOk2() throws Exception {
        PageBoundaries boundaries = new PageBoundaries(
                TEST_AREA.getSize(), BLEED1, CROP_OFFSET1, null);
        assertNotNull("Expected not null object", boundaries.getBleedBox());
        assertEquals(-5000, boundaries.getBleedBox().getX(), 1);
        assertEquals(-5000, boundaries.getBleedBox().getY(), 1);
        assertEquals(30000, boundaries.getBleedBox().getWidth(), 1);
        assertEquals(25000, boundaries.getBleedBox().getHeight(), 1);

        assertNotNull("Expected not null object", boundaries.getMediaBox());
        assertEquals(-8000, boundaries.getMediaBox().getX(), 1);
        assertEquals(-8000, boundaries.getMediaBox().getY(), 1);
        assertEquals(36000, boundaries.getMediaBox().getWidth(), 1);
        assertEquals(31000, boundaries.getMediaBox().getHeight(), 1);

        assertEquals(TEST_AREA, boundaries.getTrimBox());
        assertEquals(boundaries.getMediaBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(
                TEST_AREA.getSize(), BLEED1, CROP_OFFSET1, "media-box");
        assertEquals(boundaries.getMediaBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(
                TEST_AREA.getSize(), BLEED1, CROP_OFFSET1, "bleed-box");
        assertEquals(boundaries.getBleedBox(), boundaries.getCropBox());

        boundaries = new PageBoundaries(
                TEST_AREA.getSize(), BLEED1, CROP_OFFSET1, "trim-box");
        assertEquals(boundaries.getTrimBox(), boundaries.getCropBox());
        assertEquals(TEST_AREA, boundaries.getCropBox());

        boundaries = new PageBoundaries(
                TEST_AREA.getSize(), BLEED1, null, null);
        assertNotNull("Expected not null object", boundaries.getBleedBox());
        assertEquals(-5000, boundaries.getBleedBox().getX(), 1);
        assertEquals(-5000, boundaries.getBleedBox().getY(), 1);
        assertEquals(30000, boundaries.getBleedBox().getWidth(), 1);
        assertEquals(25000, boundaries.getBleedBox().getHeight(), 1);
        assertEquals(boundaries.getBleedBox(), boundaries.getCropBox());
        assertEquals(boundaries.getBleedBox(), boundaries.getMediaBox());
    }

    public void testBoxIllArgExc() throws Exception {
        try {
            PageBoundaries boundaries = new PageBoundaries(
                    TEST_AREA.getSize(), "0", null, null);
            fail("Expected IllegalArgumentException. Box should have units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }
}
