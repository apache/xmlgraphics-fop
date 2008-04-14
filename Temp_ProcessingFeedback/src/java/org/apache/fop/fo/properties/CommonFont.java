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
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;

/**
 * Collection of CommonFont properties
 */
public final class CommonFont {

    /** cache holding canonical CommonFont instances (only those with
     *  absolute font-size and font-size-adjust) */
    private static final PropertyCache cache = new PropertyCache();
    
    /**
     * Class holding canonical instances of bundles of the
     * cacheable (non-relative) CommonFont properties
     *
     */
    protected static final class CachedCommonFont {
        
        /** cache holding all canonical instances */
        private static final PropertyCache cache = new PropertyCache();
        
        private int hash = 0;
        
        /**
         * The "font-family" property.
         */
        private final FontFamilyProperty fontFamily;
    
        /**
         * The "font-selection-strategy" property.
         */
        private final EnumProperty fontSelectionStrategy;
    
        /**
         * The "font-stretch" property.
         */
        private final EnumProperty fontStretch;

        /**
         * The "font-style" property.
         */
        private final EnumProperty fontStyle;

        /**
         * The "font-variant" property.
         */
        private final EnumProperty fontVariant;

        /**
         * The "font-weight" property.
         */
        private final EnumProperty fontWeight;
        
        /**
         * Constructor
         * 
         * @param fontFamily    the font-family property
         * @param fontSelectionStrategy the font-selection-strategy property
         * @param fontStretch   the font-stretch property
         * @param fontStyle     the font-style property
         * @param fontVariant   the font-variant property
         * @param fontWeight    the font-weight property
         */
        private CachedCommonFont(FontFamilyProperty fontFamily,
                         EnumProperty fontSelectionStrategy,
                         EnumProperty fontStretch,
                         EnumProperty fontStyle,
                         EnumProperty fontVariant,
                         EnumProperty fontWeight) {
            this.fontFamily = fontFamily;
            this.fontSelectionStrategy = fontSelectionStrategy;
            this.fontStretch = fontStretch;
            this.fontStyle = fontStyle;
            this.fontVariant = fontVariant;
            this.fontWeight = fontWeight;
        }
        
        /**
         * Returns the canonical instance corresponding to the given
         * properties
         * 
         * @param fontFamily    the font-family property
         * @param fontSelectionStrategy the font-selection-strategy property
         * @param fontStretch   the font-stretch property
         * @param fontStyle     the font-style property
         * @param fontVariant   the font-variant property
         * @param fontWeight    the font-weight property
         * @return  the canonical instance
         */
        private static final CachedCommonFont getInstance(FontFamilyProperty fontFamily,
                           EnumProperty fontSelectionStrategy,
                           EnumProperty fontStretch,
                           EnumProperty fontStyle,
                           EnumProperty fontVariant,
                           EnumProperty fontWeight) {
            return cache.fetch(new CachedCommonFont(
                                    fontFamily,
                                    fontSelectionStrategy,
                                    fontStretch,
                                    fontStyle,
                                    fontVariant,
                                    fontWeight));
        }
        
        /** @return the first font-family name in the list */
        private String getFirstFontFamily() {
            return ((Property) fontFamily.list.get(0)).getString();
        }
        
        /** @return an array with the font-family names */
        private String[] getFontFamily() {
            List lst = fontFamily.getList();
            String[] fontFamily = new String[lst.size()];
            for (int i = 0, c = lst.size(); i < c; i++) {
                fontFamily[i] = ((Property)lst.get(i)).getString();
            }
            return fontFamily;
        }
        
        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            
            if (o instanceof CachedCommonFont) {
                CachedCommonFont ccf = (CachedCommonFont) o;
                return (ccf.fontFamily == this.fontFamily)
                    && (ccf.fontSelectionStrategy == this.fontSelectionStrategy)
                    && (ccf.fontStretch == this.fontStretch)
                    && (ccf.fontStyle == this.fontStyle)
                    && (ccf.fontVariant == this.fontVariant)
                    && (ccf.fontWeight == this.fontWeight);
            }
            return false;
        }
        
