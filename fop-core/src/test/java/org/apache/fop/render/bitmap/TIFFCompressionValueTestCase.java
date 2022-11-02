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

package org.apache.fop.render.bitmap;

import java.awt.image.BufferedImage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.apache.fop.render.bitmap.TIFFCompressionValue.CCITT_T4;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.CCITT_T6;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.DEFLATE;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.JPEG;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.LZW;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.NONE;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.PACKBITS;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.ZLIB;

public class TIFFCompressionValueTestCase {

    @Test
    public void testGetName() {
        testCompressionName("NONE", NONE);
        testCompressionName("JPEG", JPEG);
        testCompressionName("PackBits", PACKBITS);
        testCompressionName("Deflate", DEFLATE);
        testCompressionName("LZW", LZW);
        testCompressionName("ZLib", ZLIB);
        testCompressionName("CCITT T.4", CCITT_T4);
        testCompressionName("CCITT T.6", CCITT_T6);
    }

    private void testCompressionName(String name, TIFFCompressionValue expected) {
        assertEquals(name, expected.getName());
        assertEquals(expected, TIFFCompressionValue.getType(name));
    }

    @Test
    public void testGetImageType() {
        for (TIFFCompressionValue value : TIFFCompressionValue.values()) {
            if (value == CCITT_T4 || value == CCITT_T6) {
                assertEquals(BufferedImage.TYPE_BYTE_BINARY, value.getImageType());
            } else {
                assertEquals(BufferedImage.TYPE_INT_ARGB, value.getImageType());
            }
        }
    }

    @Test
    public void testHasCCITTCompression() {
        for (TIFFCompressionValue value : TIFFCompressionValue.values()) {
            if (value == CCITT_T4 || value == CCITT_T6) {
                assertTrue(value.hasCCITTCompression());
            } else {
                assertFalse(value.hasCCITTCompression());
            }
        }
    }
}
