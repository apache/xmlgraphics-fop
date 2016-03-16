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

package org.apache.fop.util;

import java.awt.color.ColorSpace;

import org.apache.fop.util.OCAColor.OCAColorValue;

/**
 * The OCA color space is a subset of RGB that includes a limited set of colors.
 * The color value is specified with one unsigned binary component that
 * specifies a named color using a two-byte value from the Standard
 * OCA Color Value table.
 *
 */
public class OCAColorSpace extends ColorSpace {

    private static final long serialVersionUID = 1L;

    protected OCAColorSpace() {
        super(ColorSpace.TYPE_RGB, 1);
    }

    public float[] fromCIEXYZ(float[] colorvalue) {
        throw new UnsupportedOperationException("Color conversion from CIE XYZ to OCA is not possible");
    }

    public float[] fromRGB(float[] rgbvalue) {
        throw new UnsupportedOperationException("Color conversion from RGB to OCA is not possible");
    }

    public float[] toCIEXYZ(float[] colorvalue) {
        float[] rgb = toRGB(colorvalue);
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        return sRGB.toCIEXYZ(rgb);
    }

    public float[] toRGB(float[] colorvalue) {
        int oca = (int) colorvalue[0];
        if (oca == OCAColorValue.BLACK.value) {
            return new float[]{0, 0, 0};
        } else if (oca == OCAColorValue.BLUE.value) {
            return new float[]{0, 0, 1.0f};
        } else if (oca == OCAColorValue.BROWN.value) {
            return new float[]{0.565f, 0.188f, 0};
        } else if (oca == OCAColorValue.CYAN.value) {
            return new float[]{0, 1.0f, 1.0f};
        } else if (oca == OCAColorValue.GREEN.value) {
            return new float[]{0, 1.0f, 0};
        } else if (oca == OCAColorValue.MAGENTA.value) {
            return new float[]{1.0f, 0, 1.0f};
        } else if (oca == OCAColorValue.RED.value) {
            return new float[]{1.0f, 0, 0};
        } else if (oca == OCAColorValue.YELLOW.value) {
            return new float[]{1.0f, 1.0f, 0};
        }
        return null;
    }

}
