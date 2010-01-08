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

import junit.framework.TestCase;

import org.apache.xmlgraphics.java2d.CMYKColorSpace;
import org.apache.xmlgraphics.java2d.ColorExt;

import org.apache.fop.fo.Constants;
import org.apache.fop.util.ColorUtil;

/**
 * Tests the BorderProps class.
 */
public class BorderPropsTestCase extends TestCase {

    /**
     * Test serialization and deserialization to/from String.
     * @throws Exception if an error occurs
     */
    public void testSerialization() throws Exception {
        Color col = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        //Normalize: Avoid false alarms due to color conversion (rounding)
        col = ColorUtil.parseColorString(null, ColorUtil.colorToString(col));

        BorderProps b1 = new BorderProps(Constants.EN_DOUBLE, 1250,
                col, BorderProps.COLLAPSE_OUTER);
        String ser = b1.toString();
        BorderProps b2 = BorderProps.valueOf(null, ser);
        assertEquals(b1, b2);

        float[] cmyk = new float[] {1.0f, 1.0f, 0.5f, 1.0f};
        CMYKColorSpace cmykCs = CMYKColorSpace.getInstance();
        float[] rgb = cmykCs.toRGB(cmyk);
        col = ColorExt.createFromFoRgbIcc(rgb[0], rgb[1], rgb[2],
                "#CMYK", null, cmykCs, cmyk);
        b1 = new BorderProps(Constants.EN_INSET, 9999,
                col, BorderProps.SEPARATE);
        ser = b1.toString();
        b2 = BorderProps.valueOf(null, ser);
        assertEquals(b1, b2);
    }

}
