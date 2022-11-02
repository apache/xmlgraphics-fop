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

// $Id$

package org.apache.fop.afp.goca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphicsSetFractionalLineWidthTestCase {

    private final float multiplier = 5.25f;
    private final GraphicsSetFractionalLineWidth gsflw = new GraphicsSetFractionalLineWidth(multiplier);

    @Test
    public void testGetDataLength() {
        assertEquals(4, gsflw.getDataLength());
    }

    @Test
    public void testWriteToStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gsflw.writeToStream(baos);
        baos.close();
        // note: 0.25 = 64/256 and 64 = 4*16, so 0x40
        // expected: 0x11 (order code), 0x02 (2 bytes next), 0x05 (integral multiplier), 0x40 (fractional
        // multiplier)
        byte[] expected = new byte[] {0x11, 0x02, 0x05, 0x40};
        assertTrue(Arrays.equals(expected, baos.toByteArray()));
    }

    @Test
    public void testToString() {
        // lets make sure we keep good coverage...
        assertEquals("GraphicsSetFractionalLineWidth{multiplier=" + multiplier + "}", gsflw.toString());
    }

}
