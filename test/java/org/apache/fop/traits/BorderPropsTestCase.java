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

package org.apache.fop.traits;

import java.awt.Color;

import org.junit.Test;

import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;

import org.apache.fop.fo.Constants;
import org.apache.fop.util.ColorUtil;

import static org.junit.Assert.assertEquals;

/**
 * Tests the BorderProps class.
 */
public class BorderPropsTestCase {

    /**
     * Test serialization and deserialization to/from String.
     * @throws Exception if an error occurs
     */
    @Test
    public void testSerialization() throws Exception {
        Color col = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        //Normalize: Avoid false alarms due to color conversion (rounding)
        col = ColorUtil.parseColorString(null, ColorUtil.colorToString(col));
        BorderProps sut = BorderProps.makeRectangular(Constants.EN_DOUBLE, 1250, col,
                BorderProps.Mode.COLLAPSE_OUTER);
        testSerialization(sut);

        float[] cmyk = new float[] {1.0f, 1.0f, 0.5f, 1.0f};
        col = DeviceCMYKColorSpace.createCMYKColor(cmyk);
        //Convert to sRGB with CMYK alternative as constructed by the cmyk() function
        float[] rgb = col.getRGBColorComponents(null);
        col = new ColorWithAlternatives(rgb[0], rgb[1], rgb[2], new Color[] {col});
        sut = BorderProps.makeRectangular(Constants.EN_INSET, 9999, col, BorderProps.Mode.SEPARATE);
        testSerialization(sut);
    }

    /**
     * Test serialization and deserialization to/from String.
     * @throws Exception if an error occurs
     */
    @Test
    public void testSerializationWithCornerRadii() throws Exception {
        Color col = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        //Normalize: Avoid false alarms due to color conversion (rounding)
        col = ColorUtil.parseColorString(null, ColorUtil.colorToString(col));
        for(BorderProps.Mode mode : BorderProps.Mode.values()) {
            BorderProps sut = BorderProps.makeRectangular(Constants.EN_SOLID, 10, col, mode);
            testSerialization(sut);
            sut = new BorderProps(Constants.EN_SOLID, 10, 4, 3, col, mode);
            testSerialization(sut);
        }
    }

    private void testSerialization(BorderProps borderProp) {
        assertEquals(borderProp, BorderProps.valueOf(null, borderProp.toString()));
    }

}
