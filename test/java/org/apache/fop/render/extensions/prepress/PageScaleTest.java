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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;

import org.junit.Test;

/**
 * Tests for the fox:scale extension property.
 */
public class PageScaleTest {

    /** 1 value is used for both x and y. */
    @Test
    public void testScale1() {
        Point2D res = PageScale.getScale(".5");
        assertEquals(0.5, res.getX(), 0.0);
        assertEquals(0.5, res.getY(), 0.0);
    }

    /** Two values, used resp. for x and y. */
    @Test
    public void testScale2() {
        Point2D res = PageScale.getScale("1. \t \n 1.2");
        assertEquals(1.0, res.getX(), 0.0);
        assertEquals(1.2, res.getY(), 0.0);
    }

    /** Scale must not contain units. */
    @Test
    public void testScaleFail() {
        try {
            PageScale.getScale("0.5mm 0.5cm");
            fail("Expected IllegalArgumentException. Scale shouldn't contain units");
        } catch (IllegalArgumentException iae) {
            // Good!
        }
    }

    /** @{code null} is returned when scale is unspecified. */
    @Test
    public void testScaleNull() {
        Point2D res = PageScale.getScale(null);
        assertNull("Result should be null", res);
        res = PageScale.getScale("");
        assertNull("Result should be null", res);
    }

}
