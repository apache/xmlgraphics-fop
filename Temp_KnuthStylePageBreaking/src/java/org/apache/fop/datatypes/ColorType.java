/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.datatypes;

import java.awt.Color;

/**
 * A colour quantity in XSL.
 */
public interface ColorType {

    /**
     * Returns the blue component of the color.
     * @return float a value between 0.0 and 1.0
     */
    public float getBlue();

    /**
     * Returns the green component of the color.
     * @return float a value between 0.0 and 1.0
     */
    public float getGreen();

    /**
     * Returns the red component of the color.
     * @return float a value between 0.0 and 1.0
     */
    public float getRed();

    /**
     * Returns the alpha (degree of opaque-ness) component of the color.
     * @return float a value between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public float getAlpha();

    /**
     * Returns an AWT instance of this color
     * @return float the AWT color represented by this ColorType instance
     */
    public Color getAWTColor();
}