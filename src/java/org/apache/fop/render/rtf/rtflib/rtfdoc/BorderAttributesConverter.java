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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.render.rtf.FOPRtfAttributes;

/** Constants for RTF border attribute names, and a static method for converting
 *  fo attribute strings. */

public class BorderAttributesConverter {

    /** Constant for a single-thick border */
    public static final String BORDER_SINGLE_THICKNESS = "brdrs";
    /** Constant for a double-thick border */
    public static final String BORDER_DOUBLE_THICKNESS = "brdrth";
    /** Constant for a shadowed border */
    public static final String BORDER_SHADOWED = "brdrsh";
    /** Constant for a double border */
    public static final String BORDER_DOUBLE = "brdrdb";
    /** Constant for a dotted border */
    public static final String BORDER_DOTTED = "brdrdot";
    /** Constant for a dashed border */
    public static final String BORDER_DASH = "brdrdash";
    /** Constant for a hairline border */
    public static final String BORDER_HAIRLINE = "brdrhair";
    /** Constant for a small-dashed border */
    public static final String BORDER_DASH_SMALL = "brdrdashsm";
    /** Constant for a dot-dashed border */
    public static final String BORDER_DOT_DASH = "brdrdashd";
    /** Constant for a dot-dot-dashed border */
    public static final String BORDER_DOT_DOT_DASH = "brdrdashdd";
    /** Constant for a triple border */
    public static final String BORDER_TRIPLE = "brdrtriple";
    /** Constant for a think-thin-small border */
    public static final String BORDER_THINK_THIN_SMALL = "brdrtnthsg";
    /** Constant for a thin-thick-small border */
    public static final String BORDER_THIN_THICK_SMALL = "brdrthtnsg";
    /** Constant for a thin-thick-thin-small border */
    public static final String BORDER_THIN_THICK_THIN_SMALL = "brdrthtnthsg";
    /** Constant for a think-thin-medium border */
    public static final String BORDER_THINK_THIN_MEDIUM = "brdrtnthmg";
    /** Constant for a thin-thick-medium border */
    public static final String BORDER_THIN_THICK_MEDIUM = "brdrthtnmg";
    /** Constant for a thin-thick-thin-medium border */
    public static final String BORDER_THIN_THICK_THIN_MEDIUM = "brdrthtnthmg";
    /** Constant for a think-thin-large border */
    public static final String BORDER_THINK_THIN_LARGE = "brdrtnthlg";
    /** Constant for a thin-thick-large border */
    public static final String BORDER_THIN_THICK_LARGE = "brdrthtnlg";
    /** Constant for a thin-thick-thin-large border */
    public static final String BORDER_THIN_THICK_THIN_LARGE = "brdrthtnthlg";
    /** Constant for a wavy border */
    public static final String BORDER_WAVY = "brdrwavy";
    /** Constant for a double wavy border */
    public static final String BORDER_WAVY_DOUBLE = "brdrwavydb";
    /** Constant for a striped border */
    public static final String BORDER_STRIPED = "brdrdashdotstr";
    /** Constant for an embossed border */
    public static final String BORDER_EMBOSS = "brdremboss";
    /** Constant for an engraved border */
    public static final String BORDER_ENGRAVE = "brdrengrave";
    /** Constant for an nil border */
    public static final String BORDER_NIL = "brdrnil";
    /** Constant for border color */
    public static final String BORDER_COLOR = "brdrcf";
    /** Constant for border space */
    public static final String BORDER_SPACE = "brsp";
    /** Constant for border width */
    public static final String BORDER_WIDTH = "brdrw";

