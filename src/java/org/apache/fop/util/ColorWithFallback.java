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

import java.awt.Color;
import java.awt.color.ColorSpace;

import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

/**
 * This class is a {@link Color} subclass adding a fallback color that FOP uses to re-serialize
 * color specifications as textual functions. The fallback is otherwise not used in producing
 * output formats.
 */
public class ColorWithFallback extends ColorWithAlternatives {

    private static final long serialVersionUID = 7913922854959637136L;

    private final Color fallback;

    /**
     * Creates a new color
     * @param cspace the color space of the primary color
     * @param components the color components
     * @param alpha the alpha component
     * @param alternativeColors the array of alternative colors if applicable (may be null)
     * @param fallback the fallback color (usually an sRGB color)
     */
    public ColorWithFallback(ColorSpace cspace, float[] components, float alpha,
            Color[] alternativeColors, Color fallback) {
        super(cspace, components, alpha, alternativeColors);
        this.fallback = fallback;
    }

    /**
     * Copy constructor adding a fallback color.
     * @param color the color to be duplicated
     * @param fallback the fallback color (usually an sRGB color)
     */
    public ColorWithFallback(Color color, Color fallback) {
        this(color.getColorSpace(), color.getColorComponents(null),
                getAlphaFloat(color), getAlternativeColors(color), fallback);
    }

    private static float getAlphaFloat(Color color) {
        float[] comps = color.getComponents(null);
        return comps[comps.length - 1]; //Alpha is on last component
    }

    private static Color[] getAlternativeColors(Color color) {
        if (color instanceof ColorWithAlternatives) {
            ColorWithAlternatives cwa = (ColorWithAlternatives)color;
            if (cwa.hasAlternativeColors()) {
                return cwa.getAlternativeColors();
            }
        }
        return null;
    }

    /**
     * Returns the fallback color.
     * @return the fallback color
     */
    public Color getFallbackColor() {
        return this.fallback;
    }

}
