/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.fop.fo.properties;

// FOP
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;

/**
 * Collection of properties used in
 */
public class CommonFont {

    /**
     * The "font-family" property.
     */
    public String fontFamily;

    /**
     * The "font-selection-strategy" property.
     */
    public int fontSelectionStrategy;

    /**
     * The "font-size" property.
     */
    public Length fontSize;

    /**
     * The "font-stretch" property.
     */
    public int fontStretch;

    /**
     * The "font-size-adjust" property.
     */
    public Numeric fontSizeAdjust;

    /**
     * The "font-style" property.
     */
    public String fontStyle;

    /**
     * The "font-variant" property.
     */
    public int fontVariant;

    /**
     * The "font-weight" property.
     */
    public String fontWeight;

    private Font fontState;

    /**
     * Create a CommonFont object.
     * @param pList The PropertyList to get properties from.
     */
    public CommonFont(PropertyList pList) throws PropertyException {
        fontFamily = pList.get(Constants.PR_FONT_FAMILY).getString();
        fontSelectionStrategy = pList.get(Constants.PR_FONT_SELECTION_STRATEGY).getEnum();
        fontSize = pList.get(Constants.PR_FONT_SIZE).getLength();
        fontStretch = pList.get(Constants.PR_FONT_STRETCH).getEnum();
        fontSizeAdjust = pList.get(Constants.PR_FONT_SIZE_ADJUST).getNumeric();
        fontStyle = pList.get(Constants.PR_FONT_STYLE).getString();
        fontVariant = pList.get(Constants.PR_FONT_VARIANT).getEnum();
        fontWeight = pList.get(Constants.PR_FONT_WEIGHT).getString();
    }

    /**
     * Create and return a Font object based on the properties. 
     * 
     * @param fontInfo
     * @return a Font object.
     */
    public Font getFontState(FontInfo fontInfo) {
        if (fontState == null) {
            /**@todo this is ugly. need to improve. */

            int font_weight = 400;
            if (fontWeight.equals("bolder")) {
                // +100 from inherited
            } else if (fontWeight.equals("lighter")) {
                // -100 from inherited
            } else {
                try {
                    font_weight = Integer.parseInt(fontWeight);
                } catch (NumberFormatException nfe) {
                } /** TODO: log that exception */
            }
            font_weight = ((int) font_weight / 100) * 100;
            if (font_weight < 100) {
                font_weight = 100;
            } else if (font_weight > 900) {
                font_weight = 900;
            }

            // NOTE: this is incomplete. font-size may be specified with
            // various kinds of keywords too
            //int fontVariant = propertyList.get("font-variant").getEnum();
            String fname = fontInfo.fontLookup(fontFamily, fontStyle,
                                               font_weight);
            FontMetrics metrics = fontInfo.getMetricsFor(fname);
            fontState = new Font(fname, metrics, fontSize.getValue());
        }
        return fontState;
    }
}
