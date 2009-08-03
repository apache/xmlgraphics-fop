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

import org.apache.fop.render.extensions.prepress.PageBoundariesAttributes;
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
        Rectangle res = PageBoundariesAttributes.getBleedBoxRectangle(TEST_AREA, null);
        assertSame("Result should be the same as TEST_AREA object", res, TEST_AREA);

        res = PageBoundariesAttributes.getBleedBoxRectangle(null, BLEED1);
        assertNull(res);
    }

    public void testBoxOk2() throws Exception {
        Rectangle res1 = PageBoundariesAttributes.getBleedBoxRectangle(TEST_AREA, BLEED1);
        assertNotNull("Expected not null object", res1);
        assertEquals(-5000, res1.getX(), 1);
        assertEquals(-5000, res1.getY(), 1);
        assertEquals(30000, res1.getWidth(), 1);
        assertEquals(25000, res1.getHeight(), 1);

        Rectangle res2 = PageBoundariesAttributes.getMediaBoxRectangle(TEST_AREA, CROP_OFFSET1);
        assertNotNull("Expected not null object", res2);
        assertEquals(-8000, res2.getX(), 1);
        assertEquals(-8000, res2.getY(), 1);
        assertEquals(36000, res2.getWidth(), 1);
        assertEquals(31000, res2.getHeight(), 1);

        Rectangle res3 = PageBoundariesAttributes.getCropBoxRectangle(
                TEST_AREA, res1, res2, "media-box");
        assertNotNull("Expected not null object", res3);
        assertEquals(res3, res2);

        res3 = PageBoundariesAttributes.getCropBoxRectangle(
                TEST_AREA, res1, res2, "bleed-box");
        assertNotNull("Expected not null object", res3);
        assertEquals(res3, res1);

        res3 = PageBoundariesAttributes.getCropBoxRectangle(
                TEST_AREA, res1, res2, "trim-box");
        assertNotNull("Expected not null object", res3);
        assertEquals(res3, TEST_AREA);
    }

    public void testBoxIllArgExc() throws Exception {
        try {
            Rectangle res = PageBoundariesAttributes.getBleedBoxRectangle(TEST_AREA, "0");
            fail("Expected IllegalArgumentException. Box should have units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }
}
