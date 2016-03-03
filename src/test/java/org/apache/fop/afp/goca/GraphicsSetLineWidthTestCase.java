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

public class GraphicsSetLineWidthTestCase {

    private final int multiplier = 5;
    private final GraphicsSetLineWidth gslw = new GraphicsSetLineWidth(multiplier);

    @Test
    public void testGetDataLength() {
        assertEquals(2, gslw.getDataLength());
    }

    @Test
    public void testWriteToStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gslw.writeToStream(baos);
        baos.close();
        // expected: 0x19 (order code), 0x05 (integral multiplier)
        byte[] expected = new byte[] {0x19, 0x05};
        assertTrue(Arrays.equals(expected, baos.toByteArray()));
    }

    @Test
    public void testToString() {
        // lets make sure we keep good coverage...
        assertEquals("GraphicsSetLineWidth{multiplier=" + multiplier + "}", gslw.toString());
    }

}
