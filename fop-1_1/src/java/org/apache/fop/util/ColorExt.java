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
import java.util.Arrays;

/**
 * Color helper class.
 * <p>
 * This class extends java.awt.Color class keeping track of the original color
 * property values specified by the fo user in a rgb-icc call.
 * @deprecated Replaced by {@link org.apache.xmlgraphics.java2d.color.ColorWithAlternatives}
 */
@Deprecated
public final class ColorExt extends Color {
    //
    private static final long serialVersionUID = 1L;

    // Values of fop-rgb-icc arguments
    private float rgbReplacementRed;
    private float rgbReplacementGreen;
    private float rgbReplacementBlue;

    private String iccProfileName;
    private String iccProfileSrc;
    private ColorSpace colorSpace;

    private float[] colorValues;

    /*
     * Helper for createFromFoRgbIcc
     */
    private ColorExt(ColorSpace colorSpace, float[] colorValues, float opacity) {
        super(colorSpace, colorValues, opacity);
    }

    /*
     * Helper for createFromSvgIccColor
     */
    private ColorExt(float red, float green, float blue, float opacity) {
        super(red, green, blue, opacity);
    }

    /**
     * Create ColorExt object backup up FO's rgb-icc color function
     *
     * @param redReplacement
     *            Red part of RGB replacement color that will be used when ICC
     *            profile can not be loaded
     * @param greenReplacement
     *            Green part of RGB replacement color that will be used when ICC
     *            profile can not be loaded
     * @param blueReplacement
     *            Blue part of RGB replacement color that will be used when ICC
     *            profile can not be loaded
     * @param profileName
     *            Name of ICC profile
     * @param profileSrc
     *            Source of ICC profile
     * @param colorSpace
     *            ICC ColorSpace for the ICC profile
     * @param iccValues
     *            color values
     * @return the requested color object
     */
    public static ColorExt createFromFoRgbIcc(float redReplacement,
            float greenReplacement, float blueReplacement, String profileName,
            String profileSrc, ColorSpace colorSpace, float[] iccValues) {
        ColorExt ce = new ColorExt(colorSpace, iccValues, 1.0f);
        ce.rgbReplacementRed = redReplacement;
        ce.rgbReplacementGreen = greenReplacement;
        ce.rgbReplacementBlue = blueReplacement;
        ce.iccProfileName = profileName;
        ce.iccProfileSrc = profileSrc;
        ce.colorSpace = colorSpace;
        ce.colorValues = iccValues;
        return ce;
    }

    /**
     * Create ColorExt object backing up SVG's icc-color function.
     *
     * @param red
     *            Red value resulting from the conversion from the user provided
     *            (icc) color values to the batik (rgb) color space
     * @param green
     *            Green value resulting from the conversion from the user
     *            provided (icc) color values to the batik (rgb) color space
     * @param blue
     *            Blue value resulting from the conversion from the user
     *            provided (icc) color values to the batik (rgb) color space
     * @param opacity
     *            Opacity
     * @param profileName
     *            ICC profile name
     * @param profileHref
     *            the URI to the color profile
     * @param profileCS
     *            ICC ColorSpace profile
     * @param colorValues
     *            ICC color values
     * @return the requested color object
     */
    public static ColorExt createFromSvgIccColor(                // CSOK: ParameterNumber
            float red, float green,
            float blue, float opacity, String profileName, String profileHref,
            ColorSpace profileCS, float[] colorValues) {
        //TODO this method is not referenced by FOP, can it be deleted?
        ColorExt ce = new ColorExt(red, green, blue, opacity);
        ce.rgbReplacementRed = -1;
        ce.rgbReplacementGreen = -1;
        ce.rgbReplacementBlue = -1;
        ce.iccProfileName = profileName;
        ce.iccProfileSrc = profileHref;
        ce.colorSpace = profileCS;
        ce.colorValues = colorValues;
        return ce;

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        //implementation from the superclass should be good enough for our purposes
        return super.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ColorExt other = (ColorExt)obj;
        //TODO maybe use super.getColorComponents() instead
        if (!Arrays.equals(colorValues, other.colorValues)) {
            return false;
        }
        if (iccProfileName == null) {
            if (other.iccProfileName != null) {
                return false;
            }
        } else if (!iccProfileName.equals(other.iccProfileName)) {
            return false;
        }
        if (iccProfileSrc == null) {
            if (other.iccProfileSrc != null) {
                return false;
            }
        } else if (!iccProfileSrc.equals(other.iccProfileSrc)) {
            return false;
        }
        if (Float.floatToIntBits(rgbReplacementBlue)
                != Float.floatToIntBits(other.rgbReplacementBlue)) {
            return false;
        }
        if (Float.floatToIntBits(rgbReplacementGreen)
                != Float.floatToIntBits(other.rgbReplacementGreen)) {
            return false;
        }
        if (Float.floatToIntBits(rgbReplacementRed)
                != Float.floatToIntBits(other.rgbReplacementRed)) {
            return false;
        }
        return true;
    }

    /**
     * Get ICC profile name
     *
     * @return ICC profile name
     */
    public String getIccProfileName() {
        return this.iccProfileName;
    }

    /**
     * Get ICC profile source
     *
     * @return ICC profile source
     */
    public String getIccProfileSrc() {
        return this.iccProfileSrc;
    }

    /**
     * @return the original ColorSpace
     */
    public ColorSpace getOrigColorSpace() {
        //TODO this method is probably unnecessary due to super.cs and getColorSpace()
        return this.colorSpace;
    }

    /**
     * Returns the original color values.
     * @return the original color values
     */
    public float[] getOriginalColorComponents() {
        //TODO this method is probably unnecessary due to super.fvalue and getColorComponents()
        float[] copy = new float[this.colorValues.length];
        System.arraycopy(this.colorValues, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Create string representation of fop-rgb-icc function call to map this
     * ColorExt settings
     * @return the string representing the internal fop-rgb-icc() function call
     */
    public String toFunctionCall() {
        StringBuffer sb = new StringBuffer(40);
        sb.append("fop-rgb-icc(");
        sb.append(this.rgbReplacementRed + ",");
        sb.append(this.rgbReplacementGreen + ",");
        sb.append(this.rgbReplacementBlue + ",");
        sb.append(this.iccProfileName + ",");
        if (this.iccProfileSrc != null) {
            sb.append("\"" + this.iccProfileSrc + "\"");
        }
        float[] colorComponents = this.getColorComponents(null);
        for (int ix = 0; ix < colorComponents.length; ix++) {
            sb.append(",");
            sb.append(colorComponents[ix]);
        }
        sb.append(")");
        return sb.toString();
    }

}
