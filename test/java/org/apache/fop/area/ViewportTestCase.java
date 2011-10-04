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

package org.apache.fop.area;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;

/**
 * Tests implementations of the {@linkplain Viewport} interface.
 */
public abstract class ViewportTestCase {

    protected void checkNonClip(Viewport v) throws Exception {
        assertFalse(v.hasClip());
        assertNull(v.getClipRectangle());
    }

    protected void checkClip(Viewport v, int expectedWidth, int expectedHeight) throws Exception {
        assertTrue(v.hasClip());
        assertEquals(new Rectangle(0, 0, expectedWidth, expectedHeight), v.getClipRectangle());
    }
}