    /** String array of border attributes */
    public static final String [] BORDERS = new String[] {
        BORDER_SINGLE_THICKNESS,    BORDER_DOUBLE_THICKNESS,            BORDER_SHADOWED,
        BORDER_DOUBLE,              BORDER_DOTTED,                      BORDER_DASH,
        BORDER_HAIRLINE,            BORDER_DASH_SMALL,                  BORDER_DOT_DASH,
        BORDER_DOT_DOT_DASH,        BORDER_TRIPLE,                      BORDER_THINK_THIN_SMALL,
        BORDER_THIN_THICK_SMALL,    BORDER_THIN_THICK_THIN_SMALL,       BORDER_THINK_THIN_MEDIUM,
        BORDER_THIN_THICK_MEDIUM,   BORDER_THIN_THICK_THIN_MEDIUM,      BORDER_THINK_THIN_LARGE,
        BORDER_THIN_THICK_LARGE,    BORDER_THIN_THICK_THIN_LARGE,       BORDER_WAVY,
        BORDER_WAVY_DOUBLE,         BORDER_STRIPED,                     BORDER_EMBOSS,
        BORDER_ENGRAVE,             BORDER_COLOR,                       BORDER_SPACE,
        BORDER_WIDTH
    };

    /**
     * Create a border control word in attributes, with border properties 
     * as specified in color, style and width.
     * @param border The CommonBorderPaddingBackground object.
     * @param side The START, END, BEFORE, AFTER enum from CommonBorderPaddingBackground. 
     * @param attributes The attributes list to set the border control word.
     * @param controlWord The border control word.
     */
    public static void makeBorder(CommonBorderPaddingBackground border, int side,
            RtfAttributes attributes, String controlWord) {
        int styleEnum = border.getBorderStyle(side);
        if (styleEnum != Constants.EN_NONE) {
            FOPRtfAttributes attrs = new FOPRtfAttributes();
            attrs.set(BORDER_COLOR, border.getBorderColor(side));
            attrs.set(convertAttributetoRtf(styleEnum));
            attrs.set(BORDER_WIDTH, border.getBorderWidth(side, false));
            attributes.set(controlWord, attrs);
        }
    }

    /**
    *
    * @param iBorderStyle the border style to be converted
    * @return String with the converted border style
    */
   public static String convertAttributetoRtf(int iBorderStyle) {
       // Added by Normand Masse
       // "solid" is interpreted like "thin"
       if (iBorderStyle == Constants.EN_NONE) {
           return BorderAttributesConverter.BORDER_NIL;
       } else if (iBorderStyle == Constants.EN_SOLID) {
           return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
/*        } else if (iBorderStyle==Constants.EN_THIN) {
                       return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
       } else if (iBorderStyle==Constants.EN_THICK) {
           return BorderAttributesConverter.BORDER_DOUBLE_THICKNESS;
       } else if (iBorderStyle==Constants.EN_ value.equals("shadowed")) {
           return BorderAttributesConverter.BORDER_SHADOWED;*/
       } else if (iBorderStyle == Constants.EN_DOUBLE) {
           return BorderAttributesConverter.BORDER_DOUBLE;
       } else if (iBorderStyle == Constants.EN_DOTTED) {
           return BorderAttributesConverter.BORDER_DOTTED;
       } else if (iBorderStyle == Constants.EN_DASHED) {
           return BorderAttributesConverter.BORDER_DASH;
/*        } else if (iBorderStyle==Constants value.equals("hairline")) {
           return BorderAttributesConverter.BORDER_HAIRLINE;*/
/*        } else if (iBorderStyle==Constant value.equals("dot-dash")) {
           return BorderAttributesConverter.BORDER_DOT_DASH;
       } else if (iBorderStyle==Constant value.equals("dot-dot-dash")) {
           return BorderAttributesConverter.BORDER_DOT_DOT_DASH;
       } else if (iBorderStyle==Constant value.equals("triple")) {
           return BorderAttributesConverter.BORDER_TRIPLE;
       } else if (iBorderStyle==Constant value.equals("wavy")) {
           return BorderAttributesConverter.BORDER_WAVY;
       } else if (iBorderStyle==Constant value.equals("wavy-double")) {
           return BorderAttributesConverter.BORDER_WAVY_DOUBLE;
       } else if (iBorderStyle==Constant value.equals("striped")) {
           return BorderAttributesConverter.BORDER_STRIPED;
       } else if (iBorderStyle==Constant value.equals("emboss")) {
           return BorderAttributesConverter.BORDER_EMBOSS;
       } else if (iBorderStyle==Constant value.equals("engrave")) {
           return BorderAttributesConverter.BORDER_ENGRAVE;*/
       } else {
           return null;
       }

   }
}