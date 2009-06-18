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

package org.apache.fop.fonts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The FontInfo for the layout and rendering of a fo document.
 * This stores the list of available fonts that are setup by
 * the renderer. The font name can be retrieved for the
 * family style and weight.
 * <br>
 * Currently font supported font-variant small-caps is not
 * implemented.
 */
public class FontInfo {
    
    /** logging instance */
    protected static Log log = LogFactory.getLog(FontInfo.class);

    /** Map containing fonts that have been used */
    private Map usedFonts;
    
    /** look up a font-triplet to find a font-name */
    private Map triplets;
    
    /** look up a font-name to get a font (that implements FontMetrics at least) */
    private Map fonts;
    
    private Collection loggedFontKeys;

    /**
     * Main constructor
     */
    public FontInfo() {
        this.triplets = new java.util.HashMap();
        this.fonts = new java.util.HashMap();
        this.usedFonts = new java.util.HashMap();
    }

    /**
     * Checks if the font setup is valid (At least the ultimate fallback font 
     * must be registered.)
     * @return True if valid
     */
    public boolean isSetupValid() {
        return triplets.containsKey(Font.DEFAULT_FONT);
    }

    /**
     * Adds a new font triplet.
     * @param name internal key
     * @param family font family name
     * @param style font style (normal, italic, oblique...)
     * @param weight font weight
     */
    public void addFontProperties(String name, String family, String style,
                                  int weight) {
        /*
         * add the given family, style and weight as a lookup for the font
         * with the given name
         */

        String key = createFontKey(family, style, weight);
        this.triplets.put(key, name);
    }

    /**
     * Adds font metrics for a specific font.
     * @param name internal key
     * @param metrics metrics to register
     */
    public void addMetrics(String name, FontMetrics metrics) {
        // add the given metrics as a font with the given name

        this.fonts.put(name, metrics);
    }

    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @param substFont true if the font may be substituted with the default font if not found
     * @return internal key
     */
    private String fontLookup(String family, String style,
                             int weight, boolean substFont) {
        String key;
        // first try given parameters
        key = createFontKey(family, style, weight);
        String f = (String)triplets.get(key);
        if (f == null) {
            // then adjust weight, favouring normal or bold
            f = findAdjustWeight(family, style, weight);

            if (!substFont && f == null) {
                return null;
            }
            // then try any family with orig weight
            if (f == null) {
                notifyFontReplacement(key);
                key = createFontKey("any", style, weight);
                f = (String)triplets.get(key);
            }

            // then use default
            if (f == null) {
                f = (String)triplets.get(Font.DEFAULT_FONT);
            }

        }

        usedFonts.put(f, fonts.get(f));
        return f;
    }

    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public String fontLookup(String family, String style,
                             int weight) {
        return fontLookup(family, style, weight, true);
    }
    
    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family (priority list)
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public String fontLookup(String[] family, String style,
                             int weight) {
        for (int i = 0; i < family.length; i++) {
            String key = fontLookup(family[i], style, weight, (i >= family.length - 1));
            if (key != null) {
                return key;
            }
        }
        throw new IllegalStateException("fontLookup must return a key on the last call");
    }
    
    private void notifyFontReplacement(String key) {
        if (loggedFontKeys == null) {
            loggedFontKeys = new java.util.HashSet();
        }
        if (!loggedFontKeys.contains(key)) {
            loggedFontKeys.add(key);
            log.warn("Font '" + key + "' not found. Substituting with default font.");
        }
    }
    
    /**
     * Find a font with a given family and style by trying
     * different font weights according to the spec.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public String findAdjustWeight(String family, String style,
                             int weight) {
        String key;
        String f = null;
        int newWeight = weight;
        if (newWeight < 400) {
            while (f == null && newWeight > 0) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        } else if (newWeight == 500) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        } else if (newWeight > 500) {
            while (f == null && newWeight < 1000) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
            newWeight = weight;
            while (f == null && newWeight > 400) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        }
        if (f == null) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        }

        return f;
    }

    /**
     * Determines if a particular font is available.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return True if available
     */
    public boolean hasFont(String family, String style, int weight) {
        String key = createFontKey(family, style, weight);
        return this.triplets.containsKey(key);
    }

    /**
     * Creates a key from the given strings.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public static String createFontKey(String family, String style,
                                       int weight) {
        return family + "," + style + "," + weight;
    }

    /**
     * Gets a Map of all registred fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map getFonts() {
        return java.util.Collections.unmodifiableMap(this.fonts);
    }

    /**
     * This is used by the renderers to retrieve all the
     * fonts used in the document.
     * This is for embedded font or creating a list of used fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map getUsedFonts() {
        return this.usedFonts;
    }

    /**
     * Returns the FontMetrics for a particular font
     * @param fontName internal key
     * @return font metrics
     */
    public FontMetrics getMetricsFor(String fontName) {
        FontMetrics metrics = (FontMetrics)fonts.get(fontName);
        usedFonts.put(fontName, metrics);
        return metrics;
    }

    /**
     * Returns the first triplet matching the given font name.
     * As there may be multiple triplets matching the font name
     * the result set is sorted first to guarantee consistent results.
     * @param fontName The font name we are looking for
     * @return The first triplet for the given font name
     */
    private String getTripletFor(String fontName) {
        List foundTriplets = new ArrayList();
        for (Iterator iter = triplets.entrySet().iterator(); iter.hasNext();) {
            Map.Entry tripletEntry = (Map.Entry) iter.next();
            if (fontName.equals(((String)tripletEntry.getValue()))) {
                foundTriplets.add(tripletEntry.getKey());
            }
        }
        if (foundTriplets.size() > 0) {
            Collections.sort(foundTriplets);
            return (String)foundTriplets.get(0);
        }
        return null;
    }
    
    /**
     * Returns the font style for a particular font.
     * There may be multiple font styles matching this font. Only the first
     * found is returned. Searching is done on a sorted list to guarantee consistent
     * results.
     * @param fontName internal key
     * @return font style
     */
    public String getFontStyleFor(String fontName) {
        String triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.substring(triplet.indexOf(',') + 1, triplet.lastIndexOf(','));
        }
        return "";
    }
    
    /**
     * Returns the font weight for a particular font.
     * There may be multiple font weights matching this font. Only the first
     * found is returned. Searching is done on a sorted list to guarantee consistent
     * results.
     * @param fontName internal key
     * @return font weight
     */
    public String getFontWeightFor(String fontName) {
        String triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.substring(triplet.lastIndexOf(',') + 1);
        }
        return "";
    }
    
}