        /** {@inheritDoc} */
        public int hashCode() {
            if (this.hash == 0) {
                int hash = 17;
                hash = 37 * hash + (fontFamily == null ? 0 : fontFamily.hashCode());
                hash = 37 * hash + (fontSelectionStrategy == null ? 0 : fontSelectionStrategy.hashCode());
                hash = 37 * hash + (fontStretch == null ? 0 : fontStretch.hashCode());
                hash = 37 * hash + (fontStyle == null ? 0 : fontStyle.hashCode());
                hash = 37 * hash + (fontVariant == null ? 0 : fontVariant.hashCode());
                hash = 37 * hash + (fontStretch == null ? 0 : fontStretch.hashCode());
                this.hash = hash;
            }
            return this.hash;
        }
        
    }

    /**
     * The cached CommonFont properties (access these through the getters)
     * The remaining properties, font-size and font-size-adjust,
     * will only be cached values if they are absolute.
     */
    private final CachedCommonFont cachedCommonFont;
    
    /**
     * The "font-size" property.
     */
    public final Length fontSize;

    /**
     * The "font-size-adjust" property.
     */
    public final Numeric fontSizeAdjust;

    /**
     * Construct a CommonFont instance
     * 
     * @param ccf       the cached CommonFont properties
     * @param fontSize  the font-size (possibly non-cached)
     * @param fontSizeAdjust    the font-size-adjust (possibly non-cached)
     */
    private CommonFont(CachedCommonFont ccf, 
                       Length fontSize, 
                       Numeric fontSizeAdjust) {
        this.cachedCommonFont = ccf;
        this.fontSize = fontSize;
        this.fontSizeAdjust = fontSizeAdjust;
    }

    /**
     * Returns a CommonFont instance for the given PropertyList
     * If the font-size and font-size-adjust properties are absolute
     * the entire instance will be cached.
     * If not, then a distinct instance will be returned, with
     * as much cached information as possible.
     * 
     * @param pList the PropertyList to get the properties from
     * @return  a CommonFont instance corresponding to the properties
     * @throws PropertyException    if there was a problem getting the properties
     */
    public static CommonFont getInstance(PropertyList pList) throws PropertyException {
        FontFamilyProperty fontFamily = (FontFamilyProperty) pList.get(Constants.PR_FONT_FAMILY);
        EnumProperty fontSelectionStrategy = (EnumProperty) pList.get(Constants.PR_FONT_SELECTION_STRATEGY);
        EnumProperty fontStretch = (EnumProperty) pList.get(Constants.PR_FONT_STRETCH);
        EnumProperty fontStyle = (EnumProperty) pList.get(Constants.PR_FONT_STYLE);
        EnumProperty fontVariant = (EnumProperty) pList.get(Constants.PR_FONT_VARIANT);
        EnumProperty fontWeight = (EnumProperty) pList.get(Constants.PR_FONT_WEIGHT);
        CachedCommonFont cachedCommonFont = CachedCommonFont.getInstance(
                                                fontFamily, 
                                                fontSelectionStrategy, 
                                                fontStretch, 
                                                fontStyle, 
                                                fontVariant, 
                                                fontWeight);
        
        Numeric fontSizeAdjust = pList.get(Constants.PR_FONT_SIZE_ADJUST).getNumeric();
        Length fontSize = pList.get(Constants.PR_FONT_SIZE).getLength();
        CommonFont cf = new CommonFont(cachedCommonFont, fontSize, fontSizeAdjust);
        if (fontSize.isAbsolute() && fontSizeAdjust.isAbsolute()) {
            return cache.fetch(cf);
        } else {
            return cf;
        }
        
    }
    
    /** @return the first font-family name in the list */
    public String getFirstFontFamily() {
        return cachedCommonFont.getFirstFontFamily();
    }
    
    /** @return the "font-selection-strategy" property */
    public int getFontSelectionStrategy() {
        return cachedCommonFont.fontSelectionStrategy.getEnum();
    }

    /** @return the "font-stretch" property */
    public int getFontStretch() {
        return cachedCommonFont.fontStretch.getEnum();
    }
    
    /** @return the "font-style" property */
    public int getFontStyle() {
        return cachedCommonFont.fontStyle.getEnum();
    }
    
    /** @return the "font-variant" property */
    public int getFontVariant() {
        return cachedCommonFont.fontVariant.getEnum();
    }

    /** @return the "font-weight" property */
    public int getFontWeight() {
        return cachedCommonFont.fontWeight.getEnum();
    }
    
    /**
     * Create and return an array of <code>FontTriplets</code> based on 
     * the properties stored in the instance variables.
     * 
     * @param fontInfo
     * @return a Font object.
     */
    public FontTriplet[] getFontState(FontInfo fontInfo) {
        int font_weight;
        switch (cachedCommonFont.fontWeight.getEnum()) {
        case Constants.EN_100: font_weight = 100; break;
        case Constants.EN_200: font_weight = 200; break;
        case Constants.EN_300: font_weight = 300; break;
        case Constants.EN_400: font_weight = 400; break;
        case Constants.EN_500: font_weight = 500; break;
        case Constants.EN_600: font_weight = 600; break;
        case Constants.EN_700: font_weight = 700; break;
        case Constants.EN_800: font_weight = 800; break;
        case Constants.EN_900: font_weight = 900; break;
        default: font_weight = 400;
        }

        String style;
        switch (cachedCommonFont.fontStyle.getEnum()) {
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
        FontTriplet[] triplets = fontInfo.fontLookup(
                                    cachedCommonFont.getFontFamily(), 
                                    style, font_weight);
        return triplets;
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o instanceof CommonFont) {
            CommonFont cf = (CommonFont) o;
            return (cf.cachedCommonFont == this.cachedCommonFont
                    && cf.fontSize == this.fontSize
                    && cf.fontSizeAdjust == this.fontSizeAdjust);
        }
        return false;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + cachedCommonFont.hashCode();
        hash = 37 * hash + fontSize.hashCode();
        hash = 37 * hash + fontSizeAdjust.hashCode();
        return hash;
    }
}
