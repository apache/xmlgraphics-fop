/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

package org.apache.fop.render.rtf;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IBorderAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;

/** Constants for RTF border attribute names, and a static method for converting
 *  fo attribute strings. */

public final class BorderAttributesConverter {

    /**
     * Constructor is private, because it's just a utility class.
     */
    private BorderAttributesConverter() {
    }
    
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
            attrs.set(IBorderAttributes.BORDER_COLOR, border.getBorderColor(side));
            attrs.set(convertAttributetoRtf(styleEnum));
            //division by 50 to convert millipoints to twips
            attrs.set(IBorderAttributes.BORDER_WIDTH, border.getBorderWidth(side, false) / 50);
            attributes.set(controlWord, attrs);
            //Don't set BORDER_SPACE, because it makes the table look quite broken: 
            //vertical and horizontal borders don't meet at corners.
            //attrs.setTwips(IBorderAttributes.BORDER_SPACE, border.getPadding(side, false, null));
            //attributes.set(controlWord, attrs);
        } else {
            // Here padding specified, but corresponding border is not available
            
            // Padding in millipoints
            double paddingPt = border.getPadding(side, false, null) / 1000.0;
            // Padding in twips
            int padding = (int) Math.round(paddingPt * FoUnitsConverter.POINT_TO_TWIPS);
            
            // Add padding to corresponding space (space-before or space-after)
            // if side == START or END, do nothing
            if (side == CommonBorderPaddingBackground.BEFORE) {
                attributes.addIntegerValue(padding, RtfText.SPACE_BEFORE);
            } else if (side == CommonBorderPaddingBackground.AFTER) {
                attributes.addIntegerValue(padding, RtfText.SPACE_AFTER);
            }
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
           return IBorderAttributes.BORDER_NIL;
       } else if (iBorderStyle == Constants.EN_SOLID) {
           return IBorderAttributes.BORDER_SINGLE_THICKNESS;
/*        } else if (iBorderStyle==Constants.EN_THIN) {
                       return IBorderAttributes.BORDER_SINGLE_THICKNESS;
       } else if (iBorderStyle==Constants.EN_THICK) {
           return IBorderAttributes.BORDER_DOUBLE_THICKNESS;
       } else if (iBorderStyle==Constants.EN_ value.equals("shadowed")) {
           return IBorderAttributes.BORDER_SHADOWED;*/
       } else if (iBorderStyle == Constants.EN_DOUBLE) {
           return IBorderAttributes.BORDER_DOUBLE;
       } else if (iBorderStyle == Constants.EN_DOTTED) {
           return IBorderAttributes.BORDER_DOTTED;
       } else if (iBorderStyle == Constants.EN_DASHED) {
           return IBorderAttributes.BORDER_DASH;
       } else if (iBorderStyle == Constants.EN_GROOVE) {
           return IBorderAttributes.BORDER_ENGRAVE;
       } else if (iBorderStyle == Constants.EN_RIDGE) {
           return IBorderAttributes.BORDER_EMBOSS;
       } else if (iBorderStyle == Constants.EN_INSET) {
           return IBorderAttributes.BORDER_ENGRAVE;
       } else if (iBorderStyle == Constants.EN_OUTSET) {
           return IBorderAttributes.BORDER_EMBOSS;
/*        } else if (iBorderStyle==Constants value.equals("hairline")) {
           return IBorderAttributes.BORDER_HAIRLINE;*/
/*        } else if (iBorderStyle==Constant value.equals("dot-dash")) {
           return IBorderAttributes.BORDER_DOT_DASH;
       } else if (iBorderStyle==Constant value.equals("dot-dot-dash")) {
           return IBorderAttributes.BORDER_DOT_DOT_DASH;
       } else if (iBorderStyle==Constant value.equals("triple")) {
           return IBorderAttributes.BORDER_TRIPLE;
       } else if (iBorderStyle==Constant value.equals("wavy")) {
           return IBorderAttributes.BORDER_WAVY;
       } else if (iBorderStyle==Constant value.equals("wavy-double")) {
           return IBorderAttributes.BORDER_WAVY_DOUBLE;
       } else if (iBorderStyle==Constant value.equals("striped")) {
           return IBorderAttributes.BORDER_STRIPED;
       } else if (iBorderStyle==Constant value.equals("emboss")) {
           return IBorderAttributes.BORDER_EMBOSS;
       } else if (iBorderStyle==Constant value.equals("engrave")) {
           return IBorderAttributes.BORDER_ENGRAVE;*/
       } else {
           return IBorderAttributes.BORDER_SINGLE_THICKNESS;
       }

   }
}