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

package org.apache.fop.fonts;

import java.awt.Rectangle;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests for MultiByteFont's glyph bounding boxes, which are stored packed as
 * ints rather than as a Rectangle per glyph.
 */
public class MultiByteFontTestCase {

    /** Not embeddable (there is no embed URI), so bounding boxes are indexed by glyph index. */
    private MultiByteFont font() {
        return new MultiByteFont(null, EmbeddingMode.AUTO);
    }

    @Test
    public void testPackedBBoxArray() {
        MultiByteFont font = font();
        font.setBBoxArray(new int[] {1, 2, 3, 4, 5, 6, 7, 8});

        assertEquals(new Rectangle(1, 2, 3, 4), font.getBoundingBox(0, 1));
        assertEquals(new Rectangle(5, 6, 7, 8), font.getBoundingBox(1, 1));
    }

    @Test
    public void testRectangleBBoxArray() {
        MultiByteFont font = font();
        font.setBBoxArray(new Rectangle[] {new Rectangle(1, 2, 3, 4), new Rectangle(5, 6, 7, 8)});

        assertEquals(new Rectangle(1, 2, 3, 4), font.getBoundingBox(0, 1));
        assertEquals(new Rectangle(5, 6, 7, 8), font.getBoundingBox(1, 1));
    }

    @Test
    public void testBoundingBoxIsScaledBySize() {
        MultiByteFont font = font();
        font.setBBoxArray(new int[] {-1, 2, 3, 4});

        assertEquals(new Rectangle(-10, 20, 30, 40), font.getBoundingBox(0, 10));
    }
}
