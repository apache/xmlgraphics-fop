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

/**
 * OCAColor is a single component color representation that is mostly used
 * in AFP documents to represent text foreground color.
 * See also {@link OCAColorSpace}.
 *
 */
public class OCAColor extends Color {

    private static final long serialVersionUID = 1L;

    public enum OCAColorValue {
        BLUE(0x1),
        RED(0x2),
        MAGENTA(0x3),
        GREEN(0x4),
        CYAN(0x5),
        YELLOW(0x6),
        BLACK(0x8),
        BROWN(0x10),
        DEVICE_DEFAULT(0xFF07),
        MEDIUM_COLOR(0xFF08);

        final int value;

        OCAColorValue(int value) {
            this.value = value;
        }

    }

    public OCAColor(OCAColorValue oca) {
        super(oca.value);
    }

    public int getOCA() {
        return this.getRGB() & 0xFFFF;
    }

    public ColorSpace getColorSpace() {
        return new OCAColorSpace();
    }

    public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
        if (cspace.isCS_sRGB()) {
            ColorSpace oca = new OCAColorSpace();
            return oca.toRGB(new float[]{getOCA()});
        }
        return null;
    }

}
