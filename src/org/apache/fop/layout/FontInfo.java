/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.util.HashMap;

/**
 * The fontinfo for the layout and rendering of a fo document.
 * This stores the list of available fonts that are setup by
 * the renderer. The font name can be retrieved for the
 * family style and weight.
 * Currently font supported font-variant small-caps is not
 * implemented.
 */
public class FontInfo {
    public static final String DEFAULT_FONT = "any,normal,400";
    public static final int NORMAL = 400;
    public static final int BOLD = 700;

    HashMap usedFonts;
    HashMap triplets;    // look up a font-triplet to find a font-name
    HashMap fonts;    // look up a font-name to get a font (that implements FontMetric at least)

    public FontInfo() {
        this.triplets = new HashMap();
        this.fonts = new HashMap();
        this.usedFonts = new HashMap();
    }

    public boolean isSetupValid() {
        return triplets.containsKey(DEFAULT_FONT);
    }

    public void addFontProperties(String name, String family, String style,
                                  int weight) {
        /*
         * add the given family, style and weight as a lookup for the font
         * with the given name
         */

        String key = createFontKey(family, style, weight);
        this.triplets.put(key, name);
    }

    public void addMetrics(String name, FontMetric metrics) {
        // add the given metrics as a font with the given name

        this.fonts.put(name, metrics);
    }

    /**
     * Lookup a font.
     * Locate the font name for a given familyi, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     */
    public String fontLookup(String family, String style,
                             int weight) {
        String key;
        // first try given parameters
        key = createFontKey(family, style, weight);
        String f = (String)triplets.get(key);
        if(f == null) {
            // then adjust weight, favouring normal or bold
            f = findAdjustWeight(family, style, weight);

            // then try any family with orig weight
            if(f == null) {
                key = createFontKey("any", style, weight);
                f = (String)triplets.get(key);
            }

            // then try any family with adjusted weight
            if(f == null) {
                f = findAdjustWeight(family, style, weight);
            }

            // then use default
            f = (String)triplets.get(DEFAULT_FONT);

        }

        usedFonts.put(f, fonts.get(f));
        return f;
    }

    /**
     * Find a font with a given family and style by trying
     * different font weights according to the spec.
     */
    public String findAdjustWeight(String family, String style,
                             int weight) {
        String key;
        String f = null;
        int newWeight = weight;
        if(newWeight < 400) {
            while(f == null && newWeight > 0) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        } else if(newWeight == 500) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        } else if(newWeight > 500) {
            while(f == null && newWeight < 1000) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
            newWeight = weight;
            while(f == null && newWeight > 400) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        }
        if(f == null) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        }

        return f;
    }

    public boolean hasFont(String family, String style, int weight) {
        String key = createFontKey(family, style, weight);
        return this.triplets.containsKey(key);
    }

    /**
     * Creates a key from the given strings
     */
    public static String createFontKey(String family, String style,
                                       int weight) {
        return family + "," + style + "," + weight;
    }

    public HashMap getFonts() {
        return this.fonts;
    }

    /**
     * This is used by the renderers to retrieve all the
     * fonts used in the document.
     * This is for embedded font or creating a list of used fonts.
     */
    public HashMap getUsedFonts() {
        return this.usedFonts;
    }

    public FontMetric getMetricsFor(String fontName) {
        usedFonts.put(fontName, fonts.get(fontName));
        return (FontMetric)fonts.get(fontName);
    }
}

