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
    
    /** collection of missing fonts; used to make sure the user gets 
     *  a warning for a missing font only once (not every time the font is used)
     */
    private Collection loggedFontKeys;

    /** Cache for Font instances. */
    private Map fontInstanceCache = new java.util.HashMap();
    
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
    public void addFontProperties(String name, String family, String style, int weight) {
        addFontProperties(name, createFontKey(family, style, weight));
    }

    /**
     * Adds a new font triplet.
     * @param name internal key
     * @param triplet the font triplet to associate with the internal key
     */
    public void addFontProperties(String name, FontTriplet triplet) {
        /*
         * add the given family, style and weight as a lookup for the font
         * with the given name
         */
        this.triplets.put(triplet, name);
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
     * @param substFont true if the font may be substituted with the 
     *                  default font if not found
     * @return internal key
     */
    private FontTriplet fontLookup(String family, String style,
                             int weight, boolean substFont) {
        if (log.isTraceEnabled()) {
            log.trace("Font lookup: " + family + " " + style + " " + weight);
        }
        FontTriplet startKey = createFontKey(family, style, weight); 
        FontTriplet key = startKey;
        // first try given parameters
        String f = getInternalFontKey(key);
        if (f == null) {
            // then adjust weight, favouring normal or bold
            key = findAdjustWeight(family, style, weight);
            f = getInternalFontKey(key);

            if (!substFont && f == null) {
                return null;
            }
            
            // only if the font may be substituted
            // fallback 1: try the same font-family and weight with default style
            if (f == null) {
                key = createFontKey(family, Font.STYLE_NORMAL, weight);
                f = getInternalFontKey(key);
            }
            
            // fallback 2: try the same font-family with default style and weight
            if (f == null) {
                key = createFontKey(family, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
                f = getInternalFontKey(key);
            }
            
            // fallback 3: try any family with orig style/weight
            if (f == null) {
                key = createFontKey("any", style, weight);
                f = getInternalFontKey(key);
            }

            // last resort: use default
            if (f == null) {
                key = Font.DEFAULT_FONT;
                f = getInternalFontKey(key);
            }
        }

        if (f != null) {
            if (key != startKey) {
                notifyFontReplacement(startKey, key);
            }
            return key;
        } else {
            return null;
        }
    }

    /**
     * Tells this class that the font with the given internal name has been used.
     * @param internalName the internal font name (F1, F2 etc.)
     */
    public void useFont(String internalName) {
        usedFonts.put(internalName, fonts.get(internalName));
    }
    
    /**
     * Retrieves a (possibly cached) Font instance based on a FontTriplet and a font size. 
     * @param triplet the font triplet designating the requested font
     * @param fontSize the font size
     * @return the requested Font instance
     */
    public Font getFontInstance(FontTriplet triplet, int fontSize) {
        Map sizes = (Map)fontInstanceCache.get(triplet);
        if (sizes == null) {
            sizes = new java.util.HashMap();
            fontInstanceCache.put(triplet, sizes);
        }
        Integer size = new Integer(fontSize);
        Font font = (Font)sizes.get(size);
        if (font == null) {
            String fname = getInternalFontKey(triplet);
            useFont(fname);
            FontMetrics metrics = getMetricsFor(fname);
            font = new Font(fname, triplet, metrics, fontSize);
            sizes.put(size, font);
        }
        return font;
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
     * @return the font triplet of the font chosen
     */
    public FontTriplet fontLookup(String family, String style,
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
     * @return font triplet of the font chosen
     */
    public FontTriplet fontLookup(String[] family, String style,
                             int weight) {
        for (int i = 0; i < family.length; i++) {
            FontTriplet triplet = fontLookup(family[i], style, weight, (i >= family.length - 1));
            if (triplet != null) {
                return triplet;
            }
        }
        throw new IllegalStateException("fontLookup must return a key on the last call");
    }
    
    private void notifyFontReplacement(FontTriplet replacedKey, FontTriplet newKey) {
        if (loggedFontKeys == null) {
            loggedFontKeys = new java.util.HashSet();
        }
        if (!loggedFontKeys.contains(replacedKey)) {
            loggedFontKeys.add(replacedKey);
            log.warn("Font '" + replacedKey + "' not found. "
                    + "Substituting with '" + newKey + "'.");
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
    public FontTriplet findAdjustWeight(String family, String style,
                             int weight) {
        FontTriplet key = null;
        String f = null;
        int newWeight = weight;
        if (newWeight < 400) {
            while (f == null && newWeight > 0) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
        } else if (newWeight == 500) {
            key = createFontKey(family, style, 400);
            f = getInternalFontKey(key);
        } else if (newWeight > 500) {
            while (f == null && newWeight < 1000) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
            newWeight = weight;
            while (f == null && newWeight > 400) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = getInternalFontKey(key);
            }
        }
        if (f == null && weight != 400) {
            key = createFontKey(family, style, 400);
            f = getInternalFontKey(key);
        }

        if (f != null) {
            return key;
        } else {
            return null;
        }
    }

    /**
     * Determines if a particular font is available.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return True if available
     */
    public boolean hasFont(String family, String style, int weight) {
        FontTriplet key = createFontKey(family, style, weight);
        return this.triplets.containsKey(key);
    }

    /**
     * Returns the internal font key (F1, F2, F3 etc.) for a given triplet.
     * @param triplet the font triplet
     * @return the associated internal key or null, if not found
     */
    public String getInternalFontKey(FontTriplet triplet) {
        return (String)triplets.get(triplet);
    }
    
    /**
     * Creates a key from the given strings.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public static FontTriplet createFontKey(String family, String style,
                                       int weight) {
        return new FontTriplet(family, style, weight);
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
    public FontTriplet getTripletFor(String fontName) {
        List foundTriplets = new ArrayList();
        for (Iterator iter = triplets.entrySet().iterator(); iter.hasNext();) {
            Map.Entry tripletEntry = (Map.Entry) iter.next();
            if (fontName.equals(((String)tripletEntry.getValue()))) {
                foundTriplets.add(tripletEntry.getKey());
            }
        }
        if (foundTriplets.size() > 0) {
            Collections.sort(foundTriplets);
            return (FontTriplet)foundTriplets.get(0);
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
        FontTriplet triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.getStyle();
        } else {
            return "";
        }
    }
    
    /**
     * Returns the font weight for a particular font.
     * There may be multiple font weights matching this font. Only the first
     * found is returned. Searching is done on a sorted list to guarantee consistent
     * results.
     * @param fontName internal key
     * @return font weight
     */
    public int getFontWeightFor(String fontName) {
        FontTriplet triplet = getTripletFor(fontName);
        if (triplet != null) {
            return triplet.getWeight();
        } else {
            return 0;
        }
    }
}
