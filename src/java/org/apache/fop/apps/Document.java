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

package org.apache.fop.apps;

// Java
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;


// FOP

import org.apache.fop.configuration.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;

/**
 * Class storing information for the FOP Document being processed, and managing
 * the processing of it.
 */
public class Document {

    /** The parent Fop object */
    private Fop fop;

    /** Map containing fonts that have been used */
    private Map usedFonts;

    /** look up a font-triplet to find a font-name */
    private Map triplets;

    /** look up a font-name to get a font (that implements FontMetrics at least) */
    private Map fonts;

    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    private Logger log;
    /**
     * Main constructor
     * @param fop the Fop object that is the "parent" of this Document
     */
    public Document(Fop fop) {
        this.fop = fop;
        this.triplets = new java.util.HashMap();
        this.fonts = new java.util.HashMap();
        this.usedFonts = new java.util.HashMap();
        log = Logger.getLogger(Fop.fopPackage);
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
     * @return internal key
     */
    public String fontLookup(String family, String style,
                             int weight) {
        String key;
        // first try given parameters
        key = createFontKey(family, style, weight);
        String f = (String)triplets.get(key);
        if (f == null) {
            // then adjust weight, favouring normal or bold
            f = findAdjustWeight(family, style, weight);

            // then try any family with orig weight
            if (f == null) {
                key = createFontKey("any", style, weight);
                f = (String)triplets.get(key);
            }

            // then try any family with adjusted weight
            if (f == null) {
                f = findAdjustWeight(family, style, weight);
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
        usedFonts.put(fontName, fonts.get(fontName));
        return (FontMetrics)fonts.get(fontName);
    }

    /**
     * Public accessor for the parent Fop of this Document
     * @return the parent Fop for this Document
     */
    public Fop getFop() {
        return fop;
    }

    /**
     * Retuns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * @return the Logger to be used for processing this Document
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * @return the FOUserAgent used for processing this document
     */
    public FOUserAgent getUserAgent() {
        return getFop().getUserAgent();
    }

}
