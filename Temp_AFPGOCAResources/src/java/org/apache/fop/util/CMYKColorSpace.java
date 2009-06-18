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

/**
 * This class represents an uncalibrated CMYK color space. It is used by
 * the JpegImage class.
 */
public class CMYKColorSpace extends ColorSpace {

    private static CMYKColorSpace instance;

    /**
     * @see java.awt.color.ColorSpace#ColorSpace(int, int)
     */
    protected CMYKColorSpace(int type, int numcomponents) {
        super(type, numcomponents);
    }

    /**
     * Returns an instance of an uncalibrated CMYK color space.
     * @return CMYKColorSpace the requested color space object
     */
    public static CMYKColorSpace getInstance() {
        if (instance == null) {
            instance = new CMYKColorSpace(TYPE_CMYK, 4);
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public float[] toRGB(float[] colorvalue) {
        return new float [] {
            (1 - colorvalue[0]) * (1 - colorvalue[3]),
            (1 - colorvalue[1]) * (1 - colorvalue[3]),
            (1 - colorvalue[2]) * (1 - colorvalue[3])};
    }

    /**
     * {@inheritDoc}
     */
    public float[] fromRGB(float[] rgbvalue) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * {@inheritDoc}
     */
    public float[] toCIEXYZ(float[] colorvalue) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * {@inheritDoc}
     */
    public float[] fromCIEXYZ(float[] colorvalue) {
        throw new UnsupportedOperationException("NYI");
    }

}
