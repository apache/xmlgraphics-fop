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
 * Color helper class.
 * <p>
 * This class extends java.awt.Color class keeping track of the original color
 * property values specified by the fo user in a rgb-icc call.
 */
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
    public static ColorExt createFromSvgIccColor(float red, float green,
            float blue, float opacity, String profileName, String profileHref,
            ColorSpace profileCS, float[] colorValues) {
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
        return this.colorSpace;
    }

    /**
     * @return the original color values
     */
    public float[] getOriginalColorComponents() {
        return this.colorValues;
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
        sb.append("\"" + this.iccProfileSrc + "\"");
        float[] colorComponents = this.getColorComponents(null);
        for (int ix = 0; ix < colorComponents.length; ix++) {
            sb.append(",");
            sb.append(colorComponents[ix]);
        }
        sb.append(")");
        return sb.toString();
    }

}
