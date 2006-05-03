/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.render.afp;

import java.awt.Color;

/**
 * AFP only supports very basic colours and this object provides a simple
 * bean for the colour attributes.
 * @todo Is this class really necessary? Should be replaced with java.awt.Color, if possible.
 */
public class AFPFontColor {

    /**
     * Red value.
     */
    private int _red = 0;

    /**
     * Green value.
     */
    private int _green = 0;

    /**
     * Blue value.
     */
    private int _blue = 0;

    /**
     * Constructor for the AFPColor Object
     * @param red The red color intensity (0-255)
     * @param green The green color intensity (0-255)
     * @param blue The blue color intensity (0-255)
     */
    public AFPFontColor(int red, int green, int blue) {

        _red = red;
        _green = green;
        _blue = blue;

    }

    /**
     * Constructor for the AFPColor Object
     * @param col The java.awt.Color object
     */
    public AFPFontColor(Color col) {

        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();

    }

    /**
     * Returns the blue attribute
     * @return int
     */
    public int getBlue() {
        return _blue;
    }

    /**
     * Returns the green attribute
     * @return int
     */
    public int getGreen() {
        return _green;
    }

    /**
     * Returns the red attribute
     * @return int
     */
    public int getRed() {
        return _red;
    }

    /**
     * Sets the blue attribute
     * @param blue The blue value to set
     */
    public void setBlue(int blue) {
        _blue = blue;
    }

    /**
     * Sets the green attribute
     * @param green The green value to set
     */
    public void setGreen(int green) {
        _green = green;
    }

    /**
     * Sets the red attribute
     * @param red The red value to set
     */
    public void setRed(int red) {
        _red = red;
    }

    /**
     * Sets this color object to the same values
     * as the given object.
     * @param col the source color
     */
    public void setTo(AFPFontColor col) {
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
    }

    /**
     * Checks whether this object is equal to the parameter passed
     * as an argument. If the parameter is an instance of AFPColor
     * then the value are compared, otherwise the generalized equals
     * method is invoked on the parent class.
     *
     * @param obj the object to compare
     * @return boolean true if the object is equal
     */
    public boolean equals(Object obj) {

        if (obj instanceof AFPFontColor) {
            AFPFontColor c = (AFPFontColor) obj;
            if (c.getRed() == _red
                && c.getGreen() == _green
                && c.getBlue() == _blue) {
                return true;
            } else {
                return false;
            }
        } else {
            return super.equals(obj);
        }

    }

}
