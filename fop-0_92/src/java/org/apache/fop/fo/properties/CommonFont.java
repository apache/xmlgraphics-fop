/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
import java.util.List;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;

/**
 * Collection of properties used in
 */
public class CommonFont {

    /**
     * The "font-family" property.
     */
    private String[] fontFamily;

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
    public int fontStyle;

    /**
     * The "font-variant" property.
     */
    public int fontVariant;

    /**
     * The "font-weight" property.
     */
    public int fontWeight;

    private Font fontState;

    /**
     * Create a CommonFont object.
     * @param pList The PropertyList to get properties from.
     */
    public CommonFont(PropertyList pList) throws PropertyException {
        List lst = pList.get(Constants.PR_FONT_FAMILY).getList();
        fontFamily = new String[lst.size()];
        for (int i = 0, c = lst.size(); i < c; i++) {
            fontFamily[i] = ((Property)lst.get(i)).getString();
        }
        if (fontFamily.length == 0) {
            //Shouldn't happen, but we never know.
            fontFamily = new String[] {"any"};
        }
        fontSelectionStrategy = pList.get(Constants.PR_FONT_SELECTION_STRATEGY).getEnum();
        fontSize = pList.get(Constants.PR_FONT_SIZE).getLength();
        fontStretch = pList.get(Constants.PR_FONT_STRETCH).getEnum();
        fontSizeAdjust = pList.get(Constants.PR_FONT_SIZE_ADJUST).getNumeric();
        fontStyle = pList.get(Constants.PR_FONT_STYLE).getEnum();
        fontVariant = pList.get(Constants.PR_FONT_VARIANT).getEnum();
        fontWeight = pList.get(Constants.PR_FONT_WEIGHT).getEnum();
    }

    /** @return the first font-family name in the list */
    public String getFirstFontFamily() {
        return this.fontFamily[0];
    }
    
    /** @return the font-family names */
    public String[] getFontFamily() {
        return this.fontFamily;
    }
    
    /**
     * Overrides the font-family.
     * @param value the new font-family
     */
    public void overrideFontFamily(String value) {
        this.fontFamily = new String[] {value};
        
    }
    
    /**
     * Create and return a Font object based on the properties. 
     * 
     * @param fontInfo
     * @return a Font object.
     */
    public Font getFontState(FontInfo fontInfo, PercentBaseContext context) {
        if (fontState == null) {
            /**@todo this is ugly. need to improve. */

            int font_weight = 400;
            if (fontWeight == Constants.EN_BOLDER) {
                // +100 from inherited
            } else if (fontWeight == Constants.EN_LIGHTER) {
                // -100 from inherited
            } else {
                switch (fontWeight) {
                case Constants.EN_100: font_weight = 100; break;
                case Constants.EN_200: font_weight = 200; break;
                case Constants.EN_300: font_weight = 300; break;
                case Constants.EN_400: font_weight = 400; break;
                case Constants.EN_500: font_weight = 500; break;
                case Constants.EN_600: font_weight = 600; break;
                case Constants.EN_700: font_weight = 700; break;
                case Constants.EN_800: font_weight = 800; break;
                case Constants.EN_900: font_weight = 900; break;
                }
            }

            String style;
            switch (fontStyle) {
            case Constants.EN_ITALIC: 
                style = "italic";
                break;
            case Constants.EN_OBLIQUE: 
                style = "oblique";
                break;
            case Constants.EN_BACKSLANT: 
                style = "backslant";
                break;
            default:
                style = "normal";
            }
            // NOTE: this is incomplete. font-size may be specified with
            // various kinds of keywords too
            //int fontVariant = propertyList.get("font-variant").getEnum();
            FontTriplet triplet = fontInfo.fontLookup(getFontFamily(), style,
                                               font_weight);
            fontState = fontInfo.getFontInstance(triplet, fontSize.getValue(context));
        }
        return fontState;
    }

}
