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

package org.apache.fop.afp;

import java.awt.Color;

import org.apache.xmlgraphics.java2d.color.ColorConverter;

import org.apache.fop.util.ColorUtil;

/**
 * Converts to grayscale using the standard RED=30%, GREEN=59% and BLUE=11%
 * weights (see http://en.wikipedia.org/wiki/Grayscale)
 */
final class GrayScaleColorConverter implements ColorConverter {

    private static final int RED_WEIGHT = 77;
    private static final int GREEN_WEIGTH = 150;
    private static final int BLUE_WEIGHT = 28;

    private static final GrayScaleColorConverter SINGLETON = new GrayScaleColorConverter();

    private GrayScaleColorConverter() { }

    /**
     * static factory
     *
     * @return singleton instance of GrayScaleColorConverter
     */
    public static GrayScaleColorConverter getInstance() {
        return SINGLETON;
    }

    /**
     * The color is converted to CMYK with just the K component {@inheritDoc}
     */
    public Color convert(Color color) {

        float kValue = (RED_WEIGHT * color.getRed() + GREEN_WEIGTH * color.getGreen() + BLUE_WEIGHT
                * color.getBlue()) / 255.0f / 255.0f;

        return ColorUtil.toCMYKGrayColor(kValue);
    }
}
