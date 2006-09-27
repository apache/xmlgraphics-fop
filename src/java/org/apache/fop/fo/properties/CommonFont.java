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

package org.apache.fop.fo.properties;

// FOP
import java.util.List;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

import org.axsl.fontR.Font;
import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontException;
import org.axsl.fontR.FontServer;
import org.axsl.fontR.FontUse;

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

    private FontServer fontServer;

    private FontUse fontUse;

    /**
     * Create a CommonFont object.
     * @param pList The PropertyList to get properties from.
     * @param fontServer the server from which to get the font
     */
    public CommonFont(PropertyList pList, FontServer fontServer)
            throws PropertyException {
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
        this.fontServer = fontServer;
    }
    
    /**
     * Return the font size based on the properties.
     * @return the font size in millipoints.
     */
    public int getFontSize(PercentBaseContext context) {
        return fontSize.getValue(context);
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
     * Return a font use based on the properties.
     * 
     * @param fontConsumer font consumer to which the returned font use will be associated
     * @return a FontUse instance corresponding to the properties
     */
    public FontUse getFontState(FontConsumer fontConsumer, PercentBaseContext context) {
        if (fontUse == null) {
            /**@todo this is ugly. need to improve. */

            short font_weight = Font.FONT_WEIGHT_ANY;
            if (fontWeight == Constants.EN_BOLDER) {
                // +100 from inherited
            } else if (fontWeight == Constants.EN_LIGHTER) {
                // -100 from inherited
            } else {
                switch (fontWeight) {
                case Constants.EN_100: font_weight = Font.FONT_WEIGHT_100; break;
                case Constants.EN_200: font_weight = Font.FONT_WEIGHT_200; break;
                case Constants.EN_300: font_weight = Font.FONT_WEIGHT_300; break;
                case Constants.EN_400: font_weight = Font.FONT_WEIGHT_400; break;
                case Constants.EN_500: font_weight = Font.FONT_WEIGHT_500; break;
                case Constants.EN_600: font_weight = Font.FONT_WEIGHT_600; break;
                case Constants.EN_700: font_weight = Font.FONT_WEIGHT_700; break;
                case Constants.EN_800: font_weight = Font.FONT_WEIGHT_800; break;
                case Constants.EN_900: font_weight = Font.FONT_WEIGHT_900; break;
                }
            }

            byte style;
            switch (fontStyle) {
            case Constants.EN_ITALIC: style = Font.FONT_STYLE_ITALIC; break;
            case Constants.EN_OBLIQUE: style = Font.FONT_STYLE_OBLIQUE; break;
            case Constants.EN_BACKSLANT: style = Font.FONT_STYLE_BACKSLANT; break;
            default:
                style = Font.FONT_STYLE_NORMAL;
            }
            byte variant = fontVariant == Constants.EN_SMALL_CAPS ? Font.FONT_VARIANT_SMALL_CAPS
                    : Font.FONT_VARIANT_NORMAL;
            byte stretch;
            /* TODO vh: handle narrower and wider */
            switch (fontStretch) {
            case Constants.EN_NORMAL: stretch = Font.FONT_STRETCH_NORMAL; break;
            case Constants.EN_ULTRA_CONDENSED: stretch = Font.FONT_STRETCH_ULTRA_CONDENSED; break;
            case Constants.EN_EXTRA_CONDENSED: stretch = Font.FONT_STRETCH_EXTRA_CONDENSED; break;
            case Constants.EN_CONDENSED: stretch = Font.FONT_STRETCH_CONDENSED; break;
            case Constants.EN_SEMI_CONDENSED: stretch = Font.FONT_STRETCH_SEMI_CONDENSED; break;
            case Constants.EN_SEMI_EXPANDED: stretch = Font.FONT_STRETCH_SEMI_EXPANDED; break;
            case Constants.EN_EXPANDED: stretch = Font.FONT_STRETCH_EXPANDED; break;
            case Constants.EN_EXTRA_EXPANDED: stretch = Font.FONT_STRETCH_EXTRA_EXPANDED; break;
            case Constants.EN_ULTRA_EXPANDED: stretch = Font.FONT_STRETCH_ULTRA_EXPANDED; break;
            default: stretch = Font.FONT_STRETCH_NORMAL;
            }
            try {
                fontUse = fontConsumer.selectFontXSL(Font.FONT_SELECTION_AUTO/*TODO vh*/,
                        getFontFamily(), style, font_weight, variant,
                        stretch, fontSize.getValue(context), ' '/*TODO vh*/);
            } catch (FontException e) {
                try {
                    fontUse = fontConsumer.selectFontXSL(Font.FONT_SELECTION_AUTO, new String[] {"any"},
                            Font.FONT_STYLE_ANY,
                            Font.FONT_WEIGHT_ANY,
                            Font.FONT_VARIANT_ANY,
                            Font.FONT_STRETCH_ANY,
                            10000, ' ');
                } catch (FontException e1) {
                    // Should never happen (see area.RenderPagesModel)
                }
            }
        }
        return fontUse;
    }

}
